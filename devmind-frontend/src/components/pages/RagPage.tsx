import { useState } from 'react'
import { Search, FolderOpen, FileText, Code, Loader2 } from 'lucide-react'
import { chatService } from '../../services/chatService'

interface SearchResult {
  chunkId: string
  content: string
  symbolName: string
  chunkType: string
  startLine: number
  endLine: number
  score: number
  retrievalSource: string
}

export function RagPage() {
  const [searchQuery, setSearchQuery] = useState('')
  const [indexPath, setIndexPath] = useState('')
  const [results, setResults] = useState<SearchResult[]>([])
  const [indexing, setIndexing] = useState(false)
  const [searching, setSearching] = useState(false)
  const [indexResult, setIndexResult] = useState<{ documentsIndexed: number; chunksCreated: number } | null>(null)
  const [error, setError] = useState('')

  const handleSearch = async () => {
    if (!searchQuery.trim()) return
    setSearching(true)
    setError('')
    try {
      const data = await chatService.ragSearch(searchQuery, 10)
      setResults(data)
    } catch {
      setError('Search failed. Make sure the backend is running.')
    } finally {
      setSearching(false)
    }
  }

  const handleIndex = async () => {
    if (!indexPath.trim()) return
    setIndexing(true)
    setError('')
    setIndexResult(null)
    try {
      const data = await chatService.ragIndex(indexPath)
      setIndexResult(data)
    } catch {
      setError('Indexing failed. Check the path and try again.')
    } finally {
      setIndexing(false)
    }
  }

  return (
    <div className="flex flex-col h-full">
      <header className="border-b border-gray-700 px-6 py-4">
        <h1 className="text-xl font-semibold text-gray-100">RAG Search</h1>
        <p className="text-sm text-gray-400">Index documents and search code semantically</p>
      </header>

      <div className="flex-1 overflow-y-auto px-6 py-4 space-y-6">
        {/* Index Section */}
        <div className="bg-gray-800 border border-gray-700 rounded-lg p-4">
          <h2 className="text-sm font-medium text-gray-300 mb-3 flex items-center gap-2">
            <FolderOpen className="w-4 h-4" />
            Index Documents
          </h2>
          <div className="flex gap-2">
            <input
              type="text"
              value={indexPath}
              onChange={e => setIndexPath(e.target.value)}
              placeholder="Directory path (e.g., C:/Users/you/project/src)"
              className="flex-1 px-3 py-2 bg-gray-900 border border-gray-600 rounded-lg text-gray-100 text-sm placeholder-gray-500 focus:outline-none focus:border-brand-500"
            />
            <button
              onClick={handleIndex}
              disabled={indexing || !indexPath.trim()}
              className="px-4 py-2 rounded-lg bg-brand-600 hover:bg-brand-700 disabled:bg-gray-700 text-white text-sm transition-colors flex items-center gap-2"
            >
              {indexing ? <Loader2 className="w-4 h-4 animate-spin" /> : <FolderOpen className="w-4 h-4" />}
              {indexing ? 'Indexing...' : 'Index'}
            </button>
          </div>
          {indexResult && (
            <div className="mt-3 text-sm text-green-400">
              Indexed {indexResult.documentsIndexed} documents, created {indexResult.chunksCreated} chunks
            </div>
          )}
        </div>

        {/* Search Section */}
        <div className="bg-gray-800 border border-gray-700 rounded-lg p-4">
          <h2 className="text-sm font-medium text-gray-300 mb-3 flex items-center gap-2">
            <Search className="w-4 h-4" />
            Semantic Search
          </h2>
          <div className="flex gap-2">
            <input
              type="text"
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleSearch()}
              placeholder="Search for code, functions, concepts..."
              className="flex-1 px-3 py-2 bg-gray-900 border border-gray-600 rounded-lg text-gray-100 text-sm placeholder-gray-500 focus:outline-none focus:border-brand-500"
            />
            <button
              onClick={handleSearch}
              disabled={searching || !searchQuery.trim()}
              className="px-4 py-2 rounded-lg bg-brand-600 hover:bg-brand-700 disabled:bg-gray-700 text-white text-sm transition-colors flex items-center gap-2"
            >
              {searching ? <Loader2 className="w-4 h-4 animate-spin" /> : <Search className="w-4 h-4" />}
              Search
            </button>
          </div>
        </div>

        {error && (
          <div className="text-sm text-red-400 bg-red-900/20 border border-red-800 rounded-lg px-4 py-2">
            {error}
          </div>
        )}

        {/* Results */}
        {results.length > 0 && (
          <div className="space-y-3">
            <h2 className="text-sm font-medium text-gray-300">
              Results ({results.length})
            </h2>
            {results.map(r => (
              <div key={r.chunkId} className="bg-gray-800 border border-gray-700 rounded-lg p-4">
                <div className="flex items-center gap-2 mb-2">
                  {r.chunkType === 'METHOD' || r.chunkType === 'FUNCTION' ? (
                    <Code className="w-4 h-4 text-blue-400" />
                  ) : (
                    <FileText className="w-4 h-4 text-gray-400" />
                  )}
                  <span className="text-sm font-medium text-gray-200">{r.symbolName}</span>
                  <span className="text-xs px-2 py-0.5 rounded-full bg-gray-700 text-gray-400">{r.chunkType}</span>
                  <span className="text-xs text-gray-500">L{r.startLine}-{r.endLine}</span>
                  <span className="ml-auto text-xs text-brand-400">Score: {r.score.toFixed(4)}</span>
                </div>
                <pre className="text-xs text-gray-300 bg-gray-900 rounded p-3 overflow-x-auto max-h-48">
                  {r.content}
                </pre>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
