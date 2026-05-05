export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  skillId?: string
  agentId?: string
  metadata?: Record<string, unknown>
}

export interface Session {
  id: string
  title: string
  createdAt: string
}

export interface SkillDefinition {
  id: string
  name: string
  description: string
  inputSchema: Record<string, string>
  tags: string[]
}

const API_BASE = '/api/v1'

export const chatService = {
  async createSession(): Promise<Session> {
    const res = await fetch(`${API_BASE}/chat/sessions`, { method: 'POST' })
    if (!res.ok) throw new Error('Failed to create session')
    return res.json()
  },

  async sendMessage(sessionId: string, content: string): Promise<Message> {
    const res = await fetch(`${API_BASE}/chat/sessions/${sessionId}/messages`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content }),
    })
    if (!res.ok) throw new Error('Failed to send message')
    return res.json()
  },

  async streamMessage(
    sessionId: string,
    content: string,
    onChunk: (chunk: string) => void
  ): Promise<void> {
    const res = await fetch(
      `${API_BASE}/chat/sessions/${sessionId}/stream?message=${encodeURIComponent(content)}`
    )
    if (!res.ok) throw new Error('Failed to stream message')

    const reader = res.body?.getReader()
    if (!reader) throw new Error('No reader available')

    const decoder = new TextDecoder()
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      const text = decoder.decode(value, { stream: true })
      const lines = text.split('\n')
      for (const line of lines) {
        if (line.startsWith('data:')) {
          onChunk(line.slice(5))
        } else if (line.trim() && !line.startsWith(':')) {
          onChunk(line)
        }
      }
    }
  },

  async getSkills(): Promise<SkillDefinition[]> {
    const res = await fetch(`${API_BASE}/skills`)
    if (!res.ok) throw new Error('Failed to fetch skills')
    return res.json()
  },

  async executeSkill(skillId: string, input: Record<string, unknown>): Promise<{ content: string; success: boolean }> {
    const res = await fetch(`${API_BASE}/skills/${skillId}/execute`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(input),
    })
    if (!res.ok) throw new Error('Failed to execute skill')
    return res.json()
  },

  async getMcpTools(): Promise<{ name: string; description: string }[]> {
    const res = await fetch(`${API_BASE}/mcp/tools`)
    if (!res.ok) throw new Error('Failed to fetch MCP tools')
    return res.json()
  },

  async ragIndex(path: string): Promise<{ documentsIndexed: number; chunksCreated: number }> {
    const res = await fetch(`${API_BASE}/rag/index`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ path }),
    })
    if (!res.ok) throw new Error('Failed to index')
    return res.json()
  },

  async ragSearch(query: string, limit = 10) {
    const res = await fetch(`${API_BASE}/rag/search`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query, limit }),
    })
    if (!res.ok) throw new Error('Failed to search')
    return res.json()
  },

  async getMemoryStats() {
    const res = await fetch(`${API_BASE}/memory/stats`)
    if (!res.ok) throw new Error('Failed to fetch memory stats')
    return res.json()
  },

  async listMemories(type?: string | null, limit = 50) {
    const params = new URLSearchParams({ limit: String(limit) })
    if (type) params.set('type', type)
    const res = await fetch(`${API_BASE}/memory/list?${params}`)
    if (!res.ok) throw new Error('Failed to list memories')
    return res.json()
  },

  async searchMemories(query: string, type?: string | null, limit = 20) {
    const body: Record<string, unknown> = { query, limit }
    if (type) body.type = type
    const res = await fetch(`${API_BASE}/memory/search`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
    if (!res.ok) throw new Error('Failed to search memories')
    return res.json()
  },

  async storeMemory(type: string, content: string, extra?: Record<string, unknown>) {
    const res = await fetch(`${API_BASE}/memory/store`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ type, content, ...extra }),
    })
    if (!res.ok) throw new Error('Failed to store memory')
    return res.json()
  },

  async deleteMemory(type: string, id: string) {
    const res = await fetch(`${API_BASE}/memory/${type}/${id}`, { method: 'DELETE' })
    if (!res.ok) throw new Error('Failed to delete memory')
    return res.json()
  },

  async consolidateMemories(messages: string[]) {
    const res = await fetch(`${API_BASE}/memory/consolidate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ messages }),
    })
    if (!res.ok) throw new Error('Failed to consolidate')
    return res.json()
  },

  async buildContext(query: string, history?: string[]) {
    const body: Record<string, unknown> = { query }
    if (history) body.history = history
    const res = await fetch(`${API_BASE}/context/build`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
    if (!res.ok) throw new Error('Failed to build context')
    return res.json()
  },

  async getContextBudget() {
    const res = await fetch(`${API_BASE}/context/budget`)
    if (!res.ok) throw new Error('Failed to get budget')
    return res.json()
  },

  async updateContextBudget(totalBudget: number) {
    const res = await fetch(`${API_BASE}/context/budget`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ totalBudget }),
    })
    if (!res.ok) throw new Error('Failed to update budget')
    return res.json()
  },

  async getAgents() {
    const res = await fetch(`${API_BASE}/agents`)
    if (!res.ok) throw new Error('Failed to fetch agents')
    return res.json()
  },

  async executeAgent(agentRole: string, content: string) {
    const res = await fetch(`${API_BASE}/agents/execute`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ agentRole, content }),
    })
    if (!res.ok) throw new Error('Failed to execute agent')
    return res.json()
  },

  async orchestrateAgents(content: string) {
    const res = await fetch(`${API_BASE}/agents/orchestrate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content }),
    })
    if (!res.ok) throw new Error('Failed to orchestrate')
    return res.json()
  },

  async getHarnessHooks() {
    const res = await fetch(`${API_BASE}/harness/hooks`)
    if (!res.ok) throw new Error('Failed to fetch hooks')
    return res.json()
  },

  async getHarnessProfiles() {
    const res = await fetch(`${API_BASE}/harness/profiles`)
    if (!res.ok) throw new Error('Failed to fetch profiles')
    return res.json()
  },

  async activateProfile(name: string) {
    const res = await fetch(`${API_BASE}/harness/profiles/${name}/activate`, { method: 'POST' })
    if (!res.ok) throw new Error('Failed to activate profile')
    return res.json()
  },

  async getAuditLogs(page = 0, size = 20) {
    const res = await fetch(`${API_BASE}/harness/audit?page=${page}&size=${size}`)
    if (!res.ok) throw new Error('Failed to fetch audit logs')
    return res.json()
  },
}
