import { useState, useEffect } from 'react'
import { chatService, SkillDefinition } from '../../services/chatService'
import { Cpu, Play, Tag } from 'lucide-react'

export function SkillsPage() {
  const [skills, setSkills] = useState<SkillDefinition[]>([])
  const [loading, setLoading] = useState(true)
  const [executing, setExecuting] = useState<string | null>(null)
  const [result, setResult] = useState<{ skillId: string; content: string } | null>(null)

  useEffect(() => {
    chatService.getSkills()
      .then(setSkills)
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  const handleExecute = async (skill: SkillDefinition) => {
    setExecuting(skill.id)
    setResult(null)
    try {
      const input: Record<string, unknown> = {}
      for (const key of Object.keys(skill.inputSchema)) {
        const value = prompt(`Enter ${key}:`)
        if (value) input[key] = value
      }
      const output = await chatService.executeSkill(skill.id, input)
      setResult({ skillId: skill.id, content: output.content })
    } catch {
      setResult({ skillId: skill.id, content: '执行失败，请确认后端服务已启动。' })
    } finally {
      setExecuting(null)
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-gray-400">加载技能中...</div>
      </div>
    )
  }

  return (
    <div className="flex flex-col h-full">
      <header className="border-b border-gray-700 px-6 py-4">
        <h1 className="text-xl font-semibold text-gray-100">技能</h1>
        <p className="text-sm text-gray-400">可由 Agent 或直接调用的模块化能力</p>
      </header>

      <div className="flex-1 overflow-y-auto px-6 py-4">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {skills.map(skill => (
            <div
              key={skill.id}
              className="bg-gray-800 border border-gray-700 rounded-lg p-4 hover:border-brand-500 transition-colors"
            >
              <div className="flex items-center gap-3 mb-3">
                <div className="w-10 h-10 rounded-lg bg-brand-600/20 flex items-center justify-center">
                  <Cpu className="w-5 h-5 text-brand-400" />
                </div>
                <div>
                  <h3 className="font-medium text-gray-100">{skill.name}</h3>
                  <code className="text-xs text-gray-500">{skill.id}</code>
                </div>
              </div>
              <p className="text-sm text-gray-400 mb-3">{skill.description}</p>
              <div className="flex flex-wrap gap-1 mb-3">
                {skill.tags.map(tag => (
                  <span
                    key={tag}
                    className="flex items-center gap-1 text-xs px-2 py-0.5 rounded-full bg-gray-700 text-gray-300"
                  >
                    <Tag className="w-3 h-3" />
                    {tag}
                  </span>
                ))}
              </div>
              <button
                onClick={() => handleExecute(skill)}
                disabled={executing === skill.id}
                className="w-full flex items-center justify-center gap-2 px-3 py-2 rounded-lg bg-brand-600 hover:bg-brand-700 disabled:bg-gray-700 text-white text-sm transition-colors"
              >
                <Play className="w-4 h-4" />
                {executing === skill.id ? '执行中...' : '执行'}
              </button>
            </div>
          ))}
        </div>

        {/* Execution result */}
        {result && (
          <div className="mt-6 bg-gray-800 border border-gray-700 rounded-lg p-4">
            <h3 className="font-medium text-gray-100 mb-2">
              结果: <code className="text-brand-400">{result.skillId}</code>
            </h3>
            <div className="text-sm text-gray-300 whitespace-pre-wrap">{result.content}</div>
          </div>
        )}
      </div>
    </div>
  )
}
