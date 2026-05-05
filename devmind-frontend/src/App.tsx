import { useState } from 'react'
import { ChatPanel } from './components/chat/ChatPanel'
import { Sidebar } from './components/layout/Sidebar'
import { SkillsPage } from './components/pages/SkillsPage'
import { RagPage } from './components/pages/RagPage'
import { MemoryPage } from './components/pages/MemoryPage'
import { ContextPage } from './components/pages/ContextPage'
import { AgentsPage } from './components/pages/AgentsPage'
import { SettingsPage } from './components/pages/SettingsPage'

type Page = 'chat' | 'skills' | 'rag' | 'memory' | 'context' | 'agents' | 'settings'

export default function App() {
  const [sessionId, setSessionId] = useState<string | null>(null)
  const [page, setPage] = useState<Page>('chat')

  return (
    <div className="flex h-screen">
      <Sidebar
        sessionId={sessionId}
        onSessionChange={setSessionId}
        currentPage={page}
        onNavigate={setPage}
      />
      <main className="flex-1 flex flex-col">
        {page === 'chat' && (
          <ChatPanel sessionId={sessionId} onSessionCreated={setSessionId} />
        )}
        {page === 'skills' && <SkillsPage />}
        {page === 'rag' && <RagPage />}
        {page === 'memory' && <MemoryPage />}
        {page === 'context' && <ContextPage />}
        {page === 'agents' && <AgentsPage />}
        {page === 'settings' && <SettingsPage />}
      </main>
    </div>
  )
}
