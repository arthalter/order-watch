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
            <h2>连续法律研读</h2>
            <p class="panel-caption">带着来源追问，让上下文成为可验证的检索线索。</p>
          </div>
          <div class="conversation-actions">
            <span class="memory-pill" :class="{ active: rememberedTurns.length }">{{ memoryLabel }}</span>
            <el-button size="small" @click="startNewConversation">新对话</el-button>
            <el-tag :type="chatState.success ? 'success' : 'info'" effect="plain">
              {{ chatLoading ? '生成中' : chatState.success ? '已生成' : '等待问题' }}
            </el-tag>
          </div>
        </div>

        <div class="chat-layout">
          <div class="question-stack">
            <div class="context-card" :class="{ active: rememberedTurns.length }">
              <span class="context-label">Conversation Context</span>
              <strong>{{ rememberedTurns.length ? '上下文记忆已启用' : '从一个主题开始' }}</strong>
              <p>{{ contextDescription }}</p>
              <span class="session-token">会话：{{ sessionLabel }}</span>
              <ol v-if="retainedTurns.length" class="context-turns">
                <li v-for="turn in retainedTurns" :key="turn.id">{{ turn.question }}</li>
              </ol>
            </div>

            <p class="prompt-label">开始话题</p>
            <button
              v-for="sample in startingQuestions"
              :key="sample"
              class="sample-question"
              type="button"
              @click="usePrompt(sample)"
            >
              {{ sample }}
            </button>

            <p class="prompt-label follow-up-label">结合上文追问</p>
            <button
              v-for="sample in followUpQuestions"
              :key="sample"
              class="sample-question follow-up-question"
              type="button"
              :disabled="!rememberedTurns.length"
              @click="usePrompt(sample)"
            >
              {{ sample }}
            </button>
          </div>

          <div class="conversation-stage">
            <div ref="conversationThread" class="conversation-thread" aria-live="polite">
              <div v-if="!conversationTurns.length" class="conversation-empty">
                <strong>问一个基于文档的问题</strong>
                <p>首次回答会创建会话；继续追问时，后端会结合该会话的近期问答摘要理解指代与上下文。</p>
              </div>

              <template v-for="turn in conversationTurns" :key="turn.id">
                <article class="message user-message">
                  <div class="message-meta">
                    <strong>你</strong>
                    <span v-if="turn.usesContext">基于上文追问</span>
                  </div>
                  <p>{{ turn.question }}</p>
                </article>
                <article class="message agent-message" :class="turn.status">
                  <div class="message-meta">
                    <strong>Legal Agent</strong>
                    <span>{{ turn.status === 'loading' ? '检索与生成中' : turn.status === 'error' ? '回答失败' : '基于文档回答' }}</span>
                  </div>
                  <div v-if="turn.status === 'loading'" class="answer-loading">
                    <span></span><span></span><span></span>
                    正在检索知识库并组织引用
                  </div>
                  <div v-else class="agent-content">{{ turn.answer }}</div>
                </article>
              </template>
            </div>

            <div class="chat-composer">
              <el-input
                v-model="question"
                type="textarea"
                :autosize="{ minRows: 2, maxRows: 4 }"
                placeholder="输入问题；在已有会话中可使用“其中”“第二条”等追问表达"
                @keydown.enter.exact.prevent="askLegalAgent"
              />
              <div class="composer-actions">
                <small>{{ rememberedTurns.length ? '本次提问将沿用当前上下文' : '本次提问将创建新会话' }}</small>
                <el-button type="primary" :icon="Position" :loading="chatLoading" @click="askLegalAgent">
                  发送
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
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
const conversationId = ref('')
const conversationTurns = ref([])
const conversationThread = ref(null)
const chatState = ref({ success: false })
const indexResult = ref('尚未执行入库操作。')
let nextTurnId = 1

const startingQuestions = [
  '正式法律意见的答复边界是什么？',
  '劳动争议证据应该如何整理？',
  '保证责任怎么查询？'
]
const followUpQuestions = [
  '其中第二条需要准备哪些材料？',
  '上一个回答的依据来源是什么？',
  '这个结论有哪些使用边界？'
]

const selectedFileName = computed(() => selectedFile.value?.name || '')
const rememberedTurns = computed(() => conversationTurns.value.filter((turn) => turn.status === 'success'))
const retainedTurns = computed(() => rememberedTurns.value.slice(-5))
const memoryLabel = computed(() => rememberedTurns.value.length
  ? `已记忆 ${retainedTurns.value.length} / 5 轮`
  : '新会话')
const sessionLabel = computed(() => conversationId.value
  ? `${conversationId.value.slice(0, 18)}...`
  : '首次回答后生成')
const contextDescription = computed(() => {
  if (!rememberedTurns.value.length) {
    return '发送首个问题后，系统将保存本会话的近期问答摘要，用于理解后续追问。'
  }
  if (rememberedTurns.value.length > 5) {
    return '页面保留完整交流记录，后端将在下一次回答时参考最近 5 轮问答摘要。'
  }
  return `下一次回答将参考当前会话已完成的 ${rememberedTurns.value.length} 轮问答摘要。`
})

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
  if (chatLoading.value) return
  if (!question.value.trim()) {
    ElMessage.warning('请输入问题')
    return
  }

  const submittedQuestion = question.value.trim()
  const turn = {
    id: nextTurnId++,
    question: submittedQuestion,
    answer: '',
    status: 'loading',
    usesContext: rememberedTurns.value.length > 0 && Boolean(conversationId.value)
  }
  conversationTurns.value.push(turn)
  question.value = ''
  chatLoading.value = true
  chatState.value = { success: false }
  await scrollConversationToEnd()
  try {
    const data = await requestJson('/api/legal_chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        conversationId: conversationId.value || null,
        question: submittedQuestion
      })
    })
    if (data.data?.success === false) {
      throw new Error(data.data.errorMessage || '问答失败')
    }
    conversationId.value = data.data?.conversationId || conversationId.value
    turn.answer = data.data?.answer || '模型未返回回答内容。'
    turn.status = 'success'
    chatState.value = { success: true }
    setLastAction('ok', '问答完成', submittedQuestion)
  } catch (error) {
    turn.answer = error.message
    turn.status = 'error'
    chatState.value = { success: false }
    setLastAction('error', '问答失败', error.message)
    ElMessage.error(error.message)
  } finally {
    chatLoading.value = false
    await scrollConversationToEnd()
  }
}

function startNewConversation() {
  conversationId.value = ''
  conversationTurns.value = []
  question.value = startingQuestions[0]
  chatState.value = { success: false }
  setLastAction('idle', '已开始新会话', '下一次回答不会引用上一段会话内容。')
  ElMessage.info('已开始新会话')
}

function usePrompt(prompt) {
  question.value = prompt
}

async function scrollConversationToEnd() {
  await nextTick()
  if (conversationThread.value) {
    conversationThread.value.scrollTop = conversationThread.value.scrollHeight
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
