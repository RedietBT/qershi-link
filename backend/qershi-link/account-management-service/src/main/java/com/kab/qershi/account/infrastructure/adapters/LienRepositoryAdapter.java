package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.model.AccountLien;
import com.kab.qershi.account.domain.model.LienStatus;
import com.kab.qershi.account.domain.ports.outbound.LienRepositoryPort;
import com.kab.qershi.account.infrastructure.persistence.AccountLienEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataLienRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Infrastructure persistence adapter implementing LienRepositoryPort.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class LienRepositoryAdapter implements LienRepositoryPort {

    private final SpringDataLienRepository lienRepository;

    public LienRepositoryAdapter(SpringDataLienRepository lienRepository) {
        this.lienRepository = lienRepository;
    }

    @Override
    public AccountLien save(AccountLien lien) {
        AccountLienEntity entity = toEntity(lien);
        AccountLienEntity saved = lienRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<AccountLien> findByLienId(UUID lienId) {
        return lienRepository.findById(lienId).map(this::toDomain);
    }

    @Override
    public List<AccountLien> findByAccountNo(String accountNo) {
        return lienRepository.findByAccountNo(accountNo).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountLien> findActiveLiensByAccountNo(String accountNo) {
        return lienRepository.findByAccountNoAndStatus(accountNo, LienStatus.ACTIVE).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private AccountLienEntity toEntity(AccountLien domain) {
        if (domain == null) return null;
        return new AccountLienEntity(
                domain.getLienId(),
                domain.getAccountNo(),
                domain.getLienAmount(),
                domain.getReason(),
                domain.getReferenceNo(),
                domain.getPlacedByUserId(),
                domain.getReleasedByUserId(),
                domain.getStatus(),
                domain.getPlacedAt(),
                domain.getReleasedAt()
        );
    }

    private AccountLien toDomain(AccountLienEntity entity) {
        if (entity == null) return null;
        return new AccountLien(
                entity.getLienId(),
                entity.getAccountNo(),
                entity.getLienAmount(),
                entity.getReason(),
                entity.getReferenceNo(),
                entity.getPlacedByUserId(),
                entity.getReleasedByUserId(),
                entity.getStatus(),
                entity.getPlacedAt(),
                entity.getReleasedAt()
        );
    }
}
