package com.marketplace.repository;

import com.marketplace.model.Order;
import com.marketplace.model.Seller;
import com.marketplace.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(Order.Status status);

    @Query("select distinct o from Order o join o.items i where i.product.seller = :seller order by o.createdAt desc")
    List<Order> findBySellerOrderByCreatedAtDesc(@Param("seller") Seller seller);
}
