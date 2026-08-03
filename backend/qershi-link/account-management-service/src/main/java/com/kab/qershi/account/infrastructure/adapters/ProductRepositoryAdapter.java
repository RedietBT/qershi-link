package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.model.AccountProduct;
import com.kab.qershi.account.domain.ports.outbound.ProductRepositoryPort;
import com.kab.qershi.account.infrastructure.persistence.AccountProductEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Infrastructure persistence adapter implementing ProductRepositoryPort.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final SpringDataProductRepository productRepository;

    public ProductRepositoryAdapter(SpringDataProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public AccountProduct save(AccountProduct product) {
        AccountProductEntity entity = toEntity(product);
        AccountProductEntity saved = productRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<AccountProduct> findByProductId(UUID productId) {
        return productRepository.findById(productId).map(this::toDomain);
    }

    @Override
    public Optional<AccountProduct> findByProductCode(String productCode) {
        return productRepository.findByProductCode(productCode).map(this::toDomain);
    }

    @Override
    public List<AccountProduct> findAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountProduct> findAllActiveProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByProductCode(String productCode) {
        return productRepository.existsByProductCode(productCode);
    }

    @Override
    public long countTotalProducts() {
        return productRepository.count();
    }

    private AccountProductEntity toEntity(AccountProduct domain) {
        if (domain == null) return null;
        return new AccountProductEntity(
                domain.getProductId(),
                domain.getProductCode(),
                domain.getProductName(),
                domain.getCategory(),
                domain.getCurrency(),
                domain.getInterestRatePa(),
                domain.getPostingFrequency(),
                domain.getMinOperatingBalance(),
                domain.getMinMonthlyContribution(),
                domain.getTermPeriodMonths(),
                domain.getEarlyWithdrawalPenaltyPct(),
                domain.isActive(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    private AccountProduct toDomain(AccountProductEntity entity) {
        if (entity == null) return null;
        return new AccountProduct(
                entity.getProductId(),
                entity.getProductCode(),
                entity.getProductName(),
                entity.getCategory(),
                entity.getCurrency(),
                entity.getInterestRatePa(),
                entity.getPostingFrequency(),
                entity.getMinOperatingBalance(),
                entity.getMinMonthlyContribution(),
                entity.getTermPeriodMonths(),
                entity.getEarlyWithdrawalPenaltyPct(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
