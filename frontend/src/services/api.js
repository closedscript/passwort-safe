import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

// JWT automatisch anhängen
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  const mp = sessionStorage.getItem('masterPassword')
  if (mp) config.headers['X-Master-Password'] = mp
  return config
})

// ── Auth ──────────────────────────────────────────────────────────────────────

export const login = (username, password) =>
  api.post('/auth/login', { username, password }).then(r => r.data)

// ── Categories ────────────────────────────────────────────────────────────────

export const getCategories = () =>
  api.get('/categories').then(r => r.data)

export const createCategory = (name) =>
  api.post('/categories', { name }).then(r => r.data)

export const updateCategory = (id, name) =>
  api.put(`/categories/${id}`, { name }).then(r => r.data)

export const deleteCategory = (id) =>
  api.delete(`/categories/${id}`)

// ── Entries ───────────────────────────────────────────────────────────────────

export const getEntries = ({ categoryId, search, sortField, ascending } = {}) => {
  const params = {}
  if (categoryId) params.categoryId = categoryId
  if (search) params.search = search
  if (sortField) params.sortField = sortField
  if (ascending !== undefined) params.ascending = ascending
  return api.get('/entries', { params }).then(r => r.data)
}

export const createEntry = (data) =>
  api.post('/entries', data).then(r => r.data)

export const deleteEntry = (id) =>
  api.delete(`/entries/${id}`)
