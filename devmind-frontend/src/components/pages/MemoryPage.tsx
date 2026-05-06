import { useState, useEffect } from 'react'
import { Brain, Search, Trash2, Plus, Loader2, Database, BookOpen, Lightbulb, Clock } from 'lucide-react'
import { chatService } from '../../services/chatService'

interface MemoryItem {
  id: string
  type: string
  content: string
  memoryType?: string
  outcome?: string
  source?: string
  confidence?: number
  importance: number
  createdAt: string
}

interface MemoryStats {
  shortTermCount: number
  longTermCount: number
  episodicCount: number
  semanticCount: number
}

export function MemoryPage() {
  const [memories, setMemories] = useState<MemoryItem[]>([])
  const [stats, setStats] = useState<MemoryStats | null>(null)
  const [searchQuery, setSearchQuery] = useState('')
  const [activeFilter, setActiveFilter] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [showAdd, setShowAdd] = useState(false)
  const [newContent, setNewContent] = useState('')
  const [newType, setNewType] = useState('LONG_TERM')

  const loadData = async () => {
    setLoading(true)
    try {
      const [statsData, memoriesData] = await Promise.all([
        chatService.getMemoryStats(),
        chatService.listMemories(activeFilter, 50),
      ])
      setStats(statsData)
      setMemories(memoriesData)
    } catch {
      setError('加载记忆失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadData() }, [activeFilter])

  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      loadData()
      return
    }
    setLoading(true)
    try {
      const results = await chatService.searchMemories(searchQuery, activeFilter, 20)
      setMemories(results)
    } catch {
      setError('搜索失败')
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (type: string, id: string) => {
    try {
      await chatService.deleteMemory(type, id)
      setMemories(prev => prev.filter(m => m.id !== id))
    } catch {
      setError('删除失败')
    }
  }

  const handleAdd = async () => {
    if (!newContent.trim()) return
    try {
      await chatService.storeMemory(newType, newContent)
      setNewContent('')
      setShowAdd(false)
      loadData()
    } catch {
      setError('存储失败')
    }
  }

  const typeIcon = (type: string) => {
    switch (type) {
      case 'LONG_TERM': return <Database className="w-4 h-4 text-blue-400" />
      case 'EPISODIC': return <BookOpen className="w-4 h-4 text-green-400" />
      case 'SEMANTIC': return <Lightbulb className="w-4 h-4 text-yellow-400" />
      case 'SHORT_TERM': return <Clock className="w-4 h-4 text-purple-400" />
      default: return <Brain className="w-4 h-4 text-gray-400" />
    }
  }

  return (
    <div className="flex flex-col h-full">
      <header className="border-b border-gray-700 px-6 py-4">
        <h1 className="text-xl font-semibold text-gray-100">记忆</h1>
        <p className="text-sm text-gray-400">查看和管理跨会话的 AI 记忆</p>
      </header>

      <div className="flex-1 overflow-y-auto px-6 py-4 space-y-6">
        {/* Stats */}
        {stats && (
          <div className="grid grid-cols-4 gap-3">
            {[
              { label: '短期', count: stats.shortTermCount, color: 'text-purple-400', icon: Clock },
              { label: '长期', count: stats.longTermCount, color: 'text-blue-400', icon: Database },
              { label: '情景', count: stats.episodicCount, color: 'text-green-400', icon: BookOpen },
              { label: '语义', count: stats.semanticCount, color: 'text-yellow-400', icon: Lightbulb },
            ].map(s => (
              <button
                key={s.label}
                onClick={() => setActiveFilter(activeFilter === s.label.toUpperCase().replace('-', '_') ? null : s.label.toUpperCase().replace('-', '_'))}
                className={`bg-gray-800 border rounded-lg p-3 text-left transition-colors ${
                  activeFilter === s.label.toUpperCase().replace('-', '_')
                    ? 'border-brand-500 bg-gray-700'
                    : 'border-gray-700 hover:border-gray-600'
                }`}
              >
                <div className="flex items-center gap-2 mb-1">
                  <s.icon className={`w-4 h-4 ${s.color}`} />
                  <span className="text-xs text-gray-400">{s.label}</span>
                </div>
                <span className="text-lg font-semibold text-gray-100">{s.count}</span>
              </button>
            ))}
          </div>
        )}

        {/* Search + Add */}
        <div className="flex gap-2">
          <div className="flex-1 flex gap-2">
            <input
              type="text"
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleSearch()}
              placeholder="搜索记忆..."
              className="flex-1 px-3 py-2 bg-gray-900 border border-gray-600 rounded-lg text-gray-100 text-sm placeholder-gray-500 focus:outline-none focus:border-brand-500"
            />
            <button
              onClick={handleSearch}
              disabled={loading}
              className="px-4 py-2 rounded-lg bg-brand-600 hover:bg-brand-700 disabled:bg-gray-700 text-white text-sm transition-colors flex items-center gap-2"
            >
              {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Search className="w-4 h-4" />}
              搜索
            </button>
          </div>
          <button
            onClick={() => setShowAdd(!showAdd)}
            className="px-4 py-2 rounded-lg bg-gray-700 hover:bg-gray-600 text-gray-200 text-sm transition-colors flex items-center gap-2"
          >
            <Plus className="w-4 h-4" />
            添加
          </button>
        </div>

        {/* Add Form */}
        {showAdd && (
          <div className="bg-gray-800 border border-gray-700 rounded-lg p-4 space-y-3">
            <select
              value={newType}
              onChange={e => setNewType(e.target.value)}
              className="px-3 py-2 bg-gray-900 border border-gray-600 rounded-lg text-gray-100 text-sm"
            >
              <option value="LONG_TERM">长期记忆（偏好）</option>
              <option value="EPISODIC">情景记忆（任务）</option>
              <option value="SEMANTIC">语义记忆（事实）</option>
            </select>
            <textarea
              value={newContent}
              onChange={e => setNewContent(e.target.value)}
              placeholder="输入记忆内容..."
              rows={3}
              className="w-full px-3 py-2 bg-gray-900 border border-gray-600 rounded-lg text-gray-100 text-sm placeholder-gray-500 focus:outline-none focus:border-brand-500"
            />
            <div className="flex gap-2 justify-end">
              <button onClick={() => setShowAdd(false)} className="px-3 py-1.5 text-sm text-gray-400 hover:text-gray-200">取消</button>
              <button onClick={handleAdd} className="px-4 py-1.5 rounded-lg bg-brand-600 hover:bg-brand-700 text-white text-sm">保存</button>
            </div>
          </div>
        )}

        {error && (
          <div className="text-sm text-red-400 bg-red-900/20 border border-red-800 rounded-lg px-4 py-2">
            {error}
          </div>
        )}

        {/* Memory List */}
        <div className="space-y-2">
          {memories.map(m => (
            <div key={m.id} className="bg-gray-800 border border-gray-700 rounded-lg p-4 group">
              <div className="flex items-start gap-3">
                <div className="mt-0.5">{typeIcon(m.type)}</div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-xs px-2 py-0.5 rounded-full bg-gray-700 text-gray-400">{m.type}</span>
                    {m.memoryType && (
                      <span className="text-xs text-blue-400">{m.memoryType}</span>
                    )}
                    {m.outcome && m.outcome !== 'UNKNOWN' && (
                      <span className={`text-xs ${m.outcome === 'SUCCESS' ? 'text-green-400' : 'text-red-400'}`}>
                        {m.outcome}
                      </span>
                    )}
                    {m.confidence !== undefined && (
                      <span className="text-xs text-yellow-400">conf: {m.confidence.toFixed(2)}</span>
                    )}
                    <span className="ml-auto text-xs text-gray-500">
                      {new Date(m.createdAt).toLocaleDateString()}
                    </span>
                  </div>
                  <p className="text-sm text-gray-200 whitespace-pre-wrap">{m.content}</p>
                </div>
                <button
                  onClick={() => handleDelete(m.type, m.id)}
                  className="opacity-0 group-hover:opacity-100 p-1 text-gray-500 hover:text-red-400 transition-all"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))}
          {memories.length === 0 && !loading && (
            <div className="text-center text-gray-500 py-12">
              <Brain className="w-12 h-12 mx-auto mb-3 opacity-30" />
              <p>暂无记忆</p>
              <p className="text-xs mt-1">记忆会在对话过程中自动创建</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
