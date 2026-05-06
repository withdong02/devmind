import { useState, useEffect } from 'react'
import { Bot, Send, Loader2, ArrowRight } from 'lucide-react'
import { chatService } from '../../services/chatService'

interface Agent {
  id: string
  role: string
  description: string
}

interface AgentResult {
  agentId: string
  content: string
  handoff: boolean
  subtaskCount?: number
  taskDetails?: { task: string; agent: string; status: string; preview?: string }[]
  agentsUsed?: string[]
}

export function AgentsPage() {
  const [agents, setAgents] = useState<Agent[]>([])
  const [selectedAgent, setSelectedAgent] = useState('CODE')
  const [input, setInput] = useState('')
  const [result, setResult] = useState<AgentResult | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [mode, setMode] = useState<'single' | 'orchestrate'>('single')

  useEffect(() => {
    chatService.getAgents().then(setAgents).catch(() => {})
  }, [])

  const handleExecute = async () => {
    if (!input.trim()) return
    setLoading(true)
    setError('')
    setResult(null)
    try {
      const data = mode === 'orchestrate'
        ? await chatService.orchestrateAgents(input)
        : await chatService.executeAgent(selectedAgent, input)
      setResult(data)
    } catch {
      setError('执行失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex flex-col h-full">
      <header className="border-b border-gray-700 px-6 py-4">
        <h1 className="text-xl font-semibold text-gray-100">多 Agent 系统</h1>
        <p className="text-sm text-gray-400">使用专家 Agent 或编排器执行任务</p>
      </header>

      <div className="flex-1 overflow-y-auto px-6 py-4 space-y-6">
        {/* Agent List */}
        <div className="grid grid-cols-2 lg:grid-cols-5 gap-3">
          {agents.map(agent => (
            <div key={agent.id} className={`bg-gray-800 border rounded-lg p-3 ${
              selectedAgent === agent.role ? 'border-brand-500' : 'border-gray-700'
            }`}>
              <div className="flex items-center gap-2 mb-1">
                <Bot className="w-4 h-4 text-brand-400" />
                <span className="text-sm font-medium text-gray-200">{agent.role}</span>
              </div>
              <p className="text-xs text-gray-400 line-clamp-2">{agent.description}</p>
            </div>
          ))}
        </div>

        {/* Mode Toggle + Input */}
        <div className="bg-gray-800 border border-gray-700 rounded-lg p-4 space-y-3">
          <div className="flex items-center gap-4">
            <h2 className="text-sm font-medium text-gray-300">执行模式</h2>
            <div className="flex gap-2">
              <button
                onClick={() => setMode('single')}
                className={`px-3 py-1 rounded text-sm transition-colors ${
                  mode === 'single' ? 'bg-brand-600 text-white' : 'bg-gray-700 text-gray-400 hover:text-gray-200'
                }`}
              >
                单个 Agent
              </button>
              <button
                onClick={() => setMode('orchestrate')}
                className={`px-3 py-1 rounded text-sm transition-colors ${
                  mode === 'orchestrate' ? 'bg-brand-600 text-white' : 'bg-gray-700 text-gray-400 hover:text-gray-200'
                }`}
              >
                编排器
              </button>
            </div>
          </div>

          {mode === 'single' && (
            <div className="flex items-center gap-2">
              <span className="text-xs text-gray-400">Agent:</span>
              <select
                value={selectedAgent}
                onChange={e => setSelectedAgent(e.target.value)}
                className="px-2 py-1 bg-gray-900 border border-gray-600 rounded text-gray-100 text-sm"
              >
                {agents.map(a => (
                  <option key={a.id} value={a.role}>{a.role} - {a.description}</option>
                ))}
              </select>
            </div>
          )}

          <div className="flex gap-2">
            <input
              type="text"
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleExecute()}
              placeholder={mode === 'orchestrate'
                ? "输入复杂任务，自动分解并分发..."
                : `向 ${selectedAgent} Agent 发送任务...`}
              className="flex-1 px-3 py-2 bg-gray-900 border border-gray-600 rounded-lg text-gray-100 text-sm placeholder-gray-500 focus:outline-none focus:border-brand-500"
            />
            <button
              onClick={handleExecute}
              disabled={loading || !input.trim()}
              className="px-4 py-2 rounded-lg bg-brand-600 hover:bg-brand-700 disabled:bg-gray-700 text-white text-sm transition-colors flex items-center gap-2"
            >
              {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Send className="w-4 h-4" />}
              执行
            </button>
          </div>
        </div>

        {error && (
          <div className="text-sm text-red-400 bg-red-900/20 border border-red-800 rounded-lg px-4 py-2">
            {error}
          </div>
        )}

        {/* Result */}
        {result && (
          <div className="space-y-4">
            {/* Metadata */}
            {result.taskDetails && result.taskDetails.length > 0 && (
              <div className="bg-gray-800 border border-gray-700 rounded-lg p-4">
                <h3 className="text-sm font-medium text-gray-300 mb-3">子任务 ({result.subtaskCount})</h3>
                <div className="space-y-2">
                  {result.taskDetails.map((td, i) => (
                    <div key={i} className="flex items-center gap-3 text-sm">
                      <span className={`w-2 h-2 rounded-full ${
                        td.status === 'completed' ? 'bg-green-400' : td.status === 'error' ? 'bg-red-400' : 'bg-yellow-400'
                      }`} />
                      <span className="text-gray-400 flex-1 truncate">{td.task}</span>
                      <ArrowRight className="w-3 h-3 text-gray-600" />
                      <span className="text-brand-400">{td.agent}</span>
                    </div>
                  ))}
                </div>
                {result.agentsUsed && (
                  <div className="mt-3 flex gap-2">
                    {result.agentsUsed.map(a => (
                      <span key={a} className="text-xs px-2 py-0.5 rounded-full bg-brand-900/30 text-brand-300 border border-brand-700">
                        {a}
                      </span>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* Response */}
            <div className="bg-gray-800 border border-gray-700 rounded-lg p-4">
              <div className="flex items-center gap-2 mb-3">
                <Bot className="w-4 h-4 text-brand-400" />
                <span className="text-sm font-medium text-gray-300">
                  {result.agentId} 的响应
                </span>
              </div>
              <div className="text-sm text-gray-200 whitespace-pre-wrap prose prose-invert max-w-none">
                {result.content}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
