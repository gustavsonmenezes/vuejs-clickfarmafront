// Utilitários de formatação para o ClickFarma
export const formatPrice = (price) => {
  if (!price && price !== 0) return 'R$ 0,00'
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL'
  }).format(price).replace(/\u00a0/g, ' ')
}

export const formatDate = (date) => {
  if (!date) return ''
  if (typeof date === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(date)) {
    const [y, m, d] = date.split('-')
    return `${d}/${m}/${y}`
  }
  const d = new Date(date)
  return d.toLocaleDateString('pt-BR')
}

export const capitalizeFirst = (str) => {
  if (!str) return ''
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase()
}