package com.platformcloud.infrastructure.infrastructure.persistence.mapper;

import com.platformcloud.infrastructure.domain.model.Node;
import com.platformcloud.infrastructure.infrastructure.persistence.entity.NodeEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NodeMapper {

    default Node toDomain(NodeEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Node(
                entity.getId(),
                entity.getCluster().getId(),
                entity.getName(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    List<Node> toDomainList(List<NodeEntity> entities);
}