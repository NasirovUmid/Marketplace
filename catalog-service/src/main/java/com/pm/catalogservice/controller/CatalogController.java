package com.pm.catalogservice.controller;

import com.pm.catalogservice.entity.Catalog;
import com.pm.catalogservice.service.CatalogService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/catalog/")
@AllArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;


    @GetMapping("catalogs")
    public ResponseEntity<List<Catalog>> getCatalogs(){

        return ResponseEntity.ok().body(catalogService.getAllCatalogs());

    }

    @GetMapping("getCategory/{id}")
    public ResponseEntity<Catalog> getNumberOfAvailableTickets(@PathVariable UUID id){

    return ResponseEntity.ok().body(catalogService.getCatalog(id));
    }

    @PostMapping("newCatalog")
    public ResponseEntity<Catalog> creatingCatalog(@RequestBody Catalog catalog){

        boolean isSuccess = catalogService.creatingCatalog(catalog);

        return isSuccess ? ResponseEntity.ok().body(catalog) : ResponseEntity.status(HttpStatus.valueOf("already exists maybe")).build();

    }

    @PutMapping("updateCatalog")
    public ResponseEntity<Catalog> updateCatalog(@RequestBody Catalog catalog){

    Catalog updatedCatalog = catalogService.updateCatalog(catalog);

    if (updatedCatalog == null) return ResponseEntity.badRequest().body(catalog);

    return ResponseEntity.ok().body(updatedCatalog);
            
    }

}
