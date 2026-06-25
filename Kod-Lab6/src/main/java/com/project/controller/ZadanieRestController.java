package com.project.controller;

import java.net.URI;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.project.model.Zadanie;
import com.project.service.ZadanieService;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")

@Tag(name = "Zadanie")
public class ZadanieRestController {
    private ZadanieService zadanieService;

    public ZadanieRestController(ZadanieService zadanieService) {
        this.zadanieService = zadanieService;
    }
    
    @GetMapping("/zadania/{zadanieId}")
    ResponseEntity<Zadanie> getZadanie(@PathVariable("zadanieId") Integer zadanieId) {
        return ResponseEntity.of(zadanieService.getZadanie(zadanieId));
    }

    @PostMapping("/zadania")
    ResponseEntity<Void> createZadanie(@Valid @RequestBody Zadanie zadanie) {
    Zadanie createdZadanie = zadanieService.setZadanie(zadanie);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
    .path("/{zadanieId}").buildAndExpand(createdZadanie.getZadanieId()).toUri();
    return ResponseEntity.created(location).build();
    }

    @PutMapping("/zadania/{zadanieId}")
    public ResponseEntity<Void> updateZadanie(@Valid @RequestBody Zadanie zadanie, 
        @PathVariable("zadanieId") Integer zadanieId) {
            return zadanieService.getZadanie(zadanieId).map(z -> {
                zadanieService.setZadanie(zadanie);
                return new ResponseEntity<Void>(HttpStatus.OK);
            }).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/zadania/{zadanieId}")
    public ResponseEntity<Void> deleteZadanie(@PathVariable("zadanieId") Integer zadanieId) {
        return zadanieService.getZadanie(zadanieId).map(z -> {
            zadanieService.deleteZadanie(zadanieId);
            return new ResponseEntity<Void>(HttpStatus.OK);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/zadania")
    public Page<Zadanie> getZadania(@ParameterObject Pageable pageable) {
        return zadanieService.getZadania(pageable);
    }

    @GetMapping(value = "/zadania", params = "nazwa")
    public Page<Zadanie> getZadaniaByNazwa(@RequestParam(name = "nazwa") String nazwa,
    @ParameterObject Pageable pageable) {
        return zadanieService.searchByNazwa(nazwa, pageable);
    }

    @GetMapping(value = "/zadania", params = "projektId")
    public Page<Zadanie> getZadaniaProjektu(@RequestParam(name = "projektId") Integer projektId,
    @ParameterObject Pageable pageable) {
        return zadanieService.getZadaniaProjektu(projektId, pageable);
    }

    @GetMapping(value = "/zadania", params = {"projektId", "nazwa"})
    public Page<Zadanie> searchZadaniaWProjekcie(@RequestParam(name = "projektId") Integer projektId, 
    @RequestParam(name = "nazwa") String nazwa, @ParameterObject Pageable pageable) {
        return zadanieService.searchZadaniaWProjekcie(projektId, nazwa, pageable);
    }
}
