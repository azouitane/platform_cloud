package com.virtacore.app.mapper;

import com.virtacore.app.dto.request.lxc.CreateLxcRequest;
import com.virtacore.app.dto.response.LxcResponse;
import com.virtacore.app.entity.lxc.LxcContainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LxcContainerMapper {
    LxcContainer toEntity(
            CreateLxcRequest createLxcRequest
    );


    @Mapping(source = "lxcId", target = "lxcId")
    @Mapping(target = "node", source = "proxmoxNode.name")
    @Mapping(target = "cpu", source = "cores")
    @Mapping(target = "clusterName", source = "cluster.name")
    @Mapping(target = "os", source = "template.distribution")
    @Mapping(target = "version", source = "template.version")
    LxcResponse toResponse(
            LxcContainer lxcContainer
    );
}

