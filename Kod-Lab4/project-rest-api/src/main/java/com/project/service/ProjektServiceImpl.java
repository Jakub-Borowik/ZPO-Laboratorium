package com.project.service;

import java.util.Optional;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.project.model.Projekt;
import com.project.repository.ProjektRepository;

import jakarta.transaction.Transactional;

@Service
public class ProjektServiceImpl implements ProjektService {
 private ProjektRepository projektRepository;
 //@Autowired //adnotację można pomijać, jeżeli nie ma kilku wersji konstruktora
 public ProjektServiceImpl(ProjektRepository projektRepository) {
 this.projektRepository = projektRepository;
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
