import SparkMD5 from 'spark-md5'
import { getUploadSignature, saveImageMetadata } from '@/api'
import { useUploadStore } from '@/stores/upload'

function computeMd5(file) {
  return new Promise((resolve, reject) => {
    const blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice
    const chunkSize = 2097152
    const chunks = Math.ceil(file.size / chunkSize)
    let currentChunk = 0
    const spark = new SparkMD5.ArrayBuffer()
    const fileReader = new FileReader()

    fileReader.onload = (e) => {
      spark.append(e.target.result)
      currentChunk++
      if (currentChunk < chunks) {
        loadNext()
      } else {
        resolve(spark.end())
      }
    }

    fileReader.onerror = () => reject(new Error('Failed to read file for MD5 computation'))

    function loadNext() {
      const start = currentChunk * chunkSize
      const end = Math.min(start + chunkSize, file.size)
      fileReader.readAsArrayBuffer(blobSlice.call(file, start, end))
    }

    loadNext()
  })
}

export function useOssUpload() {
  const uploadStore = useUploadStore()

  async function uploadFile(file, published = false) {
    const uid = file.uid || Date.now() + '-' + Math.random().toString(36).slice(2)
    uploadStore.addFile(uid, file)

    try {
      const md5 = await computeMd5(file)

      const sigRes = await getUploadSignature(file.name, file.type || 'application/octet-stream', md5)
      const data = sigRes.data.data

      if (data.exists) {
        uploadStore.markSuccess(uid, data.accessUrl)
        return data.accessUrl
      }

      const { ossKey, uploadUrl, accessUrl } = data

      await new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest()
        xhr.open('PUT', uploadUrl)
        xhr.setRequestHeader('Content-Type', file.type || 'application/octet-stream')

        xhr.upload.onprogress = (e) => {
          if (e.lengthComputable) {
            uploadStore.updateProgress(uid, Math.round((e.loaded / e.total) * 100))
          }
        }

        xhr.onload = () => {
          if (xhr.status >= 200 && xhr.status < 300) {
            resolve()
          } else {
            reject(new Error(`Upload failed with status ${xhr.status}`))
          }
        }

        xhr.onerror = () => reject(new Error('Network error during upload'))
        xhr.send(file)
      })

      await saveImageMetadata({
        filename: file.name,
        ossKey,
        ossUrl: accessUrl,
        contentType: file.type || 'application/octet-stream',
        md5,
        fileSize: file.size,
        width: null,
        height: null,
        published
      })

      uploadStore.markSuccess(uid, accessUrl)
      return accessUrl
    } catch (err) {
      const msg = err.response?.data?.msg || err.message || 'Upload failed'
      uploadStore.markError(uid, msg)
      throw err
    }
  }

  async function uploadFiles(files, published = false) {
    const results = []
    for (const file of files) {
      try {
        const url = await uploadFile(file, published)
        results.push({ file, success: true, url })
      } catch (err) {
        results.push({ file, success: false, error: err.message })
      }
    }
    return results
  }

  return { uploadFile, uploadFiles }
}
