package com.kab.qershi.account.domain.ports.outbound;

import com.kab.qershi.account.domain.model.AccountProduct;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for managing dynamic deposit product catalog persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface ProductRepositoryPort {

    AccountProduct save(AccountProduct product);

    Optional<AccountProduct> findByProductId(UUID productId);

    Optional<AccountProduct> findByProductCode(String productCode);

    List<AccountProduct> findAllProducts();

    List<AccountProduct> findAllActiveProducts();

    boolean existsByProductCode(String productCode);

    long countTotalProducts();
}
