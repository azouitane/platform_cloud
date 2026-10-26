package com.platformcloud.infrastructure.application.service.impl;

import com.platformcloud.infrastructure.application.port.ProxmoxGateway;
import com.platformcloud.infrastructure.application.service.ClusterService;
import com.platformcloud.infrastructure.domain.exception.ValidationException;
import com.platformcloud.infrastructure.domain.model.Cluster;
import com.platformcloud.infrastructure.domain.repository.ClusterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClusterServiceImpl implements ClusterService {

    private final ClusterRepository rep;
    private final ProxmoxGateway proxmox;

    @Override
    @Transactional
    public Cluster create(Cluster cluster) {

        if (rep.existsByName(cluster.name())) {
            throw new ValidationException(
                    "Cluster name already exists: " + cluster.name()
            );
        }
        if (rep.existsByHost(cluster.host())) {
            throw new  ValidationException(
              "Cluster host already existe: " + cluster.host()
            );
        }

        Cluster newCluster = new Cluster(
                null,
                cluster.name(),
                cluster.host(),
                cluster.port(),
                cluster.secretRef(),
                true,
                null
        );

        return rep.save(newCluster);
    }

    @Override
    public Cluster findById(UUID id) {
        return rep.findById(id)
                .orElseThrow(() ->
                        new ValidationException(
                                "Cluster not found: " + id
                        )
                );
    }

    @Override
    public List<Cluster> findAll() {
        return rep.findAll();
    }

    @Override
    @Transactional
    public Cluster update(Cluster cluster) {

        Cluster existing = findById(cluster.id());

        rep.findByName(cluster.name())
                .filter(found -> !found.id().equals(existing.id()))
                .ifPresent(found -> {
                    throw new ValidationException(
                            "Cluster name already exists: " + cluster.name()
                    );
                });


        Cluster updated = new Cluster(
                existing.id(),
                cluster.name(),
                cluster.host(),
                existing.port(),
                cluster.secretRef(),
                cluster.enabled(),
                existing.createdAt()
        );

        return rep.save(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {

        findById(id);

        rep.deleteById(id);
    }

    @Override
    public boolean verifyConnection(UUID id) {
        Cluster cluster = findById(id);
        return proxmox.verifyConnection(cluster);
    }
}