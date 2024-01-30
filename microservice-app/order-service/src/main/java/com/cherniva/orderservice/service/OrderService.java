package com.cherniva.orderservice.service;

import com.cherniva.orderservice.dto.OrderRequest;
import com.cherniva.orderservice.model.Order;
import com.cherniva.orderservice.model.OrderLineItems;
import com.cherniva.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private OrderRepository orderRepository;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream()
                .map(oliDto -> OrderLineItems.builder()
                        .id(oliDto.getId())
                        .skuCode(oliDto.getSkuCode())
                        .price(oliDto.getPrice())
                        .quantity(oliDto.getQuantity())
                        .build()
                )
                .toList();

        order.setOrderLineItemsList(orderLineItems);
        orderRepository.save(order);
        log.info("Order {} saved", order.getId());
    }
}
