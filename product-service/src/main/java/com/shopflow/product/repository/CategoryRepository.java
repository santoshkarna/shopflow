package com.shopflow.product.repository;

import com.shopflow.product.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Derived query: WHERE upper(name) = upper(?) */
    Optional<Category> findByNameIgnoreCase(String name);
}