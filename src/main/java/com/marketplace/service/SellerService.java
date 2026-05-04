package com.marketplace.service;

import com.marketplace.model.Seller;
import com.marketplace.model.User;
import com.marketplace.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SellerService {

    @Autowired
    private SellerRepository sellerRepository;

    public Seller createSeller(Seller seller) {
        return sellerRepository.save(seller);
    }

    public Optional<Seller> findById(Long id) {
        return sellerRepository.findById(id);
    }

    public Optional<Seller> findByUser(User user) {
        return sellerRepository.findByUser(user);
    }

    public Optional<Seller> findByUserId(Long userId) {
        return sellerRepository.findByUserId(userId);
    }

    public List<Seller> findAllActive() {
        return sellerRepository.findByActiveTrue();
    }

    public List<Seller> findVerified() {
        return sellerRepository.findByVerifiedTrue();
    }

    public List<Seller> searchByStoreName(String storeName) {
        return sellerRepository.findByStoreNameContainingIgnoreCase(storeName);
    }

    public Seller update(Seller seller) {
        return sellerRepository.save(seller);
    }

    public void delete(Long id) {
        sellerRepository.deleteById(id);
    }
}
