package com.bkplatform.dto;

import lombok.Data;

@Data
public class CheckoutResponse {
    private Long orderId;
    private String status = "OK";
}
