package com.pm.catalogservice.service;

import com.pm.catalogservice.entity.Catalog;
import com.pm.catalogservice.entity.Ticket;
import com.pm.catalogservice.enums.Status;
import com.pm.catalogservice.enums.TicketStatus;
import com.pm.catalogservice.repository.CatalogRepository;
import com.pm.catalogservice.repository.TicketRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class CatalogService {

    private final CatalogRepository catalogRepository;
    private final TicketRepository ticketRepository;

    public List<Catalog> getAllCatalogs(){

       return catalogRepository.findAll();

    }

    public boolean creatingCatalog(@NonNull Catalog catalog){

        if (catalogRepository.existsById(catalog.getId()))return false;


       Catalog newCatalog =  catalogRepository.save(Catalog.builder().
                title(catalog.getTitle()).
                description(catalog.getDescription())
                .price(catalog.getPrice())
                .creatorId(catalog.getCreatorId())
                .numberOfTickets(catalog.getNumberOfTickets())
                .status(Status.ACTIVE)
                .dateOfEvent(catalog.getDateOfEvent())
                .build());


       //generate available tickets without buyersId
        for (int i = 1; i <= newCatalog.getNumberOfTickets(); i++) {
            ticketRepository.save(Ticket.builder()
                    .catalog(newCatalog)
                    .status(TicketStatus.AVAILABLE)
                    .buyerId(null)
                    .build());
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
            catalog.setStatus(Status.FULL);
            catalog.setNumberOfTickets(0);
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
