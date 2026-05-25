package com.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.project.model.Projekt;
import com.project.model.Zadanie;
import com.project.repository.ProjektRepository;
import com.project.repository.ZadanieRepository;

@ExtendWith(MockitoExtension.class)
public class ProjektServiceTest {

    @Mock
    private ProjektRepository mockProjektRepository;

    @Mock
    private ZadanieRepository mockZadanieRepository;

    @InjectMocks
    private ProjektServiceImpl projektService;

    @Test
    void getProjekt_whenValidId_returnsOptionalWithProjekt() {
        Integer projektId = 1;
        Projekt expectedProjekt = createProjektTestowy(projektId, "Testowy Projekt");
        given(mockProjektRepository.findById(projektId)).willReturn(Optional.of(expectedProjekt));

        Optional<Projekt> actualOptional = projektService.getProjekt(projektId);

        assertThat(actualOptional).isPresent().contains(expectedProjekt);
        verify(mockProjektRepository).findById(projektId);
    }

    @Test
    void setProjekt_whenCalled_savesAndReturnsProjekt() {
        Projekt projektToSave = createProjektTestowy(null, "Nowy Projekt");
        Integer projektId = 1;
        Projekt savedProjekt = createProjektTestowy(projektId, projektToSave.getNazwa());

        given(mockProjektRepository.save(projektToSave)).willReturn(savedProjekt);

        Projekt actualProjekt = projektService.setProjekt(projektToSave);

        assertThat(actualProjekt).isEqualTo(savedProjekt);
        verify(mockProjektRepository).save(projektToSave);
    }

    @Test
    void deleteProjekt_whenValidId_deletesTasksAndProjekt() {
        Integer projektId = 1;
        List<Zadanie> powiazaneZadania = List.of(
            createZadanieTestowe(1, "Zadanie 1"),
            createZadanieTestowe(2, "Zadanie 2")
        );

        given(mockZadanieRepository.findZadaniaProjektu(projektId)).willReturn(powiazaneZadania);

        projektService.deleteProjekt(projektId);

        verify(mockZadanieRepository, times(1)).delete(powiazaneZadania.get(0));
        verify(mockZadanieRepository, times(1)).delete(powiazaneZadania.get(1));
        verify(mockProjektRepository).deleteById(projektId);
    }

    @Test
    void getProjekty_whenCalled_returnsPageFromRepository() {
        List<Projekt> listaProjektow = List.of(
            createProjektTestowy(1, "Nazwa testowa 1"),
            createProjektTestowy(2, "Nazwa testowa 2"),
            createProjektTestowy(3, "Nazwa testowa 3")
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<Projekt> expectedPage = new PageImpl<>(listaProjektow);

        given(mockProjektRepository.findAll(pageable)).willReturn(expectedPage);

        Page<Projekt> actualPage = projektService.getProjekty(pageable);

        assertThat(actualPage).isEqualTo(expectedPage);
        verify(mockProjektRepository).findAll(pageable);
    }

    @Test
    void searchByNazwa_whenPhraseProvided_callsRepositoryWithCorrectParams() {
        String searchPhrase = "java";
        Pageable pageable = PageRequest.of(0, 5);
        given(mockProjektRepository.findByNazwaContainingIgnoreCase(any(), any())).willReturn(Page.empty());

        projektService.searchByNazwa(searchPhrase, pageable);

        verify(mockProjektRepository).findByNazwaContainingIgnoreCase(searchPhrase, pageable);
    }

    // --- Metody pomocnicze (zastępują Lomboka @Builder) ---
    private Projekt createProjektTestowy(Integer id, String nazwa) {
        Projekt p = new Projekt();
        p.setProjektId(id);
        p.setNazwa(nazwa);
        p.setOpis("Opis testowy");
        p.setDataOddania(LocalDate.of(2026, 6, 1));
        return p;
    }

    private Zadanie createZadanieTestowe(Integer id, String nazwa) {
        Zadanie z = new Zadanie();
        z.setZadanieId(id);
        z.setNazwa(nazwa);
        return z;
    }
}