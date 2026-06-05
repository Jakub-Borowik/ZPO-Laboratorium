package com.project.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.project.model.Student;

public interface StudentRepository extends JpaRepository<Student, Integer> {
 Optional<Student> findByNrIndeksu(String nrIndeksu);

 @Query("SELECT s FROM Student s WHERE cast(s.nrIndeksu as string) LIKE concat(:nrIndeksu, '%')")
 Page<Student> findByNrIndeksuStartsWith(@Param("nrIndeksu") String nrIndeksu, Pageable pageable);
 
 Page<Student> findByNazwiskoStartsWithIgnoreCase(String nazwisko, Pageable pageable);
 // Metoda findByNrIndeksuStartsWith definiuje zapytanie
 // SELECT s FROM Student s WHERE s.nrIndeksu LIKE :nrIndeksu%
 // Metoda findByNazwiskoStartsWithIgnoreCase definiuje zapytanie
 // SELECT s FROM Student s WHERE upper(s.nazwisko) LIKE upper(:nazwisko%)
}
