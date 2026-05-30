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
import com.project.model.Student;
import com.project.service.StudentService;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")

@Tag(name = "Student")
public class StudentRestController {
    private StudentService studentService;

    public StudentRestController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/studenci/{studentId}")
    ResponseEntity<Student> getStudent(@PathVariable("studentId") Integer studentId) {
        return ResponseEntity.of(studentService.getStudent(studentId));
    }

    @PostMapping("/studenci")
    ResponseEntity<Void> createStudent(@Valid @RequestBody Student student) {
        Student createdStudent = studentService.setStudent(student);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{studentId}").buildAndExpand(createdStudent.getStudentId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/studenci/{studentId}")
    public ResponseEntity<Void> updateStudent(@Valid @RequestBody Student student, 
        @PathVariable("studentId") Integer studentId) {
            return studentService.getStudent(studentId).map(s -> {
                studentService.setStudent(student);
                return new ResponseEntity<Void>(HttpStatus.OK);
            }).orElseGet(() -> ResponseEntity.notFound().build());
        }

    @DeleteMapping("/studenci/{studentId}")
    public ResponseEntity<Void> deleteStudent(@PathVariable("studentId") Integer studentId) {
        return studentService.getStudent(studentId).map(s -> {
            studentService.deleteStudent(studentId);
            return new ResponseEntity<Void>(HttpStatus.OK);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/studenci")
    public Page<Student> getStudenci(@ParameterObject Pageable pageable) {
        return studentService.getStudenci(pageable);
    }

    @GetMapping(value = "/studenci", params = "nrIndeksu")
    public ResponseEntity<Student> getStudentByNrIndeksu(@RequestParam(name = "nrIndeksu") String nrIndeksu) {
        return ResponseEntity.of(studentService.getStudentByNrIndeksu(nrIndeksu));
    }

    @GetMapping(value = "/studenci", params = "poczatekIndeksu")
    public Page<Student> searchByNrIndeksuStartsWith(@RequestParam(name = "poczatekIndeksu") String poczatekIndeksu, 
    @ParameterObject Pageable pageable) {
        return studentService.searchByNrIndeksuStartsWith(poczatekIndeksu, pageable);
    }

    @GetMapping(value = "/studenci", params = "poczatekNazwiska")
    public Page<Student> searchByNazwiskoStartsWith(@RequestParam(name = "poczatekNazwiska") String poczatekNazwiska,
    @ParameterObject Pageable pageable) {
        return studentService.searchByNazwiskoStartsWith(poczatekNazwiska, pageable);
    }
}
