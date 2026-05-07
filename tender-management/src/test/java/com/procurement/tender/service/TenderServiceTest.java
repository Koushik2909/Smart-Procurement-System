package com.procurement.tender.service;

import com.procurement.core.exception.ResourceNotFoundException;
import com.procurement.tender.domain.Tender;
import com.procurement.tender.domain.TenderStatus;
import com.procurement.tender.dto.TenderInput;
import com.procurement.tender.repository.TenderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenderService Unit Tests")
class TenderServiceTest {

    @Mock
    private TenderRepository tenderRepository;

    @InjectMocks
    private TenderService tenderService;

    private Tender draftTender;

    @BeforeEach
    void setUp() {
        draftTender = new Tender();
        draftTender.setId(1L);
        draftTender.setTitle("Road Construction Tender");
        draftTender.setDescription("Build a 10km highway");
        draftTender.setStatus(TenderStatus.DRAFT);
        draftTender.setCreatedByUserId(1L);
    }

    @Test
    @DisplayName("createTender — should persist and return new tender in DRAFT status")
    void createTender_ShouldReturnDraftTender() {
        TenderInput input = new TenderInput("Road Construction Tender", "Build a 10km highway", null);
        when(tenderRepository.save(any(Tender.class))).thenReturn(draftTender);

        Tender result = tenderService.createTender(input, 1L);

        assertThat(result.getTitle()).isEqualTo("Road Construction Tender");
        assertThat(result.getStatus()).isEqualTo(TenderStatus.DRAFT);
        verify(tenderRepository, times(1)).save(any(Tender.class));
    }

    @Test
    @DisplayName("publishTender — DRAFT should transition to PUBLISHED")
    void publishTender_FromDraft_ShouldTransitionToPublished() {
        when(tenderRepository.findById(1L)).thenReturn(Optional.of(draftTender));
        Tender published = new Tender();
        published.setId(1L);
        published.setStatus(TenderStatus.PUBLISHED);
        when(tenderRepository.save(any(Tender.class))).thenReturn(published);

        Tender result = tenderService.publishTender(1L);

        assertThat(result.getStatus()).isEqualTo(TenderStatus.PUBLISHED);
    }

    @Test
    @DisplayName("publishTender — OPEN tender should throw IllegalStateException")
    void publishTender_FromOpen_ShouldThrowException() {
        draftTender.setStatus(TenderStatus.OPEN);
        when(tenderRepository.findById(1L)).thenReturn(Optional.of(draftTender));

        assertThatThrownBy(() -> tenderService.publishTender(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only DRAFT tenders can be published");
    }

    @Test
    @DisplayName("cancelTender — AWARDED tender should throw IllegalStateException")
    void cancelTender_Awarded_ShouldThrowException() {
        draftTender.setStatus(TenderStatus.AWARDED);
        when(tenderRepository.findById(1L)).thenReturn(Optional.of(draftTender));

        assertThatThrownBy(() -> tenderService.cancelTender(1L, "Not needed"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel an awarded tender");
    }

    @Test
    @DisplayName("getTenderById — non-existing id should throw ResourceNotFoundException")
    void getTenderById_NotFound_ShouldThrowException() {
        when(tenderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenderService.getTenderById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tender not found with id: 99");
    }

    @Test
    @DisplayName("getActiveTenders — should return only OPEN tenders")
    void getActiveTenders_ShouldReturnOnlyOpenTenders() {
        Tender open = new Tender();
        open.setStatus(TenderStatus.OPEN);
        when(tenderRepository.findByStatus(TenderStatus.OPEN)).thenReturn(List.of(open));

        List<Tender> result = tenderService.getActiveTenders();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(TenderStatus.OPEN);
    }
}
