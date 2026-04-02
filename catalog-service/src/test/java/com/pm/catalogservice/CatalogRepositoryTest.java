package com.pm.catalogservice;

import com.pm.catalogservice.entity.Catalog;
import com.pm.catalogservice.enums.CatalogStatus;
import com.pm.catalogservice.repository.CatalogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CatalogRepositoryTest {

    @Autowired
    private CatalogRepository catalogRepository;

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
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

    }

    @Test
    @DisplayName("search should return filtered catalogs")
    void search_ShouldReturnFilteredCatalogs() {

        Catalog catalog1 = Catalog.builder()
                .title("history lecture")
                .description("rome history")
                .price(15000D)
                .creatorId(UUID.randomUUID())
                .status(CatalogStatus.ACTIVE)
                .dateOfEvent(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .build();

        Catalog catalog2 = Catalog.builder()
                .title("math lecture")
                .description("algebra")
                .price(5000D)
                .creatorId(UUID.randomUUID())
                .status(CatalogStatus.DEACTIVATED)
                .dateOfEvent(Instant.now().plusSeconds(7200))
                .createdAt(Instant.now())
                .build();

        catalogRepository.save(catalog1);
        catalogRepository.save(catalog2);

        Page<Catalog> result = catalogRepository.search(
                10000,
                20000,
                CatalogStatus.ACTIVE,
                null,
                null,
                PageRequest.of(0, 10)
        );

        System.out.println("RESULT --------------------------"+result.toList());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("history lecture");
    }

}
