package com.platformcloud.infrastructure.infrastructure.persistence.mapper;

import com.platformcloud.infrastructure.domain.model.Cluster;
import com.platformcloud.infrastructure.infrastructure.persistence.entity.ClusterEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClusterMapper {

    default Cluster toDomain(ClusterEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Cluster(
                entity.getId(),
                entity.getName(),
                entity.getHost(),
                entity.getPort(),
                entity.getSecretRef(),
                entity.isEnabled(),
                entity.getCreatedAt()
        );
    }

    default ClusterEntity toEntity(Cluster cluster) {
        if (cluster == null) {
            return null;
        }

        return new ClusterEntity(
                cluster.name(),
                cluster.host(),
                cluster.port(),
                cluster.secretRef()
        );
    }

    List<Cluster> toDomainList(List<ClusterEntity> entities);
}