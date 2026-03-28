package com.example.ordersystem.inventory.controller;

import com.example.ordersystem.inventory.api.dto.CreateProductRequest;
import com.example.ordersystem.inventory.api.dto.ProductResponse;
import com.example.ordersystem.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog API")
public class ProductController {

  private final InventoryService inventoryService;

  @GetMapping
  @Operation(
      summary = "Get products",
      description = "Retrieve a list of products with optional category filter")
  public Page<ProductResponse> getProducts(
      @RequestParam(required = false) String category,
      @ParameterObject
          @PageableDefault(size = 20, sort = "productName", direction = Sort.Direction.ASC)
          Pageable pageable) {
    return inventoryService.getProducts(category, pageable);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get product by ID", description = "Retrieve product details by ID")
  public ProductResponse getProduct(@PathVariable UUID id) {
    return inventoryService.getProduct(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create product", description = "Create a new product (Admin only)")
  public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
    return inventoryService.createProduct(request);
  }
}
