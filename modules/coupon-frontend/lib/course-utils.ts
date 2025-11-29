export const categoryColors: Record<string, string> = {
  'Business': '#3498db',
  'Development': '#2ecc71',
  'Finance & Accounting': '#f1c40f',
  'IT & Software': '#e74c3c',
  'Office Productivity': '#9b59b6',
  'Personal Development': '#1abc9c',
  'Design': '#34495e',
  'Marketing': '#e67e22',
  'Lifestyle': '#95a5a6',
  'Photography & Video': '#16a085',
  'Health & Fitness': '#d35400',
  'Music': '#8e44ad',
  'Teaching & Academics': '#2c3e50',
}

export const levelEmojis: Record<string, string> = {
  'All Levels': '🌟',
  'Beginner': '🐣',
  'Intermediate': '🚀',
  'Expert': '🏆',
}

export const categoryIcons: Record<string, string> = {
  'Business': 'briefcase',
  'Development': 'code',
  'Finance & Accounting': 'chart-line',
  'IT & Software': 'laptop-code',
  'Office Productivity': 'file-text',
  'Personal Development': 'user',
  'Design': 'palette',
  'Marketing': 'megaphone',
  'Lifestyle': 'heart',
  'Photography & Video': 'camera',
  'Health & Fitness': 'dumbbell',
  'Music': 'music',
  'Teaching & Academics': 'graduation-cap',
}

export function getCategoryColor(category: string): string {
  return categoryColors[category] || '#bdc3c7'
}

export function getLevelEmoji(level: string): string {
  return levelEmojis[level] || '📚'
}

export function getCategoryIcon(category: string): string {
  return categoryIcons[category] || 'book'
}

