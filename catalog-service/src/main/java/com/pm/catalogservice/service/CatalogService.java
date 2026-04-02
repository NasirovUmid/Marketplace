package com.pm.catalogservice.service;

import com.pm.catalogservice.dto.*;
import com.pm.catalogservice.entity.Catalog;
import com.pm.catalogservice.entity.Ticket;
import com.pm.catalogservice.enums.CatalogStatus;
import com.pm.catalogservice.enums.TicketStatus;
import com.pm.catalogservice.repository.CatalogRepository;
import com.pm.catalogservice.repository.TicketRepository;
import com.pm.commonevents.CatalogNotificationEvent;
import com.pm.commonevents.exception.AlreadyExistsException;
import com.pm.commonevents.exception.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@AllArgsConstructor
@Service
public class CatalogService {

    private final CatalogRepository catalogRepository;
    private final KafkaEventProducer kafkaEventProducer;
    private final TicketService ticketService;

    @Transactional(readOnly = true)
    public Page<CatalogPageResponseDto> getAllCatalogs(Pageable pageable,
                                                       Integer priceFrom,
                                                       Integer priceTo,
                                                       CatalogStatus status,
                                                       Instant dateFrom,
                                                       Instant dateTo) throws BadRequestException {

        if (priceFrom != null && priceTo != null && priceFrom > priceTo) {
            throw new BadRequestException("priceFrom cannot be greater than priceTo");
        }

        return catalogRepository.search(priceFrom, priceTo, status, dateFrom, dateTo, pageable).map(CatalogPageResponseDto::from);

    }

    @Transactional
    public UUID creatingCatalog(CreationRequestDto catalog) {

        if (catalogRepository.existsByTitle(catalog.title()))
            throw new AlreadyExistsException("CATALOG CREATE: Catalog with = [ " + catalog.title() + " ] ");

        Catalog newCatalog = Catalog.builder()
                .title(catalog.title())
                .description(catalog.description())
                .price(catalog.price())
                .creatorId(catalog.creatorId())
                .ticketList(null)
                .status(CatalogStatus.ACTIVE)
                .dateOfEvent(catalog.dateOfEvents())
                .createdAt(Instant.now())
                .build();


        List<Ticket> ticketList = new ArrayList<>();

        for (int i = 1; i <= catalog.numberOfTickets(); i++) {

            ticketList.add(Ticket.builder()
                    .catalog(newCatalog)
                    .status(TicketStatus.AVAILABLE)
                    .build());
        }

        newCatalog.setTicketList(ticketList);
        Catalog savedCatalog = catalogRepository.save(newCatalog);
        kafkaEventProducer.sendCatalogNotification(
                new CatalogNotificationEvent(savedCatalog.getId(), savedCatalog.getTitle(),
                        savedCatalog.getCreatorId(), CatalogStatus.CATALOG_CREATED.name(), savedCatalog.getCreatedAt()));

        return savedCatalog.getId();
    }

    @Transactional(readOnly = true)
    public CatalogResponseDto getCatalog(UUID catalogId) {

        Catalog catalog = getCatalogById(catalogId);

        List<Ticket> tickets = ticketService.availableTickets(catalogId);
        long totalTickets = catalogRepository.totalTicketsNumber(catalog.getId());

        if (tickets.isEmpty()) {
            catalog.setStatus(CatalogStatus.SOLD_OUT);
        }

        catalog.setTicketList(null);

        return new CatalogResponseDto(catalog, ticketService.getTicketList(catalog), totalTickets, tickets.size());
    }

    public List<TicketResponseDto> getCatalogsAvailableTickets(UUID id) {

        return ticketService.availableTickets(id).stream().map(TicketResponseDto::from).toList();

    }

    @Transactional
    public Catalog updateCatalog(UUID id, UpdateRequestDto updateRequestDto) {

        if (id == null || updateRequestDto == null) {
            throw new IllegalArgumentException("Body is null");
        }

        Catalog updatingCatalog = getCatalogById(id);

        if (updateRequestDto.title() != null) {
            updatingCatalog.setTitle(updateRequestDto.title());
        }

        if (updateRequestDto.description() != null) {
            updatingCatalog.setDescription(updateRequestDto.description());
        }

        if (updateRequestDto.price() != null) {
            updatingCatalog.setPrice(updateRequestDto.price());
        }

        if (updateRequestDto.dateOfEvent() != null) {
            updatingCatalog.setDateOfEvent(updateRequestDto.dateOfEvent());
        }

        Catalog catalog = catalogRepository.save(updatingCatalog);

        catalog.setTicketList(null);

        return catalog;

    }

    public void deactivateCatalog(UUID catalogId) {

        Catalog catalog = catalogRepository.findById(catalogId).orElseThrow(() -> new NotFoundException("Catalog for Deactivation "));

        if (!catalog.getStatus().equals(CatalogStatus.ACTIVE)) {
            throw new IllegalStateException("Catalog cannot be Deactivated!");
        }

        catalog.setStatus(CatalogStatus.DEACTIVATED);
        catalogRepository.save(catalog);

    }

    public Catalog getCatalogById(UUID catalogId) {

        Optional<Catalog> catalog = catalogRepository.findCatalogById(catalogId);

        return catalog.orElseThrow(() -> new NotFoundException("GET CATALOG BY ID: Catalog with Id = [ " + catalogId + " ] "));
    }

    public static Sort toCatalogSort(String sort) {
        if (sort == null) return Sort.by("dateOfEvent").ascending();

        String s = sort.trim().toLowerCase();
        if (s.equals("dateofevent,desc")) return Sort.by("dateOfEvent").descending();
        if (s.equals("dateofevent,asc")) return Sort.by("dateOfEvent").ascending();

        if (s.equals("createdat,desc")) return Sort.by("createdAt").descending();
        if (s.equals("createdat,asc")) return Sort.by("createdAt").ascending();

        if (s.equals("price,desc")) return Sort.by("price").descending();
        if (s.equals("price,asc")) return Sort.by("price").ascending();

        return Sort.by("dateOfEvent").ascending();
    }

}
