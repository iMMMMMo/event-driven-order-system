package com.example.ordersystem.inventory.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaOrderItemLookupRepository implements OrderItemLookupRepository {

  private final EntityManager entityManager;

  @Override
  public List<OrderItemRow> findByOrderId(UUID orderId) {
    Query query =
        entityManager.createNativeQuery(
            "SELECT product_id, quantity FROM order_items WHERE order_id = :orderId");
    query.setParameter("orderId", orderId);

    @SuppressWarnings("unchecked")
    List<Object[]> rows = query.getResultList();

    List<OrderItemRow> result = new ArrayList<>(rows.size());
    for (Object[] row : rows) {
      UUID productId = toUuid(row[0]);
      int quantity = ((Number) row[1]).intValue();
      result.add(new OrderItemRow(productId, quantity));
    }

    return result;
  }

  private static UUID toUuid(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof UUID uuid) {
      return uuid;
    }
    return UUID.fromString(value.toString());
  }
}
