package com.project.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.project.model.Projekt;
import com.project.service.ProjektService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebMvcTest(ProjektRestController.class)
@AutoConfigureJsonTesters
@WithMockUser(username = "admin", password = "admin")
public class ProjektRestControllerIT {

    private final String apiPath = "/api/projekty";

    @MockitoBean
    private ProjektService mockProjektService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<Projekt> jacksonTester;

    @Test
    void getProjekt_whenValidId_returnsProjekt() throws Exception {
        Integer projektId = 1;
        Projekt expectedProjekt = createProjektTestowy(projektId, "Nazwa testowa");
        given(mockProjektService.getProjekt(projektId)).willReturn(Optional.of(expectedProjekt));

        mockMvc.perform(get(apiPath + "/{projektId}", projektId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projektId").value(projektId))
                .andExpect(jsonPath("$.nazwa").value(expectedProjekt.getNazwa()));

        verify(mockProjektService).getProjekt(projektId);
        verifyNoMoreInteractions(mockProjektService);
    }

    @Test
    void getProjekt_whenInvalidId_returnsNotFound() throws Exception {
        Integer projektId = 2;
        given(mockProjektService.getProjekt(projektId)).willReturn(Optional.empty());

        mockMvc.perform(get(apiPath + "/{projektId}", projektId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(mockProjektService).getProjekt(projektId);
        verifyNoMoreInteractions(mockProjektService);
    }

    @Test
    void getProjekty_whenAvailable_returnsPagedContent() throws Exception {
        Projekt projekt1 = createProjektTestowy(1, "Nazwa testowa 1");
        Projekt projekt2 = createProjektTestowy(2, "Nazwa testowa 2");
        Page<Projekt> expectedPage = new PageImpl<>(List.of(projekt1, projekt2));

        given(mockProjektService.getProjekty(any(Pageable.class))).willReturn(expectedPage);

        mockMvc.perform(get(apiPath)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*]").exists())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].projektId").value(projekt1.getProjektId()))
                .andExpect(jsonPath("$.content[1].projektId").value(projekt2.getProjektId()));

        verify(mockProjektService).getProjekty(any(Pageable.class));
        verifyNoMoreInteractions(mockProjektService);
    }

    @Test
    void createProjekt_whenValidData_returnsCreatedWithLocation() throws Exception {
        Projekt projektToSave = createProjektTestowy(null, "Nazwa testowa");
        Integer projektId = 1;
        Projekt createdProjekt = createProjektTestowy(projektId, projektToSave.getNazwa());
        String jsonProjekt = jacksonTester.write(projektToSave).getJson();

        given(mockProjektService.setProjekt(any(Projekt.class))).willReturn(createdProjekt);

        mockMvc.perform(post(apiPath)
                .with(csrf())
                .content(jsonProjekt)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", containsString(apiPath + "/" + projektId)));

        verify(mockProjektService).setProjekt(any(Projekt.class));
        verifyNoMoreInteractions(mockProjektService);
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

    @BeforeEach
    void before(TestInfo testInfo) {
        log.info("-- METODA -> {}", testInfo.getTestMethod().orElseThrow().getName());
    }

    @AfterEach
    void after(TestInfo testInfo) {
        log.info("<- KONIEC {}", testInfo.getTestMethod().orElseThrow().getName());
    }
}