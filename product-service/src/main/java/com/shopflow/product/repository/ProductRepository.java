package com.shopflow.product.repository;

import com.shopflow.product.domain.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data generates the implementation at startup.
 * Listing methods fetch the category in the SAME query (@EntityGraph) to avoid N+1.
 */
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /** Overrides the inherited method only to add the fetch plan. */
    @Override
    @EntityGraph(attributePaths = "category")
    List<Product> findAll(Sort sort);

    /** Dynamic filters (ProductSpecifications) + category fetched in the same query. */
    @Override
    @EntityGraph(attributePaths = "category")
    List<Product> findAll(Specification<Product> spec, Sort sort);

    /** Derived query: WHERE upper(category.name) = upper(?) ORDER BY id ASC */
    @EntityGraph(attributePaths = "category")
    List<Product> findByCategoryNameIgnoreCaseOrderByIdAsc(String categoryName);

    /**
     * Strategy A (L10 main lesson): load and lock the row until the transaction ends.
     * Deliberately WITHOUT an entity graph: combining row locks with outer-join fetches has
     * database-specific restrictions; one extra lazy category load here is cheap.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Product> findWithLockById(Long id);

    /**
     * Strategy B (L10 exercise): apply the delta in ONE atomic UPDATE, only if stock stays non-negative.
     * Returns the number of rows updated (1 = applied, 0 = not found or insufficient stock).
     * clearAutomatically: entities loaded earlier in this transaction would be stale after a bulk UPDATE.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Product p
               set p.stockQuantity = p.stockQuantity + :delta
             where p.id = :id
               and p.stockQuantity + :delta >= 0
            """)
    int adjustStockAtomically(@Param("id") Long id, @Param("delta") int delta);
}
