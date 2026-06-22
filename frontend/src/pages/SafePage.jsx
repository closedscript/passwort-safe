import { useState, useEffect, useCallback } from 'react'
import { getEntries, getCategories, createEntry, deleteEntry, updateEntry, createCategory, deleteCategory, updateCategory } from '../services/api'

// Pure function: Passwortgüte prüfen – Modul 323 (keine Seiteneffekte)
const checkPasswordStrength = (pw) => {
  if (!pw) return { score: 0, label: '', color: '' }
  const checks = [pw.length >= 8, /[A-Z]/.test(pw), /[a-z]/.test(pw), /[0-9]/.test(pw), /[^A-Za-z0-9]/.test(pw)]
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

// Pure function: Einträge filtern (keine Seiteneffekte) – Modul 323
const filterEntries = (entries, search, categoryId) =>
  entries
    .filter(e => !categoryId || e.categoryId === categoryId)
    .filter(e => !search || ['title', 'url', 'username', 'email', 'notes']
      .some(f => (e[f] || '').toLowerCase().includes(search.toLowerCase())))

// Pure function: Einträge sortieren (gibt neue Liste zurück) – Modul 323
const sortEntries = (entries, field, asc) => {
  if (!field) return entries
  return [...entries].sort((a, b) => {
    const va = (a[field] || '').toLowerCase()
    const vb = (b[field] || '').toLowerCase()
    return asc ? va.localeCompare(vb) : vb.localeCompare(va)
  })
}

const EMPTY_ENTRY = { title: '', url: '', username: '', password: '', email: '', notes: '', categoryId: '' }

export default function SafePage({ onLogout }) {
  const [entries, setEntries] = useState([])
  const [categories, setCategories] = useState([])
  const [search, setSearch] = useState('')
  const [selectedCategory, setSelectedCategory] = useState(null)
  const [sortField, setSortField] = useState('title')
  const [sortAsc, setSortAsc] = useState(true)
  const [showPasswords, setShowPasswords] = useState({})
  const [showAddEntry, setShowAddEntry] = useState(false)
  const [showAddCategory, setShowAddCategory] = useState(false)
  const [newCategoryName, setNewCategoryName] = useState('')
  const [editingCategory, setEditingCategory] = useState(null)
  const [loading, setLoading] = useState(true)
  const [newEntry, setNewEntry] = useState(EMPTY_ENTRY)
  const [editEntry, setEditEntry] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [e, c] = await Promise.all([getEntries(), getCategories()])
      setEntries(e)
      setCategories(c)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  const handleSort = (field) => {
    if (sortField === field) setSortAsc(v => !v)
    else { setSortField(field); setSortAsc(true) }
  }

  const handleAddEntry = async (e) => {
    e.preventDefault()
    await createEntry({ ...newEntry, categoryId: newEntry.categoryId || null })
    setNewEntry(EMPTY_ENTRY)
    setShowAddEntry(false)
    load()
  }

  const handleEditSubmit = async (e) => {
    e.preventDefault()
    await updateEntry(editEntry.id, { ...editEntry, categoryId: editEntry.categoryId || null })
    setEditEntry(null)
    load()
  }

  const handleDeleteEntry = async (id) => {
    if (!confirm('Eintrag wirklich löschen?')) return
    await deleteEntry(id)
    load()
  }

  const handleAddCategory = async (e) => {
    e.preventDefault()
    if (!newCategoryName.trim()) return
    await createCategory(newCategoryName.trim())
    setNewCategoryName('')
    setShowAddCategory(false)
    load()
  }

  const handleDeleteCategory = async (id) => {
    if (!confirm('Rubrik wirklich löschen?')) return
    await deleteCategory(id)
    if (selectedCategory === id) setSelectedCategory(null)
    load()
  }

  const handleEditCategory = async (id, name) => {
    await updateCategory(id, name)
    setEditingCategory(null)
    load()
  }

  // Higher-Order Functions für die Anzeige (Modul 323)
  const displayed = sortEntries(filterEntries(entries, search, selectedCategory), sortField, sortAsc)

  const SortIcon = ({ field }) => {
    if (sortField !== field) return <span style={{ color: 'var(--muted)', marginLeft: 4 }}>↕</span>
    return <span style={{ color: 'var(--accent)', marginLeft: 4 }}>{sortAsc ? '↑' : '↓'}</span>
  }

  const newStrength = checkPasswordStrength(newEntry.password)
  const editStrength = checkPasswordStrength(editEntry?.password)

  return (
    <div style={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>

      {/* Sidebar */}
      <aside style={{
        width: 220, background: 'var(--bg2)', borderRight: '1px solid var(--border)',
        display: 'flex', flexDirection: 'column', padding: '16px 12px', gap: 4, flexShrink: 0
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16, padding: '0 4px' }}>
          <span style={{ fontSize: 20 }}>🔐</span>
          <span style={{ fontWeight: 600, fontSize: 15 }}>Passwort-Safe</span>
        </div>

        <button
          className={`ghost`}
          onClick={() => setSelectedCategory(null)}
          style={{ textAlign: 'left', justifyContent: 'flex-start', background: selectedCategory === null ? 'var(--bg3)' : 'transparent', borderColor: selectedCategory === null ? 'var(--accent)' : 'transparent' }}
        >
          Alle Einträge ({entries.length})
        </button>

        <div style={{ margin: '8px 0 4px 4px', fontSize: 11, color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
          Rubriken
        </div>

        {categories.map(cat => (
          <div key={cat.id} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
            {editingCategory?.id === cat.id ? (
              <form onSubmit={e => { e.preventDefault(); handleEditCategory(cat.id, editingCategory.name) }} style={{ display: 'flex', gap: 4, flex: 1 }}>
                <input value={editingCategory.name} onChange={e => setEditingCategory({ ...editingCategory, name: e.target.value })} style={{ flex: 1, padding: '4px 8px', fontSize: 13 }} autoFocus />
                <button type="submit" className="primary" style={{ padding: '4px 8px', fontSize: 12 }}>✓</button>
              </form>
            ) : (
              <>
                <button
                  className="ghost"
                  onClick={() => setSelectedCategory(cat.id)}
                  style={{ flex: 1, textAlign: 'left', fontSize: 13, background: selectedCategory === cat.id ? 'var(--bg3)' : 'transparent', borderColor: selectedCategory === cat.id ? 'var(--accent)' : 'transparent', padding: '6px 10px' }}
                >
                  {cat.name}
                </button>
                <button onClick={() => setEditingCategory(cat)} style={{ background: 'none', padding: '4px', color: 'var(--muted)', fontSize: 12 }}>✎</button>
                <button onClick={() => handleDeleteCategory(cat.id)} style={{ background: 'none', padding: '4px', color: 'var(--danger)', fontSize: 12 }}>✕</button>
              </>
            )}
          </div>
        ))}

        {showAddCategory ? (
          <form onSubmit={handleAddCategory} style={{ display: 'flex', flexDirection: 'column', gap: 6, marginTop: 4 }}>
            <input value={newCategoryName} onChange={e => setNewCategoryName(e.target.value)} placeholder="Rubrik-Name" style={{ fontSize: 13, padding: '6px 8px' }} autoFocus />
            <div style={{ display: 'flex', gap: 6 }}>
              <button type="submit" className="primary" style={{ flex: 1, padding: '6px', fontSize: 12 }}>Hinzufügen</button>
              <button type="button" className="ghost" onClick={() => setShowAddCategory(false)} style={{ flex: 1, padding: '6px', fontSize: 12 }}>Abbruch</button>
            </div>
          </form>
        ) : (
          <button className="ghost" onClick={() => setShowAddCategory(true)} style={{ fontSize: 13, marginTop: 4, borderStyle: 'dashed' }}>
            + Rubrik hinzufügen
          </button>
        )}

        <div style={{ flex: 1 }} />
        <button className="ghost" onClick={onLogout} style={{ fontSize: 13, color: 'var(--muted)' }}>Abmelden</button>
      </aside>

      {/* Hauptinhalt */}
      <main style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>

        {/* Toolbar */}
        <div style={{ padding: '14px 20px', borderBottom: '1px solid var(--border)', display: 'flex', gap: 10, alignItems: 'center' }}>
          <input
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Suchen…"
            style={{ maxWidth: 300 }}
          />
          <div style={{ flex: 1 }} />
          <button className="primary" onClick={() => setShowAddEntry(v => !v)}>
            {showAddEntry ? '✕ Abbruch' : '+ Neuer Eintrag'}
          </button>
        </div>

        {/* Add Entry Form */}
        {showAddEntry && (
          <div style={{ padding: '16px 20px', borderBottom: '1px solid var(--border)', background: 'var(--bg2)' }}>
            <form onSubmit={handleAddEntry} style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 10 }}>
              <input required placeholder="Titel *" value={newEntry.title} onChange={e => setNewEntry(p => ({...p, title: e.target.value}))} />
              <input placeholder="URL" value={newEntry.url} onChange={e => setNewEntry(p => ({...p, url: e.target.value}))} />
              <input placeholder="Benutzername" value={newEntry.username} onChange={e => setNewEntry(p => ({...p, username: e.target.value}))} />
              <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                <input required type="password" placeholder="Passwort *" value={newEntry.password} onChange={e => setNewEntry(p => ({...p, password: e.target.value}))} />
                {newEntry.password && (
                  <div>
                    <div style={{ height: 3, background: 'var(--border)', borderRadius: 2, overflow: 'hidden' }}>
                      <div style={{ height: '100%', width: `${(newStrength.score / 5) * 100}%`, background: newStrength.color, transition: 'width 0.3s' }} />
                    </div>
                    <span style={{ fontSize: 10, color: newStrength.color }}>{newStrength.label}</span>
                  </div>
                )}
              </div>
              <input placeholder="E-Mail" value={newEntry.email} onChange={e => setNewEntry(p => ({...p, email: e.target.value}))} />
              <select value={newEntry.categoryId} onChange={e => setNewEntry(p => ({...p, categoryId: e.target.value}))}>
                <option value="">Keine Rubrik</option>
                {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
              <input placeholder="Bemerkungen" value={newEntry.notes} onChange={e => setNewEntry(p => ({...p, notes: e.target.value}))} style={{ gridColumn: '1 / -1' }} />
              <button type="submit" className="primary" style={{ gridColumn: '1 / -1' }}>Speichern</button>
            </form>
          </div>
        )}

        {/* Tabelle */}
        <div style={{ flex: 1, overflow: 'auto', padding: '0 20px 20px' }}>
          {loading ? (
            <div style={{ padding: 40, textAlign: 'center', color: 'var(--muted)' }}>Lädt…</div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 16 }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border)' }}>
                  {[['title', 'Titel'], ['url', 'URL'], ['username', 'Benutzer'], ['categoryName', 'Rubrik']].map(([f, label]) => (
                    <th key={f} onClick={() => handleSort(f)} style={{ textAlign: 'left', padding: '8px 12px', color: 'var(--muted)', cursor: 'pointer', userSelect: 'none', fontWeight: 500, fontSize: 12, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                      {label}<SortIcon field={f} />
                    </th>
                  ))}
                  <th style={{ padding: '8px 12px', color: 'var(--muted)', fontWeight: 500, fontSize: 12, textTransform: 'uppercase' }}>Passwort</th>
                  <th style={{ width: 100 }}></th>
                </tr>
              </thead>
              <tbody>
                {displayed.length === 0 && (
                  <tr><td colSpan={6} style={{ padding: 40, textAlign: 'center', color: 'var(--muted)' }}>
                    Keine Einträge gefunden.
                  </td></tr>
                )}
                {displayed.map(entry => (
                  <tr key={entry.id} style={{ borderBottom: '1px solid var(--border)' }}
                      onMouseEnter={e => e.currentTarget.style.background = 'var(--bg2)'}
                      onMouseLeave={e => e.currentTarget.style.background = 'transparent'}>
                    <td style={{ padding: '10px 12px', fontWeight: 500 }}>{entry.title}</td>
                    <td style={{ padding: '10px 12px', color: 'var(--muted)', fontSize: 13 }}>
                      {entry.url ? <a href={entry.url} target="_blank" rel="noopener" style={{ color: 'var(--accent)', textDecoration: 'none' }}>{entry.url}</a> : '—'}
                    </td>
                    <td style={{ padding: '10px 12px' }}>{entry.username || '—'}</td>
                    <td style={{ padding: '10px 12px' }}>
                      {entry.categoryName
                        ? <span style={{ background: 'var(--bg3)', border: '1px solid var(--border)', borderRadius: 4, padding: '2px 8px', fontSize: 12 }}>{entry.categoryName}</span>
                        : <span style={{ color: 'var(--muted)' }}>—</span>}
                    </td>
                    <td style={{ padding: '10px 12px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontFamily: 'monospace', fontSize: 13 }}>
                          {showPasswords[entry.id] ? entry.password : '••••••••'}
                        </span>
                        <button
                          onClick={() => setShowPasswords(p => ({ ...p, [entry.id]: !p[entry.id] }))}
                          style={{ background: 'none', padding: '2px 6px', color: 'var(--muted)', fontSize: 11 }}>
                          {showPasswords[entry.id] ? 'verbergen' : 'zeigen'}
                        </button>
                        <button
                          onClick={() => navigator.clipboard.writeText(entry.password)}
                          style={{ background: 'none', padding: '2px 6px', color: 'var(--accent)', fontSize: 11 }}>
                          kopieren
                        </button>
                      </div>
                    </td>
                    <td style={{ padding: '10px 12px' }}>
                      <div style={{ display: 'flex', gap: 6 }}>
                        <button
                          onClick={() => setEditEntry({ ...entry, categoryId: entry.categoryId || '' })}
                          style={{ background: 'none', padding: '4px 10px', fontSize: 12, color: 'var(--accent)', border: '1px solid var(--accent)', borderRadius: 4 }}>
                          Bearbeiten
                        </button>
                        <button className="danger" onClick={() => handleDeleteEntry(entry.id)} style={{ padding: '4px 10px', fontSize: 12 }}>
                          Löschen
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </main>

      {/* Edit-Modal */}
      {editEntry && (
        <div style={{
          position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100
        }} onClick={e => { if (e.target === e.currentTarget) setEditEntry(null) }}>
          <div className="card" style={{ width: 520, maxHeight: '90vh', overflow: 'auto' }}>
            <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 16 }}>Eintrag bearbeiten</h2>
            <form onSubmit={handleEditSubmit} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
              <div style={{ gridColumn: '1 / -1' }}>
                <label style={{ display: 'block', marginBottom: 4, fontSize: 12, color: 'var(--muted)' }}>Titel *</label>
                <input required value={editEntry.title} onChange={e => setEditEntry(p => ({...p, title: e.target.value}))} />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: 4, fontSize: 12, color: 'var(--muted)' }}>URL</label>
                <input value={editEntry.url} onChange={e => setEditEntry(p => ({...p, url: e.target.value}))} />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: 4, fontSize: 12, color: 'var(--muted)' }}>Benutzername</label>
                <input value={editEntry.username} onChange={e => setEditEntry(p => ({...p, username: e.target.value}))} />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: 4, fontSize: 12, color: 'var(--muted)' }}>Passwort *</label>
                <input required type="password" value={editEntry.password} onChange={e => setEditEntry(p => ({...p, password: e.target.value}))} />
                {editEntry.password && (
                  <div style={{ marginTop: 4 }}>
                    <div style={{ height: 3, background: 'var(--border)', borderRadius: 2, overflow: 'hidden' }}>
                      <div style={{ height: '100%', width: `${(editStrength.score / 5) * 100}%`, background: editStrength.color, transition: 'width 0.3s' }} />
                    </div>
                    <span style={{ fontSize: 10, color: editStrength.color }}>{editStrength.label}</span>
                  </div>
                )}
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: 4, fontSize: 12, color: 'var(--muted)' }}>E-Mail</label>
                <input value={editEntry.email} onChange={e => setEditEntry(p => ({...p, email: e.target.value}))} />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: 4, fontSize: 12, color: 'var(--muted)' }}>Rubrik</label>
                <select value={editEntry.categoryId} onChange={e => setEditEntry(p => ({...p, categoryId: e.target.value}))}>
                  <option value="">Keine Rubrik</option>
                  {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <div style={{ gridColumn: '1 / -1' }}>
                <label style={{ display: 'block', marginBottom: 4, fontSize: 12, color: 'var(--muted)' }}>Bemerkungen</label>
                <input value={editEntry.notes} onChange={e => setEditEntry(p => ({...p, notes: e.target.value}))} />
              </div>
              <div style={{ gridColumn: '1 / -1', display: 'flex', gap: 8, marginTop: 4 }}>
                <button type="submit" className="primary" style={{ flex: 1 }}>Speichern</button>
                <button type="button" className="ghost" onClick={() => setEditEntry(null)} style={{ flex: 1 }}>Abbrechen</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
