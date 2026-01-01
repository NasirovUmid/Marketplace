package com.pm.catalogservice.repository;

import com.pm.catalogservice.entity.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CatalogRepository extends JpaRepository<Catalog, UUID> {

    //Catalog finCatalogById(UUID id);
}
