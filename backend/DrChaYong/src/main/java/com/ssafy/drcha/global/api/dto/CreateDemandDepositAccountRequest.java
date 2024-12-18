package com.ssafy.drcha.global.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssafy.drcha.global.api.header.HeaderRequest;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateDemandDepositAccountRequest {
    @JsonProperty("Header")
    private HeaderRequest headerRequest;

    private String accountTypeUniqueNo;
}
