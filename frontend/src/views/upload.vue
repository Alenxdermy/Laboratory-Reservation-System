<template>
  <div class="sheet-content">
    <div class="title-row">
      <div>
        <h1>文件上传</h1>
        <p class="title-sub">实验报告与设备图片保存在服务器磁盘，单文件不超过 50MB</p>
      </div>
      <div class="title-actions">
        <el-select v-model="fileType" style="width: 150px">
          <el-option label="实验报告" value="report" />
          <el-option label="设备图片" value="image" />
          <el-option label="其他资料" value="other" />
        </el-select>
      </div>
    </div>

    <div class="panel">
      <el-upload
        drag
        multiple
        :show-file-list="false"
        :http-request="handleUpload"
        :before-upload="beforeUpload"
        accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.zip,.jpg,.jpeg,.png,.gif,.webp"
      >
        <div class="upload-body">
          <span class="upload-mark">
            <AppIcon name="file" :size="22" />
          </span>
          <p class="upload-title">拖入文件，或点击选择</p>
          <p class="upload-sub">支持 PDF、Word、Excel、PPT、图片与 ZIP</p>
        </div>
      </el-upload>
    </div>

    <div class="panel" v-if="uploadedFiles.length">
      <div class="panel-head">
        <h2>本次已上传</h2>
        <span class="panel-meta">{{ uploadedFiles.length }} 个文件</span>
      </div>
      <table class="data-table">
        <thead>
          <tr>
            <th>文件名</th>
            <th class="type-col">类型</th>
            <th class="num size-col">大小</th>
            <th class="action-col">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in uploadedFiles" :key="row.storedName">
            <td>{{ row.name }}</td>
            <td class="type-col">{{ typeText(row.type) }}</td>
            <td class="num size-col">{{ formatSize(row.size) }}</td>
            <td class="action-col">
              <button class="text-action" @click="openFile(row)">查看</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import uploadApi from '@/api/upload'
import AppIcon from '@/components/AppIcon.vue'

const fileType = ref('report')
const uploadedFiles = ref([])

function typeText(type) {
  return { report: '实验报告', image: '设备图片', other: '其他资料' }[type] || type
}

function formatSize(size) {
  if (!size) return '0 B'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(2)} MB`
}

function beforeUpload(file) {
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.error('单个文件不能超过 50MB')
    return false
  }
  return true
}

async function handleUpload(options) {
  const formData = new FormData()
  formData.append('file', options.file)
  formData.append('type', fileType.value)
  try {
    const res = await uploadApi.upload(formData)
    uploadedFiles.value.unshift(res.data)
    ElMessage.success('上传成功')
    options.onSuccess && options.onSuccess(res.data)
  } catch (e) {
    options.onError && options.onError(e)
  }
}

function openFile(row) {
  window.open(row.url, '_blank')
}
</script>

<style scoped>
.upload-body {
  padding: 28px 16px;
  text-align: center;
}

.upload-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--active);
  color: var(--muted);
  margin-bottom: 12px;
}

.upload-title {
  margin: 0;
  font-size: var(--fs-body);
  color: var(--text);
}

.upload-sub {
  margin: 6px 0 0;
  font-size: var(--fs-small);
  color: var(--muted);
}

.type-col {
  width: 120px;
}

.size-col {
  width: 110px;
}

.action-col {
  width: 80px;
  text-align: right;
}
</style>
