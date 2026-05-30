package com.project.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.project.model.Projekt;
import com.project.service.ProjektService;

@ExtendWith(MockitoExtension.class)
public class ProjektRestControllerTest {

    @Mock
    private ProjektService mockProjektService;

    @InjectMocks
    private ProjektRestController projectRestController;

    @Test
    void getProjekt_whenValidId_returnsProjekt() {
        Integer projektId = 1;
        Projekt expectedProjekt = createProjektTestowy(projektId, "Nazwa testowa");
        given(mockProjektService.getProjekt(projektId)).willReturn(Optional.of(expectedProjekt));

        ResponseEntity<Projekt> responseEntity = projectRestController.getProjekt(projektId);

        assertAll(
            () -> assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK),
            () -> assertThat(responseEntity.getBody()).isEqualTo(expectedProjekt)
        );
    }

    @Test
    void getProjekt_whenInvalidId_returnsNotFound() {
        Integer projektId = 1;
        given(mockProjektService.getProjekt(projektId)).willReturn(Optional.empty());

        ResponseEntity<Projekt> responseEntity = projectRestController.getProjekt(projektId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @SuppressWarnings("null")
    @Test
    void createProjekt_whenValidData_returnsCreatedWithLocation() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Projekt projektToSave = createProjektTestowy(null, "Nazwa testowa");
        Integer projektId = 1;
        Projekt createdProjekt = createProjektTestowy(projektId, projektToSave.getNazwa());

        given(mockProjektService.setProjekt(projektToSave)).willReturn(createdProjekt);

        ResponseEntity<Void> responseEntity = projectRestController.createProjekt(projektToSave);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getHeaders().getLocation().getPath()).isEqualTo("/" + projektId);
        verify(mockProjektService).setProjekt(projektToSave);
    }

    // Bezpieczna metoda tworząca projekt testowy (BEZ BUILDERA)
    private Projekt createProjektTestowy(Integer id, String nazwa) {
        Projekt p = new Projekt();
        p.setProjektId(id);
        p.setNazwa(nazwa);
        p.setOpis("Opis testowy");
        p.setDataOddania(LocalDate.of(2026, 6, 1));
        return p;
    }

    @AfterEach
    void resetRequestAttributes() {
        RequestContextHolder.resetRequestAttributes();
    }
}