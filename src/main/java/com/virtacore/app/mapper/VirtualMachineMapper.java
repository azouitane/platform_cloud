package com.virtacore.app.mapper;

import com.virtacore.app.dto.response.VirtualMachineResponse;
import com.virtacore.app.entity.vm.VirtualMachine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VirtualMachineMapper {
    @Mapping(source = "cluster.id", target = "clusterId")
    @Mapping(source = "template.id", target = "templateId")
    @Mapping(source = "owner.id", target = "ownerId")
    VirtualMachineResponse toResponse(VirtualMachine vm);
}
