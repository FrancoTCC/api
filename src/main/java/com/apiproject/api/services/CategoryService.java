package com.apiproject.api.services;

import com.apiproject.api.dto.CategoryDTO;
import com.apiproject.api.entity.Category;
import com.apiproject.api.exception.ResourceNotFoundException;
import com.apiproject.api.repository.CategoryRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Obtener todas las categorías con nombre opcional y paginación
    public Page<CategoryDTO> getAllCategories(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categoryPage;

        if (name != null && !name.trim().isEmpty()) {
            categoryPage = categoryRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            categoryPage = categoryRepository.findAll(pageable);
        }

        // Validar si hay categorías
        if (categoryPage.isEmpty()) {
            throw new ResourceNotFoundException("No categories found.");
        }

        return categoryPage.map(this::convertToDTO);
    }

    // Obtener una categoría por su ID
    public CategoryDTO getCategoryById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid category ID.");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return convertToDTO(category);
    }

    // Crear una nueva categoría
    public CategoryDTO createCategory(@Valid CategoryDTO categoryDTO) {
        if (categoryDTO.getName() == null || categoryDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }

        Category category = convertToEntity(categoryDTO);
        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    // Actualizar una categoría existente
    public CategoryDTO updateCategory(Long id, @Valid CategoryDTO categoryDTO) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid category ID.");
        }

        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        existingCategory.setName(categoryDTO.getName());
        Category updatedCategory = categoryRepository.save(existingCategory);
        return convertToDTO(updatedCategory);
    }

    // Eliminar una categoría
    public void deleteCategory(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid category ID.");
        }

        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with ID: " + id);
        }
        categoryRepository.deleteById(id);
    }

    // Convertir Category a CategoryDTO
    private CategoryDTO convertToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    // Convertir CategoryDTO a Category
    private Category convertToEntity(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setId(categoryDTO.getId());
        category.setName(categoryDTO.getName());
        return category;
    }
}
