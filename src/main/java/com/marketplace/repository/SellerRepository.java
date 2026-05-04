package com.marketplace.repository;

import com.marketplace.model.Seller;
import com.marketplace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
    Optional<Seller> findByUser(User user);
    Optional<Seller> findByUserId(Long userId);
    List<Seller> findByActiveTrue();
    List<Seller> findByVerifiedTrue();
    List<Seller> findByStoreNameContainingIgnoreCase(String storeName);
}
