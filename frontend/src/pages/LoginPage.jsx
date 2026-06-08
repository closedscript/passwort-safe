import { useState } from 'react'
import { login } from '../services/api'

// Pure function: Passwortgüte prüfen (keine Seiteneffekte) – Modul 323
const checkPasswordStrength = (pw) => {
  if (!pw) return { score: 0, label: '', color: '' }
  const checks = [
    pw.length >= 8,
    /[A-Z]/.test(pw),
    /[a-z]/.test(pw),
    /[0-9]/.test(pw),
    /[^A-Za-z0-9]/.test(pw),
  ]
  const score = checks.filter(Boolean).length
  const map = [
    { label: '', color: '' },
    { label: 'Sehr schwach', color: '#e05c5c' },
    { label: 'Schwach', color: '#e08c5c' },
    { label: 'Mittel', color: '#e0c05c' },
    { label: 'Stark', color: '#4caf7d' },
    { label: 'Sehr stark', color: '#2e9e6b' },
  ]
  return { score, ...map[score] }
}

export default function LoginPage({ onLogin }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPw, setShowPw] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const strength = checkPasswordStrength(password)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const data = await login(username, password)
      onLogin(data.token, password)
    } catch {
      setError('Ungültige Anmeldedaten')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh' }}>
      <div className="card" style={{ width: 380 }}>
        <div style={{ textAlign: 'center', marginBottom: 28 }}>
          <div style={{ fontSize: 36, marginBottom: 8 }}>🔐</div>
          <h1 style={{ fontSize: 20, fontWeight: 600 }}>Passwort-Safe</h1>
          <p style={{ color: 'var(--muted)', marginTop: 4 }}>Melden Sie sich an</p>
        </div>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <div>
            <label style={{ display: 'block', marginBottom: 6, color: 'var(--muted)' }}>Benutzername</label>
            <input
              value={username}
              onChange={e => setUsername(e.target.value)}
              placeholder="admin"
              autoComplete="username"
              required
            />
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: 6, color: 'var(--muted)' }}>Master-Passwort</label>
            <div style={{ position: 'relative' }}>
              <input
                type={showPw ? 'text' : 'password'}
                value={password}
                onChange={e => setPassword(e.target.value)}
                placeholder="••••••••"
                autoComplete="current-password"
                required
              />
              <button
                type="button"
                onClick={() => setShowPw(v => !v)}
                style={{
                  position: 'absolute', right: 8, top: '50%', transform: 'translateY(-50%)',
                  background: 'none', padding: '4px 6px', color: 'var(--muted)', fontSize: 12
                }}
              >
                {showPw ? 'verbergen' : 'zeigen'}
              </button>
            </div>

            {/* Passwortgüte-Anzeige */}
            {password && (
              <div style={{ marginTop: 8 }}>
                <div style={{ height: 4, background: 'var(--border)', borderRadius: 2, overflow: 'hidden' }}>
                  <div style={{
                    height: '100%',
                    width: `${(strength.score / 5) * 100}%`,
                    background: strength.color,
                    transition: 'width 0.3s'
                  }} />
                </div>
                <span style={{ fontSize: 11, color: strength.color, marginTop: 4, display: 'block' }}>
                  {strength.label}
                </span>
              </div>
            )}
          </div>

          {error && (
            <p style={{ color: 'var(--danger)', fontSize: 13, textAlign: 'center' }}>{error}</p>
          )}

          <button type="submit" className="primary" disabled={loading} style={{ marginTop: 4 }}>
            {loading ? 'Anmelden…' : 'Anmelden'}
          </button>
        </form>

        <p style={{ textAlign: 'center', color: 'var(--muted)', fontSize: 12, marginTop: 16 }}>
          Standard: admin / Admin123!
        </p>
      </div>
    </div>
  )
}
