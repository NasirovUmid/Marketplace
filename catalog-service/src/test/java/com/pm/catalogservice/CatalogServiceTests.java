package com.pm.catalogservice;

import com.pm.catalogservice.dto.*;
import com.pm.catalogservice.entity.Catalog;
import com.pm.catalogservice.entity.Ticket;
import com.pm.catalogservice.enums.CatalogStatus;
import com.pm.catalogservice.enums.TicketStatus;
import com.pm.catalogservice.repository.CatalogRepository;
import com.pm.catalogservice.service.CatalogService;
import com.pm.catalogservice.service.KafkaEventProducer;
import com.pm.catalogservice.service.TicketService;
import com.pm.commonevents.CatalogNotificationEvent;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CatalogServiceTests {

    @Mock
    private CatalogRepository catalogRepository;

    @Mock
    private KafkaEventProducer kafkaTemplate;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private CatalogService catalogService;


    @Test
    void testing_CreatingCatalog() {

        CreationRequestDto creationRequestDto = new CreationRequestDto(
                "history lecture",
                "rome history",
                15000D,
                UUID.randomUUID(),
                10L,
                Instant.now().plusSeconds(60)
        );

        UUID generatedId = UUID.randomUUID();

        System.out.println("Generated id 1: " + generatedId);

        Catalog catalog = Catalog.builder()
                .id(generatedId)
                .title(creationRequestDto.title())
                .description(creationRequestDto.description())
                .price(creationRequestDto.price())
                .creatorId(creationRequestDto.creatorId())
                .status(CatalogStatus.ACTIVE)
                .dateOfEvent(creationRequestDto.dateOfEvents())
                .createdAt(Instant.now())
                .build();

        catalog.setTicketList(List.of(new Ticket(UUID.randomUUID(), catalog, TicketStatus.AVAILABLE, UUID.randomUUID())));

        when(catalogRepository.save(any(Catalog.class))).thenReturn(catalog);

        UUID catalogId = catalogService.creatingCatalog(creationRequestDto);

        System.out.println("Generated id 2: " + catalogId);

        ArgumentCaptor<Catalog> argumentCaptor = ArgumentCaptor.forClass(Catalog.class);
        verify(catalogRepository).save(argumentCaptor.capture());
        verify(kafkaTemplate).sendCatalogNotification(any(CatalogNotificationEvent.class));

        Catalog capturedCatalog = argumentCaptor.getValue();

        assertThat(capturedCatalog.getTitle()).isEqualTo("history lecture");
        assertThat(capturedCatalog.getDescription()).isEqualTo("rome history");
        assertThat(capturedCatalog.getPrice()).isEqualTo(15000D);
        assertThat(capturedCatalog.getStatus()).isEqualTo(CatalogStatus.ACTIVE);

        assertThat(catalogId).isNotNull();
        assertThat(catalogId).isEqualTo(generatedId);

    }

    @Test
    void testing_GetCatalogs() throws BadRequestException {

        Catalog catalog = Catalog.builder()
                .title("history")
                .description("history lecture")
                .price(15000D)
                .creatorId(UUID.randomUUID())
                .status(CatalogStatus.ACTIVE)
                .dateOfEvent(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .build();

        catalog.setTicketList(List.of(new Ticket(UUID.randomUUID(), catalog, TicketStatus.AVAILABLE, UUID.randomUUID())));

        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());

        Page<Catalog> page = new PageImpl<>(List.of(catalog), pageable, 1);

        when(catalogRepository.search(100000,
                200000,
                CatalogStatus.ACTIVE,
                null,
                null,
                pageable)).thenReturn(page);

        Page<CatalogPageResponseDto> result = catalogService.getAllCatalogs(
                pageable,
                100000,
                200000,
                CatalogStatus.ACTIVE,
                null,
                null
        );

        System.out.println(result.getContent());
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("history");
        assertThat(result.getContent().get(0).price()).isEqualTo(15000);

        verify(catalogRepository).search(
                100000,
                200000,
                CatalogStatus.ACTIVE,
                null,
                null,
                pageable

        );
    }

    @Test
    void testing_GetCatalogById() {

        UUID catalogId = UUID.randomUUID();

        Catalog catalog = Catalog.builder()
                .id(catalogId)
                .title("history")
                .description("history lecture")
                .price(15000D)
                .creatorId(UUID.randomUUID())
                .status(CatalogStatus.ACTIVE)
                .dateOfEvent(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .build();

        catalog.setTicketList(List.of(new Ticket(UUID.randomUUID(), catalog, TicketStatus.AVAILABLE, UUID.randomUUID())));

        when(catalogRepository.findCatalogById(catalogId)).thenReturn(Optional.of(catalog));
        when(ticketService.getTicketList(catalog)).thenReturn(catalog.getTicketList().stream().map(TicketResponseDto::from).toList());

        CatalogResponseDto newCatalog = catalogService.getCatalog(catalogId);
        verify(catalogRepository).findCatalogById(catalogId);

        assertThat(newCatalog).isNotNull();
        assertThat(newCatalog.catalog().getId()).isEqualTo(catalogId);
        assertThat(newCatalog.catalog().getTitle()).isEqualTo(catalog.getTitle());
        assertThat(newCatalog.catalog().getDescription()).isEqualTo(catalog.getDescription());
        assertThat(newCatalog.catalog().getStatus()).isEqualTo(catalog.getStatus());
        assertThat(newCatalog.catalog().getPrice()).isEqualTo(catalog.getPrice());
        assertThat(newCatalog.catalog().getCreatorId()).isEqualTo(catalog.getCreatorId());

        System.out.println(newCatalog.catalog() + " " + newCatalog.ticketList());

    }

    @Test
    void testing_UpdateCatalog() {

        UUID catalogId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        Instant dateOfEvent = Instant.now().plusSeconds(360000);
        Instant createdAt = Instant.now();

        Catalog oldCatalog = Catalog.builder()
                .id(catalogId)
                .title("history")
                .description("history lecture")
                .price(15000D)
                .creatorId(creatorId)
                .status(CatalogStatus.ACTIVE)
                .dateOfEvent(Instant.now())
                .createdAt(createdAt)
                .build();

        oldCatalog.setTicketList(List.of(new Ticket(UUID.randomUUID(), oldCatalog, TicketStatus.AVAILABLE, UUID.randomUUID())));
        System.out.println("The old Catalog - " + oldCatalog);

        UpdateRequestDto updateRequestDto = UpdateRequestDto.builder()
                .title("rome architecture")
                .description("features and role of Great Rome`s architecture")
                .price(37500D)
                .dateOfEvent(dateOfEvent)
                .build();


        when(catalogRepository.findCatalogById(catalogId)).thenReturn(Optional.of(oldCatalog));
        when(catalogRepository.save(any(Catalog.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        Catalog catalog = catalogService.getCatalogById(oldCatalog.getId());

        System.out.println("The old catalog from db : " + catalog);

        Catalog updatedCatalog = catalogService.updateCatalog(catalogId, updateRequestDto);

        System.out.println("New Catalog : " + updatedCatalog);

        assertThat(updatedCatalog).isNotNull();
        assertThat(updatedCatalog.getTitle()).isEqualTo(updateRequestDto.title());
        assertThat(updatedCatalog.getDescription()).isEqualTo(updateRequestDto.description());
        assertThat(updatedCatalog.getPrice()).isEqualTo(updateRequestDto.price());
        assertThat(updatedCatalog.getDateOfEvent()).isEqualTo(updateRequestDto.dateOfEvent());

        verify(catalogRepository, times(2)).findCatalogById(catalogId);

    }

}
