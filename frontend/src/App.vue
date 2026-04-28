<template>
  <el-container class="app-shell">
    <el-aside width="248px" class="sidebar">
      <div class="brand">
        <div class="brand-mark">OW</div>
        <div>
          <div class="brand-name">OrderWatch</div>
          <div class="brand-subtitle">异常订单监控 Agent</div>
        </div>
      </div>

      <el-menu class="nav-menu" default-active="monitor">
        <el-menu-item index="monitor">
          <el-icon><Warning /></el-icon>
          <span>异常监控</span>
        </el-menu-item>
        <el-menu-item index="review">
          <el-icon><DocumentChecked /></el-icon>
          <span>人工审核</span>
        </el-menu-item>
        <el-menu-item index="sop">
          <el-icon><Reading /></el-icon>
          <span>SOP 知识库</span>
        </el-menu-item>
        <el-menu-item index="report">
          <el-icon><DataAnalysis /></el-icon>
          <span>监控报告</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-status">
        <div class="status-line">
          <span>Milvus RAG</span>
          <strong>在线</strong>
        </div>
        <div class="status-line">
          <span>Mock Tools</span>
          <strong>4 个</strong>
        </div>
      </div>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div>
          <div class="page-kicker">运营风控工作台</div>
          <h1>异常订单处置中心</h1>
        </div>
        <div class="topbar-actions">
          <el-input
            v-model="search"
            class="search-input"
            placeholder="搜索订单号、用户、地址"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button :icon="Refresh" @click="refreshData">刷新</el-button>
          <el-button type="primary" :icon="Cpu" :loading="reportLoading" @click="generateReport">
            一键生成报告
          </el-button>
        </div>
      </el-header>

      <el-main class="main">
        <section class="metric-strip">
          <div v-for="metric in metrics" :key="metric.label" class="metric-tile">
            <div class="metric-label">{{ metric.label }}</div>
            <div class="metric-value">{{ metric.value }}</div>
            <div class="metric-meta" :class="metric.tone">{{ metric.meta }}</div>
          </div>
        </section>

        <section class="workspace">
          <div class="panel order-panel">
            <div class="panel-head">
              <div>
                <h2>异常订单清单</h2>
                <p>按风险等级与可解释证据排序，优先处理高危订单。</p>
              </div>
              <el-segmented v-model="riskFilter" :options="riskOptions" />
            </div>

            <el-table
              :data="filteredOrders"
              height="440"
              class="risk-table"
              row-key="id"
              :row-class-name="riskRowClass"
            >
              <el-table-column label="订单" min-width="190">
                <template #default="{ row }">
                  <button class="order-link" @click="selectedOrderId = row.id">{{ row.id }}</button>
                  <div class="muted">{{ row.user }}</div>
                </template>
              </el-table-column>
              <el-table-column label="风险" width="118">
                <template #default="{ row }">
                  <el-tag :type="riskTagType(row.risk)" effect="dark" round>
                    {{ riskText(row.risk) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="type" label="异常类型" min-width="160" />
              <el-table-column label="金额" width="118" align="right">
                <template #default="{ row }">¥{{ row.amount.toLocaleString() }}</template>
              </el-table-column>
              <el-table-column prop="evidence" label="关键证据" min-width="260" show-overflow-tooltip />
              <el-table-column label="状态" width="128">
                <template #default="{ row }">
                  <el-tag :type="row.status === '待复核' ? 'warning' : 'info'" effect="plain">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <aside class="panel detail-panel">
            <div class="panel-head compact">
              <div>
                <h2>AI 分析</h2>
                <p>{{ selectedOrder.id }}</p>
              </div>
              <el-tag :type="riskTagType(selectedOrder.risk)" effect="dark" round>
                {{ riskText(selectedOrder.risk) }}
              </el-tag>
            </div>

            <div class="risk-score">
              <div>
                <span>风险评分</span>
                <strong>{{ selectedOrder.score }}</strong>
              </div>
              <el-progress
                :percentage="selectedOrder.score"
                :stroke-width="10"
                :color="progressColor"
                :show-text="false"
              />
            </div>

            <el-timeline class="analysis-timeline">
              <el-timeline-item
                v-for="item in selectedOrder.analysis"
                :key="item.title"
                :type="item.type"
                :timestamp="item.title"
              >
                {{ item.detail }}
              </el-timeline-item>
            </el-timeline>

            <div class="sop-box">
              <div class="sop-title">
                <el-icon><Connection /></el-icon>
                命中 SOP
              </div>
              <p>{{ selectedOrder.sop }}</p>
            </div>

            <div class="review-actions">
              <el-button type="danger" plain :icon="CircleClose">拦截发货</el-button>
              <el-button type="warning" plain :icon="UserFilled">转人工复核</el-button>
              <el-button type="success" plain :icon="CircleCheck">标记通过</el-button>
            </div>
          </aside>
        </section>

        <section class="lower-grid">
          <div class="panel chat-panel">
            <div class="panel-head compact">
              <div>
                <h2>运营问答</h2>
                <p>通过 Agent 查询异常指标、订单证据与 SOP 规则。</p>
              </div>
            </div>
            <div class="chat-output">{{ chatAnswer }}</div>
            <div class="chat-input-row">
              <el-input
                v-model="question"
                placeholder="例如：为什么 ORDER-20260427-001 被判定为高危？"
                @keyup.enter="askAgent"
              />
              <el-button type="primary" :icon="Position" :loading="chatLoading" @click="askAgent">
                发送
              </el-button>
            </div>
          </div>

          <div class="panel report-panel">
            <div class="panel-head compact">
              <div>
                <h2>异常监控报告</h2>
                <p>报告会汇总异常概览、证据摘要、SOP 与人工确认项。</p>
              </div>
            </div>
            <pre class="report-preview">{{ report }}</pre>
          </div>
        </section>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import {
  CircleCheck,
  CircleClose,
  Connection,
  Cpu,
  DataAnalysis,
  DocumentChecked,
  Position,
  Reading,
  Refresh,
  Search,
  UserFilled,
  Warning
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const search = ref('')
const riskFilter = ref('全部')
const selectedOrderId = ref('ORDER-20260427-001')
const question = ref('最近 24 小时有哪些异常订单？')
const chatLoading = ref(false)
const reportLoading = ref(false)

const riskOptions = ['全部', '高危', '中危', '低危']

const orders = ref([
  {
    id: 'ORDER-20260427-001',
    user: 'USER-20488 / 新客',
    risk: 'high',
    type: '大额订单异常',
    amount: 12999,
    evidence: '金额高于均值 33.4 倍，首次地址，客服催促极速发货',
    status: '待复核',
    score: 92,
    sop: '大额订单需先核对支付状态、收货地址、历史消费水平和客服备注，审核完成前不建议发货。',
    analysis: [
      { type: 'danger', title: '订单金额', detail: '当前订单 ¥12,999，显著高于店铺平均客单价 ¥389。' },
      { type: 'warning', title: '地址画像', detail: '收货地址为首次出现，且与历史常用区域不一致。' },
      { type: 'primary', title: '客服证据', detail: '备注中出现“尽快发货”诉求，符合高危 SOP 中的人工审核触发条件。' }
    ]
  },
  {
    id: 'ORDER-20260427-018',
    user: 'USER-20488 / 老客',
    risk: 'medium',
    type: '频繁取消异常',
    amount: 486,
    evidence: '24 小时内下单 8 次，取消 6 次，命中羊毛党行为片段',
    status: '待复核',
    score: 71,
    sop: '频繁取消应核查近期订单轨迹、优惠券使用情况与支付失败记录，必要时限制营销权益。',
    analysis: [
      { type: 'warning', title: '行为频次', detail: '该用户 24 小时内出现 8 次下单行为，高于同群体基线。' },
      { type: 'warning', title: '取消比例', detail: '取消率达到 75%，疑似试探库存或优惠套利。' },
      { type: 'primary', title: 'SOP 建议', detail: '先暂停自动优惠发放，再由运营确认是否加入观察名单。' }
    ]
  },
  {
    id: 'ORDER-20260427-031',
    user: 'ADDR-MOCK-009 / 5 个账号',
    risk: 'high',
    type: '同地址多账号',
    amount: 2195,
    evidence: '同地址关联 5 个新用户账号，均使用首单券并集中下单',
    status: '待复核',
    score: 88,
    sop: '同地址多账号需要合并查看收货地址、设备指纹、优惠券领取记录和客服工单。',
    analysis: [
      { type: 'danger', title: '地址聚集', detail: '同一收货地址关联 5 个新账号，超过 SOP 高危阈值。' },
      { type: 'warning', title: '权益使用', detail: '关联订单均使用首单券，存在批量套券风险。' },
      { type: 'primary', title: '人工确认', detail: '建议人工核验地址真实性，并暂缓相关订单出库。' }
    ]
  },
  {
    id: 'ORDER-20260427-044',
    user: 'USER-31810 / 复购',
    risk: 'low',
    type: '支付延迟异常',
    amount: 329,
    evidence: '支付回调延迟 14 分钟，未发现地址或账号聚集风险',
    status: '观察中',
    score: 39,
    sop: '支付延迟优先等待支付系统补偿回调，低风险订单无需立即拦截。',
    analysis: [
      { type: 'info', title: '支付链路', detail: '支付回调延迟，但最终支付状态已确认成功。' },
      { type: 'success', title: '用户画像', detail: '复购用户，历史履约正常，无客服投诉记录。' },
      { type: 'primary', title: '处置建议', detail: '保持观察，无需进入强人工审核队列。' }
    ]
  }
])

const chatAnswer = ref('最近 24 小时发现 3 类重点异常：大额订单异常、频繁取消异常、同地址多账号下单异常。建议优先审核高危订单 ORDER-20260427-001 与 ORDER-20260427-031。')
const report = ref('点击“一键生成报告”后，将从后端 /api/order_anomaly_monitor 获取报告；后端未启动时显示本地演示报告。')

const metrics = computed(() => {
  const high = orders.value.filter((item) => item.risk === 'high').length
  const pending = orders.value.filter((item) => item.status === '待复核').length
  return [
    { label: '异常订单', value: orders.value.length, meta: '+18% vs 昨日', tone: 'danger' },
    { label: '高危风险', value: high, meta: '需优先审核', tone: 'danger' },
    { label: '待人工复核', value: pending, meta: '平均等待 12 分钟', tone: 'warning' },
    { label: 'SOP 命中率', value: '96%', meta: 'Milvus TopK=3', tone: 'success' }
  ]
})

const filteredOrders = computed(() => {
  const keyword = search.value.trim().toLowerCase()
  const riskMap = { 高危: 'high', 中危: 'medium', 低危: 'low' }

  return orders.value.filter((order) => {
    const riskMatched = riskFilter.value === '全部' || order.risk === riskMap[riskFilter.value]
    const keywordMatched =
      !keyword ||
      [order.id, order.user, order.type, order.evidence].some((value) =>
        value.toLowerCase().includes(keyword)
      )
    return riskMatched && keywordMatched
  })
})

const selectedOrder = computed(() => {
  return orders.value.find((order) => order.id === selectedOrderId.value) || orders.value[0]
})

const progressColor = computed(() => {
  if (selectedOrder.value.score >= 85) return '#c9332b'
  if (selectedOrder.value.score >= 60) return '#c47b18'
  return '#2f7d5b'
})

function riskText(risk) {
  return { high: '高危', medium: '中危', low: '低危' }[risk]
}

function riskTagType(risk) {
  return { high: 'danger', medium: 'warning', low: 'success' }[risk]
}

function riskRowClass({ row }) {
  return `risk-row-${row.risk}`
}

async function postJson(url, payload = {}) {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
  if (!response.ok) throw new Error(`HTTP ${response.status}`)
  return response.json()
}

async function askAgent() {
  chatLoading.value = true
  try {
    const data = await postJson('/api/ops_chat', { question: question.value })
    chatAnswer.value = data.answer || data.message || JSON.stringify(data, null, 2)
  } catch {
    chatAnswer.value = `演示回答：${question.value}\n\n该问题会调用 Agent Tool 获取订单事实，并结合 Milvus 检索到的 SOP 片段生成解释。当前建议优先处理高危订单，完成支付状态、地址画像、客服备注与优惠券使用记录核验。`
    ElMessage.info('后端未响应，已使用前端演示数据。')
  } finally {
    chatLoading.value = false
  }
}

async function generateReport() {
  reportLoading.value = true
  try {
    const data = await postJson('/api/order_anomaly_monitor')
    report.value = data.report || data.message || JSON.stringify(data, null, 2)
  } catch {
    report.value = `# 异常订单监控报告

## 异常概览
最近 24 小时发现 4 笔异常订单，其中高危 2 笔、中危 1 笔、低危 1 笔。

## 重点清单
- ORDER-20260427-001：大额订单异常，风险评分 92。
- ORDER-20260427-031：同地址多账号下单，风险评分 88。

## 证据摘要
高危订单集中命中金额偏离、首次地址、多账号聚集、客服催促发货等证据。

## 处理建议
暂停高危订单自动发货，转人工审核支付状态、地址真实性、历史消费水平与客服备注。

## 需要人工确认
确认收货地址是否真实、优惠券是否批量套利、是否存在恶意催发货。`
    ElMessage.info('后端未响应，已生成前端演示报告。')
  } finally {
    reportLoading.value = false
  }
}

function refreshData() {
  ElMessage.success('监控数据已刷新')
}
</script>
