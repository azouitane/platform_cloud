package com.virtacore.app.mapper;

import com.virtacore.app.dto.request.vm.TemplateRequest;
import com.virtacore.app.dto.response.TemplateResponse;
import com.virtacore.app.dto.response.TemplateSummary;
import com.virtacore.app.entity.vm.Template;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TemplateMapper {
    Template toEntity(TemplateRequest request);

    TemplateResponse toResponse(Template template);

    TemplateSummary toSummaryResponse(Template template);
}
