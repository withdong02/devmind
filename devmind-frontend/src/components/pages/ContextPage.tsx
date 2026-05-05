import { useState, useEffect } from 'react'
import { Layers, Play, Settings, Loader2 } from 'lucide-react'
import { chatService } from '../../services/chatService'

interface ContextComponent {
  id: string
  type: string
  tokenCount: number
  priorityScore: number
  preview: string
}

interface ContextResult {
  totalTokens: number
  tokenBudget: number
  withinBudget: boolean
  componentCount: number
  components: ContextComponent[]
  renderedPreview: string
}

const TYPE_COLORS: Record<string, string> = {
  SYSTEM_PROMPT: 'bg-purple-900/30 border-purple-700 text-purple-300',
  USER_PREFERENCE: 'bg-blue-900/30 border-blue-700 text-blue-300',
  EPISODIC_MEMORY: 'bg-green-900/30 border-green-700 text-green-300',
  SEMANTIC_KNOWLEDGE: 'bg-yellow-900/30 border-yellow-700 text-yellow-300',
  RAG_DOCUMENT: 'bg-orange-900/30 border-orange-700 text-orange-300',
  CONVERSATION_HISTORY: 'bg-gray-800 border-gray-600 text-gray-300',
  CURRENT_QUERY: 'bg-brand-900/30 border-brand-700 text-brand-300',
}

export function ContextPage() {
  const [query, setQuery] = useState('')
  const [result, setResult] = useState<ContextResult | null>(null)
  const [budget, setBudget] = useState(4096)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    chatService.getContextBudget().then(d => setBudget(d.totalBudget)).catch(() => {})
  }, [])

  const handleBuild = async () => {
    if (!query.trim()) return
    setLoading(true)
    setError('')
    try {
      const data = await chatService.buildContext(query)
      setResult(data)
    } catch {
      setError('Failed to build context')
    } finally {
      setLoading(false)
    }
  }

  const handleBudgetUpdate = async (newBudget: number) => {
    setBudget(newBudget)
    try {
      await chatService.updateContextBudget(newBudget)
    } catch {}
  }

  return (
    <div className="flex flex-col h-full">
      <header className="border-b border-gray-700 px-6 py-4">
        <h1 className="text-xl font-semibold text-gray-100">Context Engineering</h1>
        <p className="text-sm text-gray-400">Debug context assembly, token budget, and priority scoring</p>
      </header>

      <div className="flex-1 overflow-y-auto px-6 py-4 space-y-6">
        {/* Budget Config */}
        <div className="bg-gray-800 border border-gray-700 rounded-lg p-4">
          <h2 className="text-sm font-medium text-gray-300 mb-3 flex items-center gap-2">
            <Settings className="w-4 h-4" />
            Token Budget
          </h2>
          <div className="flex items-center gap-4">
            <input
              type="range"
              min={1024}
              max={16384}
              step={256}
              value={budget}
              onChange={e => handleBudgetUpdate(Number(e.target.value))}
              className="flex-1"
            />
            <span className="text-sm text-gray-200 w-20 text-right">{budget} tokens</span>
          </div>
        </div>

        {/* Query Input */}
        <div className="bg-gray-800 border border-gray-700 rounded-lg p-4">
          <h2 className="text-sm font-medium text-gray-300 mb-3 flex items-center gap-2">
            <Play className="w-4 h-4" />
            Test Context Assembly
          </h2>
          <div className="flex gap-2">
            <input
              type="text"
              value={query}
              onChange={e => setQuery(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleBuild()}
              placeholder="Enter a query to see how context is assembled..."
              className="flex-1 px-3 py-2 bg-gray-900 border border-gray-600 rounded-lg text-gray-100 text-sm placeholder-gray-500 focus:outline-none focus:border-brand-500"
            />
            <button
              onClick={handleBuild}
              disabled={loading || !query.trim()}
              className="px-4 py-2 rounded-lg bg-brand-600 hover:bg-brand-700 disabled:bg-gray-700 text-white text-sm transition-colors flex items-center gap-2"
            >
              {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Layers className="w-4 h-4" />}
              Build
            </button>
          </div>
        </div>

        {error && (
          <div className="text-sm text-red-400 bg-red-900/20 border border-red-800 rounded-lg px-4 py-2">
            {error}
          </div>
        )}

        {/* Results */}
        {result && (
          <>
            {/* Summary Bar */}
            <div className="bg-gray-800 border border-gray-700 rounded-lg p-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-300">Budget Usage</span>
                <span className={`text-sm ${result.withinBudget ? 'text-green-400' : 'text-red-400'}`}>
                  {result.totalTokens} / {result.tokenBudget} tokens
                </span>
              </div>
              <div className="w-full bg-gray-700 rounded-full h-2">
                <div
                  className={`h-2 rounded-full transition-all ${result.withinBudget ? 'bg-green-500' : 'bg-red-500'}`}
                  style={{ width: `${Math.min(100, (result.totalTokens / result.tokenBudget) * 100)}%` }}
                />
              </div>
              <div className="mt-2 text-xs text-gray-500">
                {result.componentCount} components assembled
              </div>
            </div>

            {/* Component Breakdown */}
            <div className="space-y-2">
              <h2 className="text-sm font-medium text-gray-300">Components (by priority)</h2>
              {result.components.map((comp, i) => (
                <div key={comp.id + i} className={`border rounded-lg p-3 ${TYPE_COLORS[comp.type] || 'bg-gray-800 border-gray-700'}`}>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-xs font-mono px-1.5 py-0.5 rounded bg-black/20">{comp.type}</span>
                    <span className="text-xs opacity-70">{comp.tokenCount} tokens</span>
                    <span className="ml-auto text-xs opacity-70">score: {comp.priorityScore.toFixed(3)}</span>
                  </div>
                  <pre className="text-xs opacity-80 whitespace-pre-wrap break-words max-h-24 overflow-y-auto">
                    {comp.preview}
                  </pre>
                </div>
              ))}
            </div>

            {/* Rendered Preview */}
            <div className="bg-gray-800 border border-gray-700 rounded-lg p-4">
              <h2 className="text-sm font-medium text-gray-300 mb-3">Rendered Prompt Preview</h2>
              <pre className="text-xs text-gray-300 bg-gray-900 rounded p-3 overflow-x-auto max-h-96 whitespace-pre-wrap">
                {result.renderedPreview}
              </pre>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
