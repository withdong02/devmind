import { useState, useRef, useEffect } from 'react'
import { MessageBubble } from './MessageBubble'
import { Send, Loader2 } from 'lucide-react'
import { chatService, Message } from '../../services/chatService'

interface ChatPanelProps {
  sessionId: string | null
  onSessionCreated: (id: string) => void
}

export function ChatPanel({ sessionId, onSessionCreated }: ChatPanelProps) {
  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [streamingContent, setStreamingContent] = useState('')
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(scrollToBottom, [messages, streamingContent])

  const handleSend = async () => {
    if (!input.trim() || isLoading) return

    const userMessage: Message = {
      id: crypto.randomUUID(),
      role: 'user',
      content: input.trim(),
    }
    setMessages(prev => [...prev, userMessage])
    setInput('')
    setIsLoading(true)
    setStreamingContent('')

    try {
      // Create session if needed
      let currentSessionId = sessionId
      if (!currentSessionId) {
        const session = await chatService.createSession()
        currentSessionId = session.id
        onSessionCreated(currentSessionId)
      }

      // Use SSE streaming
      let fullContent = ''
      await chatService.streamMessage(currentSessionId, userMessage.content, (chunk) => {
        fullContent += chunk
        setStreamingContent(fullContent)
      })

      const assistantMessage: Message = {
        id: crypto.randomUUID(),
        role: 'assistant',
        content: fullContent,
      }
      setMessages(prev => [...prev, assistantMessage])
      setStreamingContent('')
    } catch (error) {
      const errorMessage: Message = {
        id: crypto.randomUUID(),
        role: 'assistant',
        content: '出错了，请确认后端服务已启动。',
      }
      setMessages(prev => [...prev, errorMessage])
      setStreamingContent('')
    } finally {
      setIsLoading(false)
    }
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <header className="border-b border-gray-700 px-6 py-4">
        <h1 className="text-xl font-semibold text-gray-100">DevMind</h1>
        <p className="text-sm text-gray-400">AI 开发者助手</p>
      </header>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-6 py-4 space-y-4">
        {messages.length === 0 && (
          <div className="flex items-center justify-center h-full">
            <div className="text-center text-gray-500">
              <div className="text-6xl mb-4">&#129504;</div>
              <h2 className="text-xl font-medium mb-2">欢迎使用 DevMind</h2>
              <p className="text-sm">可以问我代码审查、Bug 分析、任务规划，或任何开发相关问题。</p>
            </div>
          </div>
        )}
        {messages.map(msg => (
          <MessageBubble key={msg.id} message={msg} />
        ))}
        {streamingContent && (
          <MessageBubble
            message={{ id: 'streaming', role: 'assistant', content: streamingContent }}
            isStreaming
          />
        )}
        {isLoading && !streamingContent && (
          <div className="flex items-center gap-2 text-gray-400">
            <Loader2 className="w-4 h-4 animate-spin" />
            <span className="text-sm">思考中...</span>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input */}
      <div className="border-t border-gray-700 px-6 py-4">
        <div className="flex gap-3">
          <textarea
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="向 DevMind 提问..."
            className="flex-1 bg-gray-800 border border-gray-600 rounded-lg px-4 py-3 text-gray-100 placeholder-gray-500 resize-none focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500"
            rows={1}
            disabled={isLoading}
          />
          <button
            onClick={handleSend}
            disabled={!input.trim() || isLoading}
            className="bg-brand-600 hover:bg-brand-700 disabled:bg-gray-700 disabled:cursor-not-allowed text-white rounded-lg px-4 py-3 flex items-center gap-2 transition-colors"
          >
            <Send className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  )
}
