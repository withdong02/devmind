import { useState, useEffect } from 'react'
import { Settings, Shield, Activity, CheckCircle, XCircle, Loader2 } from 'lucide-react'
import { chatService } from '../../services/chatService'

interface HookInfo { name: string; order: number }
interface ProfileInfo { description?: string; model?: string; temperature?: number; hooks?: Record<string, unknown[]>; guardrails?: Record<string, unknown> }
interface AuditEntry { id: string; userId: string; action: string; actor: string; target: string; success: boolean; error: string; createdAt: string }

export function SettingsPage() {
  const [hooks, setHooks] = useState<Record<string, HookInfo[]>>({})
  const [profiles, setProfiles] = useState<{ active: string; profiles: Record<string, ProfileInfo> }>({ active: 'default', profiles: {} })
  const [auditLogs, setAuditLogs] = useState<AuditEntry[]>([])
  const [auditTotal, setAuditTotal] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([
      chatService.getHarnessHooks(),
      chatService.getHarnessProfiles(),
      chatService.getAuditLogs(0, 20),
    ]).then(([h, p, a]) => {
      setHooks(h)
      setProfiles(p)
      setAuditLogs(a.logs || [])
      setAuditTotal(a.total || 0)
    }).catch(() => setError('加载设置失败')).finally(() => setLoading(false))
  }, [])

  const handleActivateProfile = async (name: string) => {
    try {
      await chatService.activateProfile(name)
      setProfiles(prev => ({ ...prev, active: name }))
    } catch { setError('激活 Profile 失败') }
  }

  if (loading) return <div className="flex items-center justify-center h-full"><Loader2 className="w-8 h-8 animate-spin text-gray-500" /></div>

  return (
    <div className="flex flex-col h-full">
      <header className="border-b border-gray-700 px-6 py-4">
        <h1 className="text-xl font-semibold text-gray-100">设置 & Harness</h1>
        <p className="text-sm text-gray-400">Hook 链、Profile、守卫规则和审计日志</p>
      </header>
      <div className="flex-1 overflow-y-auto px-6 py-4 space-y-6">
        {error && <div className="text-sm text-red-400 bg-red-900/20 border border-red-800 rounded-lg px-4 py-2">{error}</div>}

        {/* Profiles */}
        <div className="bg-gray-800 border border-gray-700 rounded-lg p-4">
          <h2 className="text-sm font-medium text-gray-300 mb-3 flex items-center gap-2"><Settings className="w-4 h-4" /> Agent Profile</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {Object.entries(profiles.profiles).map(([name, profile]) => (
              <div key={name} className={`border rounded-lg p-3 cursor-pointer transition-colors ${
                profiles.active === name ? 'border-brand-500 bg-gray-700' : 'border-gray-600 hover:border-gray-500'
              }`} onClick={() => handleActivateProfile(name)}>
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-sm font-medium text-gray-200">{name}</span>
                  {profiles.active === name && <span className="text-xs px-1.5 py-0.5 rounded bg-brand-600 text-white">当前</span>}
                </div>
                <p className="text-xs text-gray-400">{profile.description || '暂无描述'}</p>
                <div className="mt-2 flex gap-2 text-xs text-gray-500">
                  {profile.model && <span>model: {profile.model}</span>}
                  {profile.temperature !== undefined && <span>temp: {profile.temperature}</span>}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Hooks */}
        <div className="bg-gray-800 border border-gray-700 rounded-lg p-4">
          <h2 className="text-sm font-medium text-gray-300 mb-3 flex items-center gap-2"><Shield className="w-4 h-4" /> Hook 链</h2>
          <div className="space-y-3">
            {Object.entries(hooks).map(([type, hookList]) => (
              <div key={type}>
                <span className="text-xs font-mono text-gray-400">{type}</span>
                <div className="flex gap-2 mt-1 flex-wrap">
                  {hookList.map(h => (
                    <span key={h.name} className="text-xs px-2 py-1 rounded bg-gray-700 text-gray-300">
                      {h.name} <span className="text-gray-500">({h.order})</span>
                    </span>
                  ))}
                  {hookList.length === 0 && <span className="text-xs text-gray-600">无 Hook</span>}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Guardrails */}
        {profiles.profiles[profiles.active]?.guardrails && (
          <div className="bg-gray-800 border border-gray-700 rounded-lg p-4">
            <h2 className="text-sm font-medium text-gray-300 mb-3">当前守卫规则</h2>
            <pre className="text-xs text-gray-300 bg-gray-900 rounded p-3 overflow-x-auto">
              {JSON.stringify(profiles.profiles[profiles.active].guardrails, null, 2)}
            </pre>
          </div>
        )}

        {/* Audit Log */}
        <div className="bg-gray-800 border border-gray-700 rounded-lg p-4">
          <h2 className="text-sm font-medium text-gray-300 mb-3 flex items-center gap-2">
            <Activity className="w-4 h-4" /> 审计日志 <span className="text-gray-500">({auditTotal})</span>
          </h2>
          {auditLogs.length > 0 ? (
            <div className="space-y-1">
              {auditLogs.map(log => (
                <div key={log.id} className="flex items-center gap-3 text-xs py-1 border-b border-gray-700/50">
                  {log.success ? <CheckCircle className="w-3 h-3 text-green-400" /> : <XCircle className="w-3 h-3 text-red-400" />}
                  <span className="text-gray-400 w-16">{log.action}</span>
                  <span className="text-gray-500 flex-1 truncate">{log.target}</span>
                  <span className="text-gray-600">{new Date(log.createdAt).toLocaleString()}</span>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-gray-600">暂无审计记录</p>
          )}
        </div>
      </div>
    </div>
  )
}
