package com.project.service;

import java.net.URI;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.project.exception.HttpException;
import com.project.model.Zadanie;

@Service // Bardzo ważne - to mówi Springowi, że ta klasa to nasz "Kucharz"
public class ZadanieServiceImpl implements ZadanieService {

    // Wstrzykujemy naszego "Magazyniera" (Repozytorium)
    private final ZadanieRepository zadanieRepository;

    //@Autowired
    public ZadanieServiceImpl(ZadanieRepository zadanieRepository) {
        this.zadanieRepository = zadanieRepository;
    }

    @Override
    public Optional<Zadanie> getZadanie(Integer zadanieId) {
        return zadanieRepository.findById(zadanieId);
    }

    @Override
    @Transactional
    public Zadanie setZadanie(Zadanie zadanie) {
        return zadanieRepository.save(zadanie);
    }

    @Override
    @Transactional
    public void deleteZadanie(Integer zadanieId) {
        zadanieRepository.deleteById(zadanieId);
    }

    @Override
    public Page<Zadanie> getZadania(Pageable pageable) {
        return zadanieRepository.findAll(pageable);
    }

    @Override
    public Page<Zadanie> searchByNazwa(String nazwa, Pageable pageable) {
        return zadanieRepository.findByNazwaContainingIgnoreCase(nazwa, pageable);
    }

    @Override
    public Page<Zadanie> getZadaniaProjektu(Integer projektId, Pageable pageable) {
        return zadanieRepository.findZadaniaProjektu(projektId, pageable);
    }

    @Override
    public Page<Zadanie> searchZadaniaWProjekcie(Integer projektId, String nazwa, Pageable pageable) {
        return zadanieRepository.findByProjekt_ProjektIdAndNazwaContainingIgnoreCase(projektId, nazwa, pageable);
    }
}