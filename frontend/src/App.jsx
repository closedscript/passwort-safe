import { useState, useEffect } from 'react'
import LoginPage from './pages/LoginPage'
import SafePage from './pages/SafePage'

export default function App() {
  const [token, setToken] = useState(() => localStorage.getItem('token'))

  const handleLogin = (tok, masterPassword) => {
    localStorage.setItem('token', tok)
    sessionStorage.setItem('masterPassword', masterPassword)
    setToken(tok)
  }

  const handleLogout = () => {
    localStorage.removeItem('token')
    sessionStorage.removeItem('masterPassword')
    setToken(null)
  }

  return token
    ? <SafePage onLogout={handleLogout} />
    : <LoginPage onLogin={handleLogin} />
}
