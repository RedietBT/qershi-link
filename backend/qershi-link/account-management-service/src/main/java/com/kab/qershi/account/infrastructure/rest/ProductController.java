package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.domain.model.AccountProduct;
import com.kab.qershi.account.domain.ports.inbound.ProductManagementUseCase;
import com.kab.qershi.account.infrastructure.rest.dto.CreateProductRequest;
import com.kab.qershi.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller exposing Dynamic SACCO Deposit Product Factory APIs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/accounts/products")
@Tag(name = "Deposit Product Factory", description = "Endpoints for configuring dynamic SACCO deposit products (FLEXCUBE / Temenos Standard).")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductManagementUseCase productManagementUseCase;

    public ProductController(ProductManagementUseCase productManagementUseCase) {
        this.productManagementUseCase = productManagementUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('PRODUCT_CREATE')")
    @Operation(summary = "Create Dynamic Deposit Product", description = "Defines a new deposit product and auto-assigns a unique 3-digit product code (e.g. 101, 102).")
    public ResponseEntity<ApiResponse<AccountProduct>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        AccountProduct product = productManagementUseCase.createProduct(
                request.getProductName(),
                request.getCategory(),
                request.getCurrency(),
                request.getInterestRatePa(),
                request.getPostingFrequency(),
                request.getMinOperatingBalance(),
                request.getMinMonthlyContribution(),
                request.getTermPeriodMonths(),
                request.getEarlyWithdrawalPenaltyPct()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(product, "Deposit product created successfully with product code: " + product.getProductCode()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('PRODUCT_VIEW')")
    @Operation(summary = "Get All Deposit Products", description = "Retrieves all deposit products configured for the active SACCO.")
    public ResponseEntity<ApiResponse<List<AccountProduct>>> getAllProducts() {
        List<AccountProduct> products = productManagementUseCase.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success(products, "Retrieved " + products.size() + " deposit products."));
    }

    @GetMapping("/{productCode}")
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('PRODUCT_VIEW')")
    @Operation(summary = "Get Product Details", description = "Retrieves deposit product rules by its 3-digit product code.")
    public ResponseEntity<ApiResponse<AccountProduct>> getProductByCode(@PathVariable String productCode) {
        AccountProduct product = productManagementUseCase.getProductByCode(productCode);
        return ResponseEntity.ok(ApiResponse.success(product, "Deposit product retrieved successfully."));
    }
}
