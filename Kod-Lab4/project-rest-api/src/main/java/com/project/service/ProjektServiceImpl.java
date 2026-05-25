package com.project.service;

import java.util.Optional;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.project.model.Projekt;
import com.project.model.Zadanie;
import com.project.repository.ProjektRepository;
import com.project.repository.ZadanieRepository;

import jakarta.transaction.Transactional;

@Service
public class ProjektServiceImpl implements ProjektService {
 private ProjektRepository projektRepository;
 private ZadanieRepository zadanieRepository;
 //@Autowired // WAŻNE: Konstruktor MUSI mieć w nawiasach oba repozytoria!
    public ProjektServiceImpl(ProjektRepository projektRepository, ZadanieRepository zadanieRepository) {
        this.projektRepository = projektRepository;
        this.zadanieRepository = zadanieRepository; // To przypisanie zapobiega błędowi NullPointer!
    }

 @Override
 public Optional<Projekt> getProjekt(Integer projektId) {
 return projektRepository.findById(projektId);
 }

 @Override
 @Transactional
 public Projekt setProjekt(Projekt projekt) {
 return projektRepository.save(projekt);
 }

 @Override
    @Transactional
    public void deleteProjekt(Integer projektId) {
        for (Zadanie zadanie : zadanieRepository.findZadaniaProjektu(projektId)) {
            zadanieRepository.delete(zadanie);
        }
        projektRepository.deleteById(projektId);
    }

 @Override
 public Page<Projekt> getProjekty(Pageable pageable) {
 return projektRepository.findAll(pageable);
 }

 @Override
 public Page<Projekt> searchByNazwa(String nazwa, Pageable pageable) {
 return projektRepository.findByNazwaContainingIgnoreCase(nazwa, pageable);
 }
}
