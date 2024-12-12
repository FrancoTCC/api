package com.apiproject.api.services;

import com.apiproject.api.dto.ProductDTO;
import com.apiproject.api.entity.Category;
import com.apiproject.api.entity.Product;
import com.apiproject.api.exception.ResourceNotFoundException;
import com.apiproject.api.repository.CategoryRepository;
import com.apiproject.api.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // Obtener todos los productos, con nombre opcional y paginación
    public Page<ProductDTO> getAllProducts(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage;

        if (name != null && !name.trim().isEmpty()) {
            productPage = productRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        // Validar si hay productos
        if (productPage.isEmpty()) {
            throw new ResourceNotFoundException("No products found.");
        }

        return productPage.map(this::convertToDTO);
    }

    // Obtener productos por categoría con paginación
    public Page<ProductDTO> getProductsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByCategoryId(categoryId, pageable);

        // Validar si hay productos en la categoría
        if (productPage.isEmpty()) {
            throw new ResourceNotFoundException("No products found in the category.");
        }

        return productPage.map(this::convertToDTO);
    }

    // Obtener un producto por su ID
    public ProductDTO getProductById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid product ID.");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return convertToDTO(product);
    }

    // Crear un nuevo producto
    public ProductDTO createProduct(@Valid ProductDTO productDTO) {
        if (productDTO.getCategoryId() == null || productDTO.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Category ID must be provided.");
        }

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    // Actualizar un producto existente
    public ProductDTO updateProduct(Long id, @Valid ProductDTO productDTO) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid product ID.");
        }

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        existingProduct.setName(productDTO.getName());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStock(productDTO.getStock());
        existingProduct.setCategory(category);

        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDTO(updatedProduct);
    }

    // Eliminar un producto
    public void deleteProduct(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid product ID.");
        }

        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
    }

    // Buscar productos por nombre con paginación
    public Page<ProductDTO> searchProducts(String name, int page, int size) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required for search.");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByNameContainingIgnoreCase(name, pageable);

        if (productPage.isEmpty()) {
            throw new ResourceNotFoundException("No products found with name: " + name);
        }

        return productPage.map(this::convertToDTO);
    }

    // Convertir Producto a ProductDTO
    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .build();
    }
}
