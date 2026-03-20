package com.pm.catalogservice.repository;

import com.pm.catalogservice.entity.Catalog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CatalogRepository extends JpaRepository<Catalog, UUID> {

    @Query("""
            select c from Catalog c
            where (:status is null or c.status = :status)
              and (:priceFrom is null or c.price >= :priceFrom)
              and (:priceTo is null or c.price <= :priceTo)
              and (:dateFrom is null or c.dateOfEvent >= :dateFrom)
              and (:dateTo is null or c.dateOfEvent <= :dateTo)
            """)
    Page<Catalog> search(
            Integer priceFrom,
            Integer priceTo,
            String status,
            Instant dateFrom,
            Instant dateTo,
            Pageable pageable
    );

    boolean existsByTitle(String title);

    @Query(value = """
            SELECT COUNT(*) from Ticket t where t.catalog.id =:catalogId""")
    long totalTicketsNumber(@Param("catalogId") UUID catalogId);

    Optional<Catalog> findCatalogById(UUID catalogId);
}
