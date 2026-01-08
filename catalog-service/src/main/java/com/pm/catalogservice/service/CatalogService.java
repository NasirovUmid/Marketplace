package com.pm.catalogservice.service;

import com.pm.catalogservice.entity.Catalog;
import com.pm.commonevents.CatalogNotificationEvent;
import com.pm.catalogservice.dto.CreationRequestDto;
import com.pm.catalogservice.entity.Ticket;
import com.pm.catalogservice.enums.Status;
import com.pm.commonevents.enums.CatalogStatus;
import com.pm.commonevents.enums.TicketStatus;
import com.pm.catalogservice.repository.CatalogRepository;
import com.pm.catalogservice.repository.TicketRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class CatalogService {

    private final CatalogRepository catalogRepository;
    private final TicketRepository ticketRepository;
    private final KafkaEventProducer kafkaEventProducer;
    private final Logger logger = LoggerFactory.getLogger(CatalogService.class);

    public List<Catalog> getAllCatalogs(){

       return catalogRepository.findAll();

    }

    public boolean creatingCatalog(CreationRequestDto catalog){

        // IF YOU CHANGED CODE (REMOVED CODE OR ADDED CONFIGURATIONS)ALWAYS REBUILD(docker compose up -d --build {name/or without - all})
        //OTHERWISE OLD CODE WILL RUIN YOUR LIFE AS YOUR EXES

    try {

    Catalog newCatalog = catalogRepository.save(Catalog.builder().
            title(catalog.title()).
            description(catalog.description())
            .price(catalog.price())
            .creatorId(catalog.creatorId())
            .numberOfTickets(catalog.numberOfTickets())
            .status(CatalogStatus.ACTIVE)
            .dateOfEvent(catalog.dateOfEvents())
            .createdAt(Instant.now())
            .build());


    kafkaEventProducer.sendCatalogNotification(
            new CatalogNotificationEvent(newCatalog.getId(), newCatalog.getTitle(),
                    newCatalog.getCreatorId(), CatalogStatus.CATALOG_CREATED.name(), newCatalog.getCreatedAt()));


    //generate available tickets without buyersId
    for (int i = 1; i <= newCatalog.getNumberOfTickets(); i++) {
        ticketRepository.save(Ticket.builder()
                .catalog(newCatalog)
                .status(TicketStatus.AVAILABLE)
                .buyerId(null)
                .build());
    }
      }  catch (Exception argumentException){

      argumentException.printStackTrace();
      logger.error("catalog = [ {} ] , problem = [ {} ]",argumentException,catalog);
        return false;

      }
        return true;
    }

    public Catalog getCatalog(UUID catalogId){

        //couldnt unite them but maybe its more understandable this way

        Catalog catalog = catalogRepository.findById(catalogId).get();

        //finding tickets of catalog
        List<Ticket> tickets = ticketRepository.findTicketByCatalog(catalog)
                .stream().filter(ticket -> ticket.getCatalog().equals(catalog)).toList();


        //counting available ones and returning
        Long amount = tickets.stream().
                filter(ticket -> ticket.getStatus().equals(TicketStatus.AVAILABLE)).count();

        if (amount<1) {
            catalog.setStatus(CatalogStatus.SOLD_OUT);
            catalog.setNumberOfTickets(null);
        }
        catalog.setNumberOfTickets(amount);

        return catalog;
    }

    public Catalog updateCatalog(Catalog catalog){

        Catalog updatingCatalog = catalogRepository.findById(catalog.getId()).get();

        if (updatingCatalog == null) return null;


        updatingCatalog = Catalog.builder()
                .title(catalog.getTitle())
                .description(catalog.getDescription())
                .price(catalog.getPrice())
                .status(catalog.getStatus())
                .dateOfEvent(catalog.getDateOfEvent())
                .build();

        return catalogRepository.save(updatingCatalog);

    }

    public void deleteCatalog(UUID catalogId){

        catalogRepository.deleteById(catalogId);

    }



}
