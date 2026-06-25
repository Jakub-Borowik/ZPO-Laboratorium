package com.project.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.project.model.Projekt;
import com.project.model.Student;
import com.project.model.Zadanie;
import com.project.repository.ProjektRepository;
import com.project.repository.StudentRepository;
import com.project.repository.ZadanieRepository;

import jakarta.transaction.Transactional;

@Service
public class ProjektServiceImpl implements ProjektService {
    private ProjektRepository projektRepository;
    private ZadanieRepository zadanieRepository;
    private StudentRepository studentRepository;

    public ProjektServiceImpl(ProjektRepository projektRepository, ZadanieRepository zadanieRepository,
            StudentRepository studentRepository) {
        this.projektRepository = projektRepository;
        this.zadanieRepository = zadanieRepository; // To przypisanie zapobiega błędowi NullPointer!
        this.studentRepository = studentRepository;
    }

    @Override
    public Optional<Projekt> getProjekt(Integer projektId) {
        return projektRepository.findById(projektId);
    }

    @Override
    @Transactional
    public Projekt setProjekt(Projekt projekt) {
        Set<Student> requestedStudenci = projekt.getStudenci();
        projekt.setStudenci(null);
        Projekt savedProjekt = projektRepository.save(projekt);
        syncProjektStudenci(savedProjekt, requestedStudenci);
        return savedProjekt;
    }

    private void syncProjektStudenci(Projekt projekt, Set<Student> requestedStudenci) {
        if (projekt.getProjektId() == null) {
            return;
        }

        Set<Integer> requestedIds = requestedStudenci == null ? Collections.emptySet()
                : requestedStudenci.stream()
                        .map(Student::getStudentId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        Set<Integer> currentIds = projektRepository.findWithStudenciByProjektId(projekt.getProjektId())
                .map(Projekt::getStudenci)
                .orElse(Collections.emptySet())
                .stream()
                .map(Student::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Integer> toRemove = new HashSet<>(currentIds);
        toRemove.removeAll(requestedIds);

        Set<Integer> toAdd = new HashSet<>(requestedIds);
        toAdd.removeAll(currentIds);

        for (Integer studentId : toRemove) {
            studentRepository.findById(studentId).ifPresent(student -> {
                if (student.getProjekty() != null) {
                    student.getProjekty().removeIf(p -> Objects.equals(p.getProjektId(), projekt.getProjektId()));
                }
                studentRepository.save(student);
            });
        }

        Projekt projektRef = projektRepository.getReferenceById(projekt.getProjektId());
        for (Integer studentId : toAdd) {
            studentRepository.findById(studentId).ifPresent(student -> {
                if (student.getProjekty() == null) {
                    student.setProjekty(new HashSet<>());
                }
                student.getProjekty().add(projektRef);
                studentRepository.save(student);
            });
        }
    }

    @Override
    @Transactional
    public void deleteProjekt(Integer projektId) {
        projektRepository.findWithStudenciByProjektId(projektId).ifPresent(projekt -> {
            if (projekt.getStudenci() != null) {
                for (Student student : projekt.getStudenci()) {
                    if (student.getProjekty() != null) {
                        student.getProjekty().removeIf(p -> Objects.equals(p.getProjektId(), projektId));
                    }
                    studentRepository.save(student);
                }
            }
        });
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
