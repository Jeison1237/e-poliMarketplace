package com.marketplace.service;

import com.marketplace.model.Product;
import com.marketplace.model.Seller;
import com.marketplace.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> findAll() {
        return productRepository.findByActiveTrue();
    }

    public List<Product> findFeatured() {
        return productRepository.findByFeaturedTrue();
    }

    public List<Product> findBySeller(Seller seller) {
        return productRepository.findBySeller(seller);
    }

    public List<Product> findBySellerId(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> search(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.searchProducts(keyword, category, minPrice, maxPrice);
    }

    public List<String> findAllCategories() {
        return productRepository.findAllCategories();
    }

    public Product update(Product product) {
        return productRepository.save(product);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}
