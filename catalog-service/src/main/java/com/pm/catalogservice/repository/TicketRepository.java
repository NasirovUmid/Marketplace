package com.pm.catalogservice.repository;

import com.pm.catalogservice.entity.Catalog;
import com.pm.catalogservice.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket , UUID> {

    List<Ticket> findTicketByCatalog(Catalog catalog);

    List<Ticket> findAllByCatalogId(UUID catalogId);
}
