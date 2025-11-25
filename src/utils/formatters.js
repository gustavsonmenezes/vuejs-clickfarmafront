// Utilitários de formatação para o ClickFarma
export const formatPrice = (price) => {
  if (!price && price !== 0) return 'R$ 0,00'
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL'
  }).format(price)
}

export const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('pt-BR')
}

export const capitalizeFirst = (str) => {
  if (!str) return ''
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase()
}