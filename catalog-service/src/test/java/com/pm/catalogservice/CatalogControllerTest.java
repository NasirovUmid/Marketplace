package com.pm.catalogservice;

import com.pm.catalogservice.controller.CatalogController;
import com.pm.catalogservice.dto.CatalogResponseDto;
import com.pm.catalogservice.dto.CreationRequestDto;
import com.pm.catalogservice.dto.UpdateRequestDto;
import com.pm.catalogservice.entity.Catalog;
import com.pm.catalogservice.enums.CatalogStatus;
import com.pm.catalogservice.exception.GlobalExceptionHandler;
import com.pm.catalogservice.service.CatalogService;
import com.pm.commonevents.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CatalogController.class)
@Import(GlobalExceptionHandler.class)
public class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CatalogService catalogService;

    @Test
    void creatingCatalog_ShouldReturn201_WhenRequestIsValid() throws Exception {

        CreationRequestDto creationRequestDto = new CreationRequestDto(
                "history lecture",
                "rome history",
                15000D,
                UUID.randomUUID(),
                10L,
                Instant.now().plusSeconds(3600)
        );

        UUID generatedId = UUID.randomUUID();

        when(catalogService.creatingCatalog(creationRequestDto)).thenReturn(generatedId);

        mockMvc.perform(post("/catalogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().string("\"" + generatedId + "\""));
    }

    @Test
    void getCatalogDetails_ShouldReturn404_WhenCatalogDoesNotExist() throws Exception {

        UUID id = UUID.randomUUID();

        when(catalogService.getCatalog(id)).thenThrow(new NotFoundException("Catalog with ID :" + id + " not found"));

        mockMvc.perform(get("/catalogs/{id}", id))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("not_found_exception"));
    }

    @Test
    void updateCatalog_ShouldReturn200_WhenRequestIsValid() throws Exception {

        UUID id = UUID.randomUUID();

        UpdateRequestDto request = new UpdateRequestDto(
                "updated title",
                "updated description",
                20000D,
                Instant.now().plusSeconds(7200)
        );

        Catalog updated = new Catalog(
                id,
                "updated title",
                "updated description",
                20000D,
                UUID.randomUUID(),
                null,
                CatalogStatus.ACTIVE,
                Instant.now().plusSeconds(7200),
                Instant.now()
        );

        when(catalogService.updateCatalog(id, request)).thenReturn(updated);

        mockMvc.perform(put("/catalogs/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("updated title"))
                .andExpect(jsonPath("$.price").value(20000));

    }

    @Test
    void deactivateCatalog_ShouldReturn204_WhenCatalogIsDeactivated() throws Exception {

        UUID id = UUID.randomUUID();

        doNothing().when(catalogService).deactivateCatalog(id);

        mockMvc.perform(patch("/catalogs/{id}/deactivate", id))
                .andDo(print())
                .andExpect(status().isNoContent());

    }
}
