// Utilitário para gerenciar imagens e placeholders
export const placeholderConfig = {
  getProductImage: (imagePath) => {
    if (!imagePath || imagePath.includes('placeholder')) {
      return '/placeholder-product.svg';
    }
    return imagePath;
  },
  
  handleImageError: (event, fallbackSrc = '/placeholder-product.svg') => {
    console.log('🖼️ Imagem não encontrada, usando placeholder...');
    event.target.src = fallbackSrc;
    event.target.onerror = null; // Prevenir loop infinito
    return true;
  },
  
  // Para imagens da homepage
  getHomeImage: (imageName) => {
    const images = {
      'hero-bg.jpg': '/images/hero-bg.svg',
      'hero-bg.svg': '/images/hero-bg.svg',
      'feature-delivery.jpg': '/images/feature-delivery.svg',
      'feature-delivery.svg': '/images/feature-delivery.svg'
    };
    return images[imageName] || '/placeholder-product.svg';
  },
  
  // Verificar se imagem existe
  imageExists: (url) => {
    return new Promise((resolve) => {
      const img = new Image();
      img.onload = () => resolve(true);
      img.onerror = () => resolve(false);
      img.src = url;
    });
  }
};

export default placeholderConfig;