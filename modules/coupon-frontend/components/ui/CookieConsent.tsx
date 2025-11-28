'use client'

import { useThemeCookie } from '@/lib/theme-provider-cookie'
import { X } from 'lucide-react'

export function CookieConsent() {
  const { cookiesAccepted, acceptCookies } = useThemeCookie()

  if (cookiesAccepted) return null

  return (
    <div className="fixed bottom-0 left-0 right-0 z-50 bg-card border-t shadow-lg p-4">
      <div className="container mx-auto flex items-center justify-between gap-4">
        <div className="flex-1">
          <p className="text-sm">
            We use cookies to store your theme preference and improve your experience.
            By clicking "Accept", you consent to our use of cookies.
          </p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={acceptCookies}
            className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors text-sm font-medium"
          >
            Accept
          </button>
        </div>
      </div>
    </div>
  )
}

