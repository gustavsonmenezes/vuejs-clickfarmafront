package com.clickfarma.backend.dto;

import java.util.List;

/**
 * DTO para requisição de análise de carrinho pela IA
 * Contém os itens do carrinho para análise inteligente
 */
public class CartAnalysisRequestDTO {
    private List<CartItemDTO> items;
    private Double totalPrice;

    public CartAnalysisRequestDTO() {
    }

    public CartAnalysisRequestDTO(List<CartItemDTO> items, Double totalPrice) {
        this.items = items;
        this.totalPrice = totalPrice;
    }

    public List<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    /**
     * DTO interno para representar um item do carrinho
     */
    public static class CartItemDTO {
        private Long id;
        private String name;
        private String description;
        private Double price;
        private Integer quantity;
        private String category;

        public CartItemDTO() {
        }

        public CartItemDTO(Long id, String name, String description, Double price, Integer quantity, String category) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.quantity = quantity;
            this.category = category;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }
}
