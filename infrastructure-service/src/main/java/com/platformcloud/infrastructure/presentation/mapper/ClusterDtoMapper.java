package com.platformcloud.infrastructure.presentation.mapper;

import com.platformcloud.infrastructure.domain.model.Cluster;
import com.platformcloud.infrastructure.presentation.request.CreateClusterRequest;
import com.platformcloud.infrastructure.presentation.request.UpdateClusterRequest;
import com.platformcloud.infrastructure.presentation.response.ClusterResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ClusterDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    Cluster toDomain(CreateClusterRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "createdAt", ignore = true)
    Cluster toDomain(UpdateClusterRequest request, UUID id);

    ClusterResponse toResponse(Cluster cluster);

    List<ClusterResponse> toResponseList(List<Cluster> clusters);
}