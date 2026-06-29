package com.virtacore.app.dto.response;


import java.util.Map;

public record ProxmoxResponse<T>(
        T data
) {
}
