import { MessageSquare, Cpu, Search, Brain, Layers, Bot, Settings } from 'lucide-react'

type Page = 'chat' | 'skills' | 'rag' | 'memory' | 'context' | 'agents' | 'settings'

interface SidebarProps {
  sessionId: string | null
  onSessionChange: (id: string) => void
  currentPage: Page
  onNavigate: (page: Page) => void
}

export function Sidebar({ currentPage, onNavigate }: SidebarProps) {
  return (
    <aside className="w-64 bg-gray-800 border-r border-gray-700 flex flex-col">
      {/* Logo */}
      <div className="px-4 py-4 border-b border-gray-700">
        <h1 className="text-lg font-bold text-brand-400">DevMind</h1>
        <p className="text-xs text-gray-500">AI Developer Assistant</p>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-2 py-4 space-y-1">
        <button
          onClick={() => onNavigate('chat')}
          className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg transition-colors ${
            currentPage === 'chat'
              ? 'bg-gray-700 text-gray-100'
              : 'text-gray-400 hover:bg-gray-700 hover:text-gray-100'
          }`}
        >
          <MessageSquare className="w-5 h-5" />
          <span>Chat</span>
        </button>
        <button
          onClick={() => onNavigate('skills')}
          className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg transition-colors ${
            currentPage === 'skills'
              ? 'bg-gray-700 text-gray-100'
              : 'text-gray-400 hover:bg-gray-700 hover:text-gray-100'
          }`}
        >
          <Cpu className="w-5 h-5" />
          <span>Skills</span>
        </button>
        <button
          onClick={() => onNavigate('rag')}
          className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg transition-colors ${
            currentPage === 'rag'
              ? 'bg-gray-700 text-gray-100'
              : 'text-gray-400 hover:bg-gray-700 hover:text-gray-100'
          }`}
        >
          <Search className="w-5 h-5" />
          <span>RAG Search</span>
        </button>
        <button
          onClick={() => onNavigate('memory')}
          className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg transition-colors ${
            currentPage === 'memory'
              ? 'bg-gray-700 text-gray-100'
              : 'text-gray-400 hover:bg-gray-700 hover:text-gray-100'
          }`}
        >
          <Brain className="w-5 h-5" />
          <span>Memory</span>
        </button>
        <button
          onClick={() => onNavigate('context')}
          className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg transition-colors ${
            currentPage === 'context'
              ? 'bg-gray-700 text-gray-100'
              : 'text-gray-400 hover:bg-gray-700 hover:text-gray-100'
          }`}
        >
          <Layers className="w-5 h-5" />
          <span>Context</span>
        </button>
        <button
          onClick={() => onNavigate('agents')}
          className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg transition-colors ${
            currentPage === 'agents'
              ? 'bg-gray-700 text-gray-100'
              : 'text-gray-400 hover:bg-gray-700 hover:text-gray-100'
          }`}
        >
          <Bot className="w-5 h-5" />
          <span>Agents</span>
        </button>
        <button
          onClick={() => onNavigate('settings')}
          className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg transition-colors ${
            currentPage === 'settings'
              ? 'bg-gray-700 text-gray-100'
              : 'text-gray-400 hover:bg-gray-700 hover:text-gray-100'
          }`}
        >
          <Settings className="w-5 h-5" />
          <span>Settings</span>
        </button>
      </nav>

      {/* Footer */}
      <div className="px-4 py-3 border-t border-gray-700">
        <p className="text-xs text-gray-500">DevMind v1.0.0</p>
      </div>
    </aside>
  )
}
