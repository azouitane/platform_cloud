package com.virtacore.app.mapper;

import com.virtacore.app.dto.request.lxc.CreateLxcRequest;
import com.virtacore.app.dto.response.LxcResponse;
import com.virtacore.app.entity.lxc.LxcContainer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LxcContaineMapper {
    LxcContainer toEntity(CreateLxcRequest createLxcRequest);

    LxcResponse toResponse(LxcContainer lxcContainer);
}

