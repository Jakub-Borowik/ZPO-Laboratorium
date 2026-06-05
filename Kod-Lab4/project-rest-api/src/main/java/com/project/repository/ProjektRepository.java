package com.project.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.model.Projekt;
import java.util.List;


public interface ProjektRepository extends JpaRepository<Projekt, Integer> {
    Page<Projekt> findByNazwaContainingIgnoreCase(String nazwa, Pageable pageable);

    List<Projekt> findByNazwaContainingIgnoreCase(String nazwa);

    @EntityGraph(attributePaths = {"studenci"})
    Optional<Projekt> findWithStudenciByProjektId(Integer projektId);

}
