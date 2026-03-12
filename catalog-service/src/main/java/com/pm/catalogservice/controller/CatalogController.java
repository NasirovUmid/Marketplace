package com.pm.catalogservice.controller;

import com.pm.catalogservice.dto.CatalogResponseDto;
import com.pm.catalogservice.dto.CreationRequestDto;
import com.pm.catalogservice.entity.Catalog;
import com.pm.catalogservice.entity.Ticket;
import com.pm.catalogservice.service.CatalogService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.pm.catalogservice.service.CatalogService.toCatalogSort;

@RestController
@RequestMapping("/catalog/")
@AllArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("catalogs")
    public Page<Catalog> getCatalogs(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "dateOfEvent,asc") String sort,
                                     @RequestParam(required = false) Integer priceFrom,
                                     @RequestParam(required = false) Integer priceTo,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) Instant dateFrom,
                                     @RequestParam(required = false) Instant dateTo) {

        Pageable pageable = PageRequest.of(Math.max(page, 0), 20, toCatalogSort(sort));
        return catalogService.getAllCatalogs(pageable, priceFrom, priceTo, status, dateFrom, dateTo);
    }


    @PostMapping("newCatalog")
    public ResponseEntity<Void> creatingCatalog(@RequestBody CreationRequestDto catalog) {

        boolean isSaved = catalogService.creatingCatalog(catalog);

        return isSaved ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();

    }

    @GetMapping("catalog/{id}")
    public CatalogResponseDto getCatalogDetails(@PathVariable UUID id) {

        return catalogService.getCatalog(id);

    }

    @GetMapping("catalog/{id}/tickets")
    public List<Ticket> getNumberOfAvailableTickets(@PathVariable UUID id) {

        return catalogService.getCatalogsAvailableTickets(id);

    }

    @PutMapping("catalog")
    public ResponseEntity<Catalog> updateCatalog(@RequestBody Catalog catalog) {

        Catalog updatedCatalog = catalogService.updateCatalog(catalog);

        return updatedCatalog != null ? ResponseEntity.ok().body(updatedCatalog) : ResponseEntity.badRequest().body(catalog);

    }

}
