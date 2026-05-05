import { Message } from '../../services/chatService'
import { Bot, User, Wrench } from 'lucide-react'

interface MessageBubbleProps {
  message: Message
  isStreaming?: boolean
}

export function MessageBubble({ message, isStreaming }: MessageBubbleProps) {
  const isUser = message.role === 'user'
  const hasSkill = message.skillId && message.skillId.length > 0

  return (
    <div className={`flex gap-3 ${isUser ? 'justify-end' : 'justify-start'}`}>
      {!isUser && (
        <div className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${
          hasSkill ? 'bg-amber-600' : 'bg-brand-600'
        }`}>
          {hasSkill ? <Wrench className="w-4 h-4 text-white" /> : <Bot className="w-4 h-4 text-white" />}
        </div>
      )}
      <div className="max-w-[70%]">
        {/* Skill indicator */}
        {hasSkill && (
          <div className="flex items-center gap-2 mb-1">
            <span className="text-xs px-2 py-0.5 rounded-full bg-amber-900/50 text-amber-300 border border-amber-700">
              Skill: {message.skillId}
            </span>
          </div>
        )}
        <div
          className={`rounded-lg px-4 py-3 ${
            isUser
              ? 'bg-brand-600 text-white'
              : hasSkill
                ? 'bg-gray-800 text-gray-100 border border-amber-800/50'
                : 'bg-gray-800 text-gray-100 border border-gray-700'
          }`}
        >
          <div className="text-sm whitespace-pre-wrap break-words">
            {message.content}
            {isStreaming && (
              <span className="inline-block w-2 h-4 ml-1 bg-gray-400 animate-pulse" />
            )}
          </div>
        </div>
      </div>
      {isUser && (
        <div className="w-8 h-8 rounded-full bg-gray-600 flex items-center justify-center flex-shrink-0">
          <User className="w-4 h-4 text-white" />
        </div>
      )}
    </div>
  )
}
