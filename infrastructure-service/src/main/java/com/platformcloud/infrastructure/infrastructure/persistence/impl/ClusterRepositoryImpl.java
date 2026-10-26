package com.platformcloud.infrastructure.infrastructure.persistence.impl;

import com.platformcloud.infrastructure.domain.exception.ValidationException;
import com.platformcloud.infrastructure.domain.model.Cluster;
import com.platformcloud.infrastructure.domain.repository.ClusterRepository;
import com.platformcloud.infrastructure.infrastructure.persistence.entity.ClusterEntity;
import com.platformcloud.infrastructure.infrastructure.persistence.mapper.ClusterMapper;
import com.platformcloud.infrastructure.infrastructure.persistence.repository.ClusterJpaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClusterRepositoryImpl implements ClusterRepository {

    private final ClusterJpaRepository jpaRepository;
    private final ClusterMapper mapper;

    @Override
    @Transactional
    public Cluster save(Cluster cluster) {

        ClusterEntity entity;

        if (cluster.id() == null) {
            entity = mapper.toEntity(cluster);
        } else {
            entity = jpaRepository.findById(cluster.id())
                    .orElseThrow(() ->
                            new ValidationException(
                                    "Cluster not found: " + cluster.id()
                            )
                    );

            entity.update(
                    cluster.name(),
                    cluster.host(),
                    cluster.secretRef(),
                    cluster.enabled()
            );
        }

        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Async
    @Override
    public Optional<Cluster> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Cluster> findAll() {
        return mapper.toDomainList(jpaRepository.findAll());
    }

    @Override
    public Optional<Cluster> findByName(String name) {
        return jpaRepository.findByName(name)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override

    public boolean existsByHost(String host) {
        return jpaRepository.existsByHost(host);
    };

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}