import { getUploadSignature, saveImageMetadata } from '@/api'
import { useUploadStore } from '@/stores/upload'

export function useOssUpload() {
  const uploadStore = useUploadStore()

  async function uploadFile(file) {
    const uid = file.uid || Date.now() + '-' + Math.random().toString(36).slice(2)
    uploadStore.addFile(uid, file)

    try {
      const sigRes = await getUploadSignature(file.name, file.type || 'application/octet-stream')
      const { ossKey, uploadUrl, accessUrl } = sigRes.data.data

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
        fileSize: file.size,
        width: null,
        height: null
      })

      uploadStore.markSuccess(uid, accessUrl)
      return accessUrl
    } catch (err) {
      const msg = err.response?.data?.msg || err.message || 'Upload failed'
      uploadStore.markError(uid, msg)
      throw err
    }
  }

  async function uploadFiles(files) {
    const results = []
    for (const file of files) {
      try {
        const url = await uploadFile(file)
        results.push({ file, success: true, url })
      } catch (err) {
        results.push({ file, success: false, error: err.message })
      }
    }
    return results
  }

  return { uploadFile, uploadFiles }
}
