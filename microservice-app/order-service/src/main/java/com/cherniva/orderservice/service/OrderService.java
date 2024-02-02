package com.cherniva.orderservice.service;

import com.cherniva.orderservice.dto.InventoryResponse;
import com.cherniva.orderservice.dto.OrderRequest;
import com.cherniva.orderservice.model.Order;
import com.cherniva.orderservice.model.OrderLineItems;
import com.cherniva.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public String placeOrder(OrderRequest orderRequest) {
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

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        // check if order line items are in inventory stock
        InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCodes", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductsInStock = Arrays.stream(inventoryResponseArray)
                .allMatch(InventoryResponse::isInStock);

        if (allProductsInStock) {
            orderRepository.save(order);
            log.info("Order {} saved", order.getId());
            return "Order places successfully";
        } else {
            log.error("Product is not in stock");
            throw new IllegalArgumentException("Product is not in stock");
        }
    }
}
