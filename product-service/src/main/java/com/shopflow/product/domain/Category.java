package com.shopflow.product.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A product category. Names are unique (case-insensitive lookups happen in the service).
 * Lombok policy for entities: no @Data / @EqualsAndHashCode (L04).
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    public Category(String name) {
        this.name = name;
    }

    /**
     * Identity-based equality, safe for JPA entities:
     * - two categories are equal only if both have been saved and share an id;
     * - uses getId() (not the field) so Hibernate lazy proxies compare correctly;
     * - constant hashCode, so the hash never changes when the id is assigned (the L04 HashSet bug).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Category other)) {
            return false;
        }
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Category.class.hashCode();
    }
}
