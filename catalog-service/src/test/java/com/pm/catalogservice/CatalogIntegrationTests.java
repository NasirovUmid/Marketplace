package com.pm.catalogservice;

import com.pm.catalogservice.dto.CreationRequestDto;
import com.pm.catalogservice.entity.Catalog;
import com.pm.catalogservice.enums.CatalogStatus;
import com.pm.catalogservice.repository.CatalogRepository;
import com.pm.catalogservice.service.KafkaEventProducer;
import com.pm.commonevents.CatalogNotificationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CatalogIntegrationTests {

    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        if (!postgreSQLContainer.isRunning()) {
            postgreSQLContainer.start();
        }
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgreSQLContainer::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

    }

    @Autowired
    private CatalogRepository catalogRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KafkaEventProducer kafkaEventProducer;

    @Autowired
    private

    @Test
    @DisplayName("POST /catalogs should save and send notification to kafka")
    void createCatalog_ShouldSaveCatalogAndSendNotification() throws Exception {

        UUID creatorId = UUID.randomUUID();


        CreationRequestDto request = new CreationRequestDto(
                "history lecture",
                "rome history",
                15000D,
                creatorId,
                10L,
                Instant.now().plusSeconds(3600)
        );

        mockMvc.perform(post("/catalogs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Catalog savedCatalog = catalogRepository.findAll().getFirst();

        System.out.println("SAVED CATALOG ======================================= "+savedCatalog.getTitle()+" "+savedCatalog.getStatus());

        assertThat(savedCatalog).isNotNull();
        assertThat(savedCatalog.getTitle()).isEqualTo("history lecture");
        assertThat(savedCatalog.getDescription()).isEqualTo("rome history");
        assertThat(savedCatalog.getPrice()).isEqualTo(15000D);
        assertThat(savedCatalog.getCreatorId()).isEqualTo(request.creatorId());

        ArgumentCaptor<CatalogNotificationEvent> captor = ArgumentCaptor.forClass(CatalogNotificationEvent.class);

        verify(kafkaEventProducer).sendCatalogNotification(captor.capture());

        CatalogNotificationEvent sentEvent = captor.getValue();

        System.out.println("NOTIFICATION EVENT  ==============="+sentEvent);

        assertThat(sentEvent).isNotNull();
        assertThat(sentEvent.catalogId()).isEqualTo(savedCatalog.getId());
        assertThat(sentEvent.catalogName()).isEqualTo(savedCatalog.getTitle());
        assertThat(sentEvent.creatorId()).isEqualTo(savedCatalog.getCreatorId());
        assertThat(sentEvent.catalogStatus()).isEqualTo(CatalogStatus.CATALOG_CREATED.name());

    }

}
