package com.virtacore.app.mapper;

import com.virtacore.app.dto.request.lxc.LxcTemplateRequest;
import com.virtacore.app.dto.response.LxcTemplateResponse;
import com.virtacore.app.entity.lxc.LxcTemplate;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LxcTemplateMapper {
    LxcTemplate toEntity(LxcTemplateRequest lxcTemplateRequest);

    LxcTemplateResponse toResponse(LxcTemplate lxcTemplate);
}
