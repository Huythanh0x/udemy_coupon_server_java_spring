import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

function normalizeToDate(value: string | number | Date): Date {
  if (value instanceof Date) return value

  if (typeof value === 'number') {
    // Heuristic:
    // - epoch seconds ~ 1e9
    // - epoch millis ~ 1e12
    const ms = value < 1e12 ? value * 1000 : value
    return new Date(ms)
  }

  // value is string
  const trimmed = value.trim()
  const maybeNum = Number(trimmed)
  // If it's a numeric string, treat it like the numeric case (seconds vs millis)
  if (trimmed.length > 0 && !Number.isNaN(maybeNum) && /^[0-9]+(\.[0-9]+)?$/.test(trimmed)) {
    const ms = maybeNum < 1e12 ? maybeNum * 1000 : maybeNum
    return new Date(ms)
  }

  // Otherwise assume ISO date string / RFC string
  return new Date(value)
}

export function formatDate(date: string | number | Date): string {
  const d = normalizeToDate(date)
  return d.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

export function formatRelativeTime(date: string | number | Date): string {
  const now = new Date()
  const target = normalizeToDate(date)
  const diffInSeconds = Math.floor((target.getTime() - now.getTime()) / 1000)

  // Future dates (for expiry)
  if (diffInSeconds > 0) {
    if (diffInSeconds < 60) return 'in a moment'
    if (diffInSeconds < 3600) return `in ${Math.floor(diffInSeconds / 60)} minutes`
    if (diffInSeconds < 86400) return `in ${Math.floor(diffInSeconds / 3600)} hours`
    if (diffInSeconds < 604800) return `in ${Math.floor(diffInSeconds / 86400)} days`
    if (diffInSeconds < 2592000) return `in ${Math.floor(diffInSeconds / 86400)} days`
    return `in ${Math.floor(diffInSeconds / 2592000)} months`
  }

  // Past dates
  const absDiff = Math.abs(diffInSeconds)
  if (absDiff < 60) return 'just now'
  if (absDiff < 3600) return `${Math.floor(absDiff / 60)} minutes ago`
  if (absDiff < 86400) return `${Math.floor(absDiff / 3600)} hours ago`
  if (absDiff < 604800) return `${Math.floor(absDiff / 86400)} days ago`
  return formatDate(date)
}

