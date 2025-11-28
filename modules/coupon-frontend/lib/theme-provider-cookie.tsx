'use client'

/**
 * Alternative theme provider using cookies instead of local storage.
 * This requires cookie consent from the user.
 * 
 * To use this instead of theme-provider.tsx:
 * 1. Replace ThemeProvider import in layout.tsx
 * 2. Add CookieConsent component to layout
 * 3. Install js-cookie: npm install js-cookie @types/js-cookie
 */

import { createContext, useContext, useEffect, useState } from 'react'

type Theme = 'light' | 'dark'

interface ThemeContextType {
  theme: Theme
  toggleTheme: () => void
  cookiesAccepted: boolean
  acceptCookies: () => void
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined)

function getCookie(name: string): string | null {
  if (typeof document === 'undefined') return null
  const value = `; ${document.cookie}`
  const parts = value.split(`; ${name}=`)
  if (parts.length === 2) return parts.pop()?.split(';').shift() || null
  return null
}

function setCookie(name: string, value: string, days: number = 365) {
  if (typeof document === 'undefined') return
  const date = new Date()
  date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000)
  document.cookie = `${name}=${value};expires=${date.toUTCString()};path=/;SameSite=Lax`
}

export function ThemeProviderCookie({ children }: { children: React.ReactNode }) {
  const [theme, setTheme] = useState<Theme>('light')
  const [mounted, setMounted] = useState(false)
  const [cookiesAccepted, setCookiesAccepted] = useState(false)

  useEffect(() => {
    setMounted(true)
    // Check if cookies are accepted
    const cookieConsent = getCookie('cookie-consent')
    if (cookieConsent === 'accepted') {
      setCookiesAccepted(true)
      // Get theme from cookie
      const storedTheme = getCookie('theme') as Theme | null
      if (storedTheme) {
        setTheme(storedTheme)
        document.documentElement.classList.toggle('dark', storedTheme === 'dark')
      } else {
        // Check system preference
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
        const initialTheme = prefersDark ? 'dark' : 'light'
        setTheme(initialTheme)
        document.documentElement.classList.toggle('dark', initialTheme === 'dark')
        setCookie('theme', initialTheme)
      }
    }
  }, [])

  const acceptCookies = () => {
    setCookie('cookie-consent', 'accepted', 365)
    setCookiesAccepted(true)
    // Set initial theme
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    const initialTheme = prefersDark ? 'dark' : 'light'
    setTheme(initialTheme)
    document.documentElement.classList.toggle('dark', initialTheme === 'dark')
    setCookie('theme', initialTheme)
  }

  const toggleTheme = () => {
    if (!cookiesAccepted) return
    const newTheme = theme === 'light' ? 'dark' : 'light'
    setTheme(newTheme)
    setCookie('theme', newTheme)
    document.documentElement.classList.toggle('dark', newTheme === 'dark')
  }

  if (!mounted) {
    return <>{children}</>
  }

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme, cookiesAccepted, acceptCookies }}>
      {children}
    </ThemeContext.Provider>
  )
}

export function useThemeCookie() {
  const context = useContext(ThemeContext)
  if (context === undefined) {
    throw new Error('useThemeCookie must be used within a ThemeProviderCookie')
  }
  return context
}

