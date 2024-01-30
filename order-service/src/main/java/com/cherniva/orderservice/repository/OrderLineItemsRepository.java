package com.cherniva.orderservice.repository;

import com.cherniva.orderservice.model.OrderLineItems;
import org.springframework.data.repository.CrudRepository;

public interface OrderLineItemsRepository extends CrudRepository<OrderLineItems, Long> {
}
