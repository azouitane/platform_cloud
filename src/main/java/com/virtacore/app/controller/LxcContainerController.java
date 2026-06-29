package com.virtacore.app.controller;

import com.virtacore.app.dto.request.lxc.CreateLxcRequest;
import com.virtacore.app.dto.response.LxcResponse;
import com.virtacore.app.dto.response.LxcSummaryResponse;
import com.virtacore.app.security.CustomUserDetails;
import com.virtacore.app.service.LxcContainerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lxc")
@RequiredArgsConstructor
public class LxcContainerController {
    private final LxcContainerService lxcService;



    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LxcResponse create(
            @Valid @RequestBody CreateLxcRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return lxcService.create(
                request,
                userDetails.getId()
        );
    }


    @GetMapping
    public List<LxcSummaryResponse> findAll() {

        return lxcService.findAll();
    }



    @GetMapping("/{id}")
    public LxcResponse findById(
            @PathVariable UUID id
    ) {

        return lxcService.findById(id);
    }



    @GetMapping("/vmid/{lxcId}")
    public LxcResponse findByVmId(
            @PathVariable Long lxcId
    ) {

        return lxcService.findByVmId(lxcId);
    }



    @PostMapping("/{Id}/start")
    public void start(
            @PathVariable UUID Id
    ) {

        lxcService.start(Id);
    }



    @PostMapping("/{Id}/stop")
    public void stop(
            @PathVariable UUID Id
    ) {

        lxcService.stop(Id);
    }



    @PostMapping("/{Id}/restart")
    public void restart(
            @PathVariable UUID Id
    ) {

        lxcService.restart(Id);
    }



    @DeleteMapping("/{Id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID Id
    ) {
        lxcService.delete(Id);
    }

    @PostMapping("/{Id}/shutdown")
    @ResponseStatus(HttpStatus.OK)
    public void shutdown(
            @PathVariable UUID Id
    ) {
        lxcService.shutdown(Id);
    }
}
