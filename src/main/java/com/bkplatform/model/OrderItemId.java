package com.bkplatform.model;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class OrderItemId implements Serializable {
    private Long order;
    private Long product;
}
