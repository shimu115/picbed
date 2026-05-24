import SparkMD5 from 'spark-md5'
import { getUploadSignature, saveImageMetadata } from '@/api'
import { useUploadStore } from '@/stores/upload'
import i18n from '@/i18n'
import { ElMessageBox } from 'element-plus'

const MAX_FILE_SIZE = 50 * 1024 * 1024

function validateFilename(name, maxLen = 64) {
  const dotIndex = name.lastIndexOf('.')
  const base = dotIndex > 0 ? name.substring(0, dotIndex) : name
  if (base.length > maxLen) {
    throw new Error(i18n.global.t('upload.filenameTooLong', { max: maxLen }))
  }
}

function compressImage(file) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    const url = URL.createObjectURL(file)
    img.onload = () => {
      URL.revokeObjectURL(url)
      let { naturalWidth: w, naturalHeight: h } = img
      const maxDim = 4096
      if (w > maxDim || h > maxDim) {
        const ratio = Math.min(maxDim / w, maxDim / h)
        w = Math.round(w * ratio)
        h = Math.round(h * ratio)
      }
      const canvas = document.createElement('canvas')
      canvas.width = w
      canvas.height = h
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, w, h)
      const mimeType = file.type === 'image/jpeg' || file.type === 'image/webp'
        ? file.type : 'image/jpeg'
      canvas.toBlob((blob) => {
        if (blob) {
          resolve(new File([blob], file.name, { type: mimeType, lastModified: Date.now() }))
        } else {
          reject(new Error(i18n.global.t('upload.compressFailed')))
        }
      }, mimeType, 0.7)
    }
    img.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error(i18n.global.t('upload.compressFailed')))
    }
    img.src = url
  })
}

function getImageDimensions(file) {
  return new Promise((resolve) => {
    if (!file.type.startsWith('image/')) {
      resolve({ width: null, height: null })
      return
    }
    const url = URL.createObjectURL(file)
    const img = new Image()
    img.onload = () => {
      URL.revokeObjectURL(url)
      resolve({ width: img.naturalWidth, height: img.naturalHeight })
    }
    img.onerror = () => {
      URL.revokeObjectURL(url)
      resolve({ width: null, height: null })
    }
    img.src = url
  })
}

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
      let uploadFileObj = file

      if (file.size > MAX_FILE_SIZE) {
        try {
          await ElMessageBox.confirm(
            i18n.global.t('upload.fileTooLarge', { max: 50 }),
            i18n.global.t('common.confirm'),
            { type: 'warning', confirmButtonText: i18n.global.t('common.yes'), cancelButtonText: i18n.global.t('common.no') }
          )
          uploadFileObj = await compressImage(file)
          uploadStore.updateFile(uid, uploadFileObj)
        } catch {
          throw new Error(i18n.global.t('upload.fileTooLargeRejected', { max: 50 }))
        }
      }

      validateFilename(uploadFileObj.name)

      const [md5, dims] = await Promise.all([computeMd5(uploadFileObj), getImageDimensions(uploadFileObj)])

      const sigRes = await getUploadSignature(uploadFileObj.name, uploadFileObj.type || 'application/octet-stream', md5)
      const data = sigRes.data.data

      if (data.exists) {
        uploadStore.markSuccess(uid, data.accessUrl)
        return data.accessUrl
      }

      const { ossKey, uploadUrl, accessUrl } = data

      await new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest()
        xhr.open('PUT', uploadUrl)
        xhr.setRequestHeader('Content-Type', uploadFileObj.type || 'application/octet-stream')

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
        xhr.send(uploadFileObj)
      })

      await saveImageMetadata({
        filename: uploadFileObj.name,
        ossKey,
        ossUrl: accessUrl,
        contentType: uploadFileObj.type || 'application/octet-stream',
        md5,
        fileSize: uploadFileObj.size,
        width: dims.width,
        height: dims.height,
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
