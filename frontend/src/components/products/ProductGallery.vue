<template>
  <div class="product-gallery">
    <div class="main-image mb-3">
      <img :src="mainImage" :alt="product.name" class="img-fluid rounded" style="max-height: 400px; width: 100%; object-fit: cover;">
    </div>
    
    <div class="thumbnail-grid" v-if="product.images && product.images.length > 1">
      <div 
        v-for="(image, index) in product.images" 
        :key="index" 
        class="thumbnail"
        :class="{ active: currentImageIndex === index }"
        @click="currentImageIndex = index"
      >
        <img :src="image" :alt="`${product.name} - Imagem ${index + 1}`" class="img-thumbnail">
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProductGallery',
  props: {
    product: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      currentImageIndex: 0
    }
  },
  computed: {
    mainImage() {
      if (this.product.images && this.product.images.length > 0) {
        return this.product.images[this.currentImageIndex]
      }
      return this.getDefaultImage()
    }
  },
  methods: {
    getDefaultImage() {
      // Imagem padrão baseada na categoria
      const categoryImages = {
        'Medicamentos': 'https://via.placeholder.com/400x400/007bff/ffffff?text=Medicamento',
        'Cosméticos': 'https://via.placeholder.com/400x400/28a745/ffffff?text=Cosmético',
        'Higiene': 'https://via.placeholder.com/400x400/17a2b8/ffffff?text=Higiene',
        'Vitaminas': 'https://via.placeholder.com/400x400/ffc107/000000?text=Vitamina',
        'Maternidade': 'https://via.placeholder.com/400x400/e83e8c/ffffff?text=Maternidade'
      }
      return categoryImages[this.product.category] || 'https://via.placeholder.com/400x400/6c757d/ffffff?text=Produto'
    }
  }
}
</script>

<style scoped>
.thumbnail-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.thumbnail {
  cursor: pointer;
  transition: transform 0.2s;
}

.thumbnail:hover {
  transform: scale(1.05);
}

.thumbnail.active .img-thumbnail {
  border-color: #0d6efd;
  border-width: 2px;
}

.img-thumbnail {
  width: 100%;
  height: 80px;
  object-fit: cover;
}
</style>