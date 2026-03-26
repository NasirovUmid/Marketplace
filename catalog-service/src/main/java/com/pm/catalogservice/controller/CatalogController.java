package com.pm.catalogservice.controller;

import com.pm.catalogservice.dto.CatalogPageResponseDto;
import com.pm.catalogservice.dto.CatalogResponseDto;
import com.pm.catalogservice.dto.CreationRequestDto;
import com.pm.catalogservice.dto.UpdateRequestDto;
import com.pm.catalogservice.entity.Catalog;
import com.pm.catalogservice.entity.Ticket;
import com.pm.catalogservice.enums.CatalogStatus;
import com.pm.catalogservice.service.CatalogService;
import com.pm.commonevents.exception.ApiProblem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/catalogs")
@AllArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @Operation(summary = "Page of Catalogs", description = "Returns page of catalogs with tickets")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returns page of 20/50 catalogs",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected Error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiProblem.class)))
    })
    @GetMapping()
    public Page<CatalogPageResponseDto> getCatalogs(@Parameter(description = "Page of Catalogs , size = 20 ", example = "1") @RequestParam(defaultValue = "0", name = "page") int page,
                                                    @RequestParam(defaultValue = "dateOfEvent,asc", name = "sort") String sort,
                                                    @RequestParam(required = false, name = "priceFrom") Integer priceFrom,
                                                    @RequestParam(required = false, name = "priceTo") Integer priceTo,
                                                    @RequestParam(required = false, name = "status") CatalogStatus status,
                                                    @RequestParam(required = false, name = "dateFrom") Instant dateFrom,
                                                    @RequestParam(required = false, name = "dateTo") Instant dateTo) {

        Pageable pageable = PageRequest.of(Math.max(page, 0), 20, toCatalogSort(sort));
        return catalogService.getAllCatalogs(pageable, priceFrom, priceTo, status, dateFrom, dateTo);
    }


    @Operation(summary = "Create Catalog", description = "Creates new Catalog")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catalog created and Returns its ID",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "409", description = "conflict with existing data",
                    content = @Content(mediaType = "application/problem + json",
                            schema = @Schema(implementation = ApiProblem.class)))
    })
    @PostMapping()
    public UUID creatingCatalog(@RequestBody CreationRequestDto catalog) {

        UUID catalogId = catalogService.creatingCatalog(catalog);

        return catalogId;

    }

    @Operation(summary = "Returns Catalog", description = "Returns catalog without tickets list but number of Available and total of them ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returns exact Catalog with given id ",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CatalogResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Catalog was not found",
                    content = @Content(mediaType = "application/problem + json",
                            schema = @Schema(implementation = ApiProblem.class)))
    })
    @GetMapping("/{id}")
    public CatalogResponseDto getCatalogDetails(@PathVariable(name = "id") UUID id) {

        return catalogService.getCatalog(id);

    }

    @Operation(summary = "List of Tickets", description = "Returns List of Available Tickets of a exact Catalog")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all available tickets",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "404", description = "Catalog was found",
                    content = @Content(mediaType = "application/problem + json",
                            schema = @Schema(implementation = ApiProblem.class)))
    })
    @GetMapping("/{id}/tickets")
    public List<Ticket> getNumberOfAvailableTickets(@PathVariable UUID id) {

        return catalogService.getCatalogsAvailableTickets(id);

    }

    @Operation(summary = "Updates Catalog", description = "Updates existing Catalog by ID ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catalog successfully updated",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Catalog was not found")
    })
    @PutMapping("{id}")
    public ResponseEntity<Catalog> updateCatalog(@PathVariable UUID id, @RequestBody UpdateRequestDto catalog) {

        Catalog updatedCatalog = catalogService.updateCatalog(id, catalog);

        return ResponseEntity.ok().body(updatedCatalog);

    }

}
