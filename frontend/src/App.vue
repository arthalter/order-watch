<template>
  <main class="legal-shell">
    <aside class="rail">
      <div class="brand">
        <div class="brand-mark">LW</div>
        <div>
          <strong>LegalWatch</strong>
          <span>Mini RAG Console</span>
        </div>
      </div>

      <nav class="rail-nav" aria-label="LegalWatch sections">
        <a href="#status">
          <el-icon><Monitor /></el-icon>
          状态
        </a>
        <a href="#knowledge">
          <el-icon><Files /></el-icon>
          知识库
        </a>
        <a href="#search">
          <el-icon><Search /></el-icon>
          检索
        </a>
        <a href="#chat">
          <el-icon><ChatLineRound /></el-icon>
          问答
        </a>
      </nav>

      <div class="rail-note">
        <span>后端代理</span>
        <strong>localhost:8080</strong>
      </div>
    </aside>

    <section class="desk">
      <header class="hero">
        <div>
          <p class="eyebrow">法律 SOP / Milvus / 文档问答</p>
          <h1>法律知识库 RAG 工作台</h1>
          <p class="hero-copy">
            用一个页面完成健康检查、collection 初始化、Markdown 入库、SOP 检索和法律问答。
          </p>
        </div>
        <div class="hero-actions">
          <el-button :icon="Refresh" :loading="statusLoading" @click="refreshStatus">
            刷新状态
          </el-button>
          <el-button type="primary" :icon="Cpu" :loading="indexing" @click="indexLocalDocs">
            索引本地文档
          </el-button>
        </div>
      </header>

      <section id="status" class="status-grid">
        <article class="status-card">
          <div class="status-top">
            <span>Spring Boot</span>
            <el-tag :type="health.ok ? 'success' : 'danger'" effect="dark">
              {{ health.ok ? '在线' : '离线' }}
            </el-tag>
          </div>
          <strong>{{ health.text }}</strong>
          <small>{{ health.detail }}</small>
        </article>

        <article class="status-card">
          <div class="status-top">
            <span>Milvus</span>
            <el-tag :type="milvus.connected ? 'success' : 'warning'" effect="dark">
              {{ milvus.connected ? '可连接' : '待检查' }}
            </el-tag>
          </div>
          <strong>{{ milvus.host }}:{{ milvus.port }}</strong>
          <small>{{ milvus.latencyMs }} ms</small>
        </article>

        <article class="status-card">
          <div class="status-top">
            <span>Collection</span>
            <el-tag :type="collection.exists ? 'success' : 'info'" effect="dark">
              {{ collection.exists ? '已存在' : '未创建' }}
            </el-tag>
          </div>
          <strong>legal_sop_chunks</strong>
          <small>{{ collection.message }}</small>
        </article>

        <article class="status-card">
          <div class="status-top">
            <span>最近操作</span>
            <el-tag type="info" effect="plain">{{ lastAction.level }}</el-tag>
          </div>
          <strong>{{ lastAction.title }}</strong>
          <small>{{ lastAction.detail }}</small>
        </article>
      </section>

      <section class="work-grid">
        <article id="knowledge" class="panel knowledge-panel">
          <div class="panel-head">
            <div>
              <p class="section-label">Knowledge Base</p>
              <h2>文档入库</h2>
            </div>
            <el-button :icon="Box" :loading="initializing" @click="initCollection">
              初始化 Collection
            </el-button>
          </div>

          <div class="upload-zone" @dragover.prevent @drop.prevent="handleDrop">
            <input
              ref="fileInput"
              type="file"
              accept=".md,.markdown,text/markdown,text/plain"
              class="file-input"
              @change="handleFileSelect"
            />
            <el-icon><UploadFilled /></el-icon>
            <strong>{{ selectedFileName || '拖入 Markdown，或选择文件上传' }}</strong>
            <span>上传后会立即调用 `/api/documents/upload` 并写入 Milvus。</span>
            <div class="upload-actions">
              <el-button :icon="FolderOpened" @click="openFileDialog">选择文件</el-button>
              <el-button
                type="primary"
                :icon="Upload"
                :disabled="!selectedFile"
                :loading="uploading"
                @click="uploadDocument"
              >
                上传并入库
              </el-button>
            </div>
          </div>

          <div class="result-box">
            <span>入库结果</span>
            <pre>{{ indexResult }}</pre>
          </div>
        </article>

        <article id="search" class="panel search-panel">
          <div class="panel-head">
            <div>
              <p class="section-label">Retrieval</p>
              <h2>SOP 检索</h2>
            </div>
            <el-input-number v-model="topK" :min="1" :max="10" controls-position="right" />
          </div>

          <div class="query-row">
            <el-input
              v-model="searchQuery"
              placeholder="例如：保证责任、正式法律意见、劳动争议证据"
              @keyup.enter="searchSop"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" :icon="Search" :loading="searching" @click="searchSop">
              检索
            </el-button>
          </div>

          <div class="results-list">
            <article v-for="item in searchResults" :key="`${item.fileName}-${item.chunkIndex}`" class="result-item">
              <div class="result-meta">
                <strong>{{ item.title || item.fileName }}</strong>
                <span>{{ item.fileName }} · chunk {{ item.chunkIndex }} · {{ formatScore(item.score) }}</span>
              </div>
              <p>{{ item.content }}</p>
            </article>
            <el-empty v-if="!searchResults.length" description="暂无检索结果" />
          </div>
        </article>
      </section>

      <section id="chat" class="panel chat-panel">
        <div class="panel-head">
          <div>
            <p class="section-label">Legal Agent</p>
            <h2>法律文档问答</h2>
          </div>
          <el-tag :type="chatState.success ? 'success' : 'info'" effect="plain">
            {{ chatState.success ? '已生成' : '等待问题' }}
          </el-tag>
        </div>

        <div class="chat-layout">
          <div class="question-stack">
            <button
              v-for="sample in sampleQuestions"
              :key="sample"
              class="sample-question"
              type="button"
              @click="question = sample"
            >
              {{ sample }}
            </button>
          </div>

          <div class="answer-card">
            <div class="answer-body">{{ chatAnswer }}</div>
            <div class="chat-input-row">
              <el-input
                v-model="question"
                type="textarea"
                :autosize="{ minRows: 2, maxRows: 4 }"
                placeholder="输入一个需要结合已入库 SOP 回答的问题"
                @keydown.enter.exact.prevent="askLegalAgent"
              />
              <el-button type="primary" :icon="Position" :loading="chatLoading" @click="askLegalAgent">
                发送
              </el-button>
            </div>
          </div>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  Box,
  ChatLineRound,
  Cpu,
  Files,
  FolderOpened,
  Monitor,
  Position,
  Refresh,
  Search,
  Upload,
  UploadFilled
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const statusLoading = ref(false)
const initializing = ref(false)
const indexing = ref(false)
const uploading = ref(false)
const searching = ref(false)
const chatLoading = ref(false)

const health = ref({ ok: false, text: '未检查', detail: '点击刷新状态检查后端健康。' })
const milvus = ref({ connected: false, host: 'localhost', port: 19530, latencyMs: '-' })
const collection = ref({ exists: false, message: '点击初始化 collection 后可写入文档。' })
const lastAction = ref({ level: 'idle', title: '尚未操作', detail: '等待连接后端。' })

const fileInput = ref(null)
const selectedFile = ref(null)
const searchQuery = ref('正式法律意见的答复边界')
const topK = ref(3)
const searchResults = ref([])
const question = ref('正式法律意见的答复边界是什么？')
const chatState = ref({ success: false })
const chatAnswer = ref('先完成文档入库，然后向 Legal Agent 提问。回答会基于 Milvus 召回的 SOP 片段生成。')
const indexResult = ref('尚未执行入库操作。')

const sampleQuestions = [
  '正式法律意见的答复边界是什么？',
  '劳动争议证据应该如何整理？',
  '保证责任怎么查询？'
]

const selectedFileName = computed(() => selectedFile.value?.name || '')

onMounted(() => {
  refreshStatus()
})

async function requestJson(url, options = {}) {
  const response = await fetch(url, options)
  const data = await response.json().catch(() => ({}))
  if (!response.ok || data.success === false) {
    throw new Error(data.message || `HTTP ${response.status}`)
  }
  return data
}

async function refreshStatus() {
  statusLoading.value = true
  try {
    const [healthData, milvusData, collectionData] = await Promise.all([
      requestJson('/health'),
      requestJson('/health/milvus'),
      requestJson('/api/sop/collection')
    ])

    health.value = {
      ok: true,
      text: healthData.data || 'ok',
      detail: '后端健康检查通过。'
    }
    milvus.value = milvusData.data || milvus.value
    collection.value = {
      exists: Boolean(collectionData.data?.exists),
      message: collectionData.data?.exists ? 'Collection 可用于写入和检索。' : 'Collection 尚未创建。'
    }
    setLastAction('ok', '状态已刷新', '后端、Milvus 与 collection 状态已同步。')
  } catch (error) {
    health.value = { ok: false, text: '不可用', detail: error.message }
    setLastAction('error', '状态检查失败', error.message)
    ElMessage.warning('后端暂不可用，请确认 Spring Boot 已启动。')
  } finally {
    statusLoading.value = false
  }
}

async function initCollection() {
  initializing.value = true
  try {
    const data = await requestJson('/api/sop/collection/init', { method: 'POST' })
    collection.value = { exists: true, message: 'Collection 初始化完成。' }
    setLastAction('ok', 'Collection 已初始化', JSON.stringify(data.data, null, 2))
    ElMessage.success('Collection 初始化完成')
  } catch (error) {
    setLastAction('error', '初始化失败', error.message)
    ElMessage.error(error.message)
  } finally {
    initializing.value = false
  }
}

async function indexLocalDocs() {
  indexing.value = true
  try {
    const data = await requestJson('/api/sop/index-local-docs', { method: 'POST' })
    indexResult.value = JSON.stringify(data.data, null, 2)
    setLastAction('ok', '本地文档已索引', indexResult.value)
    ElMessage.success('本地 legal-docs 已索引')
  } catch (error) {
    indexResult.value = error.message
    setLastAction('error', '索引失败', error.message)
    ElMessage.error(error.message)
  } finally {
    indexing.value = false
  }
}

function openFileDialog() {
  fileInput.value?.click()
}

function handleFileSelect(event) {
  selectedFile.value = event.target.files?.[0] || null
}

function handleDrop(event) {
  selectedFile.value = event.dataTransfer.files?.[0] || null
}

async function uploadDocument() {
  if (!selectedFile.value) return

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    const data = await requestJson('/api/documents/upload', {
      method: 'POST',
      body: formData
    })
    indexResult.value = JSON.stringify(data.data, null, 2)
    setLastAction('ok', '文档上传完成', selectedFile.value.name)
    ElMessage.success('文档已上传并入库')
  } catch (error) {
    indexResult.value = error.message
    setLastAction('error', '上传失败', error.message)
    ElMessage.error(error.message)
  } finally {
    uploading.value = false
  }
}

async function searchSop() {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入检索词')
    return
  }

  searching.value = true
  try {
    const params = new URLSearchParams({
      query: searchQuery.value.trim(),
      topK: String(topK.value)
    })
    const data = await requestJson(`/api/sop/search?${params.toString()}`)
    searchResults.value = data.data || []
    setLastAction('ok', '检索完成', `返回 ${searchResults.value.length} 条 SOP chunk。`)
  } catch (error) {
    searchResults.value = []
    setLastAction('error', '检索失败', error.message)
    ElMessage.error(error.message)
  } finally {
    searching.value = false
  }
}

async function askLegalAgent() {
  if (!question.value.trim()) {
    ElMessage.warning('请输入问题')
    return
  }

  chatLoading.value = true
  try {
    const data = await requestJson('/api/legal_chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question: question.value.trim() })
    })
    chatState.value = { success: Boolean(data.data?.success) }
    chatAnswer.value = data.data?.answer || data.data?.errorMessage || data.message
    setLastAction('ok', '问答完成', question.value.trim())
  } catch (error) {
    chatState.value = { success: false }
    chatAnswer.value = error.message
    setLastAction('error', '问答失败', error.message)
    ElMessage.error(error.message)
  } finally {
    chatLoading.value = false
  }
}

function formatScore(score) {
  if (typeof score !== 'number') return 'score -'
  return `score ${score.toFixed(4)}`
}

function setLastAction(level, title, detail) {
  lastAction.value = { level, title, detail }
}
</script>
