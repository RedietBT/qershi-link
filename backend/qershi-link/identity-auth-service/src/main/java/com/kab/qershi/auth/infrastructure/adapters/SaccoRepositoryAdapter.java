package com.kab.qershi.auth.infrastructure.adapters;

import com.kab.qershi.auth.domain.model.Sacco;
import com.kab.qershi.auth.domain.model.SaccoStatus;
import com.kab.qershi.auth.domain.ports.outbound.SaccoRepositoryPort;
import com.kab.qershi.auth.infrastructure.persistence.SaccoEntity;
import com.kab.qershi.auth.infrastructure.persistence.SpringDataSaccoRepository;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;


@Component
public class SaccoRepositoryAdapter implements SaccoRepositoryPort {


    private final SpringDataSaccoRepository repository;


    public SaccoRepositoryAdapter(SpringDataSaccoRepository repository) {
        this.repository = repository;
    }


    @Override
    public Sacco save(Sacco sacco) {

        SaccoEntity entity = mapToEntity(sacco);

        SaccoEntity saved = repository.save(entity);

        return mapToDomain(saved);
    }


    @Override
    public Optional<Sacco> findById(UUID saccoId) {

        return repository.findById(saccoId)
                .map(this::mapToDomain);
    }


    @Override
    public boolean existsBySaccoName(String saccoName) {

        return repository.existsBySaccoName(saccoName);
    }


    @Override
    public boolean existsBySchemaName(String schemaName) {

        return repository.existsBySchemaName(schemaName);
    }



    private SaccoEntity mapToEntity(Sacco domain) {

        SaccoEntity entity = new SaccoEntity();


        entity.setSaccoId(domain.getSaccoId());

        entity.setParentUnionId(domain.getParentUnionId());

        entity.setSaccoName(domain.getSaccoName());

        entity.setSchemaName(domain.getSchemaName());

        entity.setUnion(domain.isUnion());

        entity.setMinShareRequirement(
                domain.getMinShareRequirement()
        );


        entity.setStatus(
                domain.getStatus()
        );


        entity.setCreatedAt(
                domain.getCreatedAt()
        );


        entity.setUpdatedAt(
                domain.getUpdatedAt()
        );


        return entity;
    }



    private Sacco mapToDomain(SaccoEntity entity) {


        Sacco sacco = new Sacco(
                entity.getSaccoId(),
                entity.getSaccoName(),
                entity.getSchemaName(),
                entity.isUnion(),
                entity.getMinShareRequirement()
        );


        if(entity.getParentUnionId()!=null){
            sacco.attachToUnion(entity.getParentUnionId());
        }


        if(entity.getStatus()==SaccoStatus.ACTIVE){
            sacco.activate();
        }


        return sacco;
    }

}