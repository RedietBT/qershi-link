package com.kab.qershi.transaction.infrastructure.adapters;

import com.kab.qershi.transaction.domain.model.JournalEntry;
import com.kab.qershi.transaction.domain.model.JournalLine;
import com.kab.qershi.transaction.domain.ports.outbound.JournalRepositoryPort;
import com.kab.qershi.transaction.infrastructure.persistence.JournalEntryEntity;
import com.kab.qershi.transaction.infrastructure.persistence.JournalLineEntity;
import com.kab.qershi.transaction.infrastructure.persistence.SpringDataJournalEntryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Outbound persistence adapter implementing JournalRepositoryPort.
 * Converts JournalEntry & JournalLine domain models to JPA entities and vice-versa.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class JournalRepositoryAdapter implements JournalRepositoryPort {

    private final SpringDataJournalEntryRepository repository;

    public JournalRepositoryAdapter(SpringDataJournalEntryRepository repository) {
        this.repository = repository;
    }

    @Override
    public JournalEntry save(JournalEntry journalEntry) {
        JournalEntryEntity entity = toEntity(journalEntry);
        JournalEntryEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<JournalEntry> findByTransactionRef(String transactionRef) {
        return repository.findByTransactionRef(transactionRef).map(this::toDomain);
    }

    private JournalEntryEntity toEntity(JournalEntry domain) {
        if (domain == null) return null;
        JournalEntryEntity entity = new JournalEntryEntity();
        if (domain.getEntryId() != null && repository.existsById(domain.getEntryId())) {
            entity.setEntryId(domain.getEntryId());
        }
        entity.setTransactionRef(domain.getTransactionRef());
        if (domain.getPostingDate() != null) {
            entity.setPostingDate(domain.getPostingDate());
        }
        entity.setDescription(domain.getDescription());
        if (domain.getCreatedAt() != null) {
            entity.setCreatedAt(domain.getCreatedAt());
        }

        if (domain.getLines() != null) {
            for (JournalLine lineDomain : domain.getLines()) {
                JournalLineEntity lineEntity = new JournalLineEntity();
                entity.addLine(lineEntity);
                lineEntity.setGlAccountCode(lineDomain.getGlAccountCode());
                lineEntity.setEntryType(lineDomain.getEntryType());
                lineEntity.setAmount(lineDomain.getAmount());
                if (lineDomain.getCreatedAt() != null) {
                    lineEntity.setCreatedAt(lineDomain.getCreatedAt());
                }
                entity.addLine(lineEntity);
            }
        }
        return entity;
    }

    private JournalEntry toDomain(JournalEntryEntity entity) {
        if (entity == null) return null;
        JournalEntry domain = new JournalEntry(
                entity.getEntryId(),
                entity.getTransactionRef(),
                entity.getPostingDate(),
                entity.getDescription(),
                entity.getCreatedAt()
        );

        if (entity.getLines() != null) {
            List<JournalLine> domainLines = entity.getLines().stream().map(lineEntity -> new JournalLine(
                    lineEntity.getLineId(),
                    entity.getEntryId(),
                    lineEntity.getGlAccountCode(),
                    lineEntity.getEntryType(),
                    lineEntity.getAmount(),
                    lineEntity.getCreatedAt()
            )).collect(Collectors.toList());
            domain.setLines(domainLines);
        }
        return domain;
    }
}
