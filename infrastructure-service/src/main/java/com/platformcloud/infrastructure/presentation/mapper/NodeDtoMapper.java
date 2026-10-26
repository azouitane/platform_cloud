package com.platformcloud.infrastructure.presentation.mapper;

import com.platformcloud.infrastructure.domain.enums.NodeStatus;
import com.platformcloud.infrastructure.domain.model.Node;
import com.platformcloud.infrastructure.presentation.request.CreateNodeRequest;
import com.platformcloud.infrastructure.presentation.response.NodeResponse;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface NodeDtoMapper {

    default Node toDomain(CreateNodeRequest request, UUID clusterId) {
        if (request == null) {
            return null;
        }

        return new Node(
                null,
                clusterId,
                request.name(),
                NodeStatus.UNKNOWN,
                null
        );
    }

    NodeResponse toResponse(Node node);

    List<NodeResponse> toResponseList(List<Node> nodes);
}