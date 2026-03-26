package com.pm.catalogservice.repository;

import com.pm.catalogservice.entity.Catalog;
import com.pm.catalogservice.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Optional<List<Ticket>> findTicketsByCatalog(Catalog catalog);

    List<Ticket> findAllByCatalogId(UUID catalogId);

}
