package com.apiproject.api.controller;

import com.apiproject.api.dto.CategoryDTO;
import com.apiproject.api.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public Page<CategoryDTO> getAllCategories(
            @RequestParam(required = false) String name,  // Búsqueda opcional por nombre
            @RequestParam int page,
            @RequestParam int size) {

        // Llamamos al servicio para obtener las categorías con paginación y búsqueda
        return categoryService.getAllCategories(name, page, size);
    }

    @GetMapping("/category/{id}")
    public CategoryDTO getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @PostMapping("/category")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDTO createCategory(@RequestBody @Valid CategoryDTO categoryDTO) {
        return categoryService.createCategory(categoryDTO);
    }

    @PutMapping("/category/{id}")
    public CategoryDTO updateCategory(@PathVariable Long id, @RequestBody @Valid CategoryDTO categoryDTO) {
        return categoryService.updateCategory(id, categoryDTO);
    }

    @DeleteMapping("/category/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }
}
