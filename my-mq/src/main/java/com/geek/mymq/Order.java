package com.geek.mymq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单费用
     */
    private Integer price;
}
