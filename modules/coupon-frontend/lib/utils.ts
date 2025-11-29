import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatDate(date: string | Date): string {
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

export function formatRelativeTime(date: string | Date): string {
  const now = new Date()
  const target = new Date(date)
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

