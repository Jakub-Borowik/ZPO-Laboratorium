package com.project.service;

import com.project.model.Zadanie;
import com.project.repository.ZadanieRepository;

import jakarta.transaction.Transactional;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

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