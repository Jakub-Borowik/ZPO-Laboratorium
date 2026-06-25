package com.project.controller;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;
import com.project.model.Projekt;
import com.project.model.Student;
import com.project.service.ProjektService;
import com.project.service.StudentService;

@Controller
public class StudentController {
    private StudentService studentService;
    private ProjektService projektService;

    public StudentController(StudentService studentService, ProjektService projektService) {
        this.studentService = studentService;
        this.projektService = projektService;
    }

    @GetMapping("/studentList")
    public String studentList(Model model,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        String trimmedQuery = query == null ? "" : query.trim();
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Student> pageData;
        if (trimmedQuery.isEmpty()) {
            pageData = studentService.getStudenci(pageRequest);
        } else if (trimmedQuery.matches("\\d+")) {
            pageData = studentService.searchByNrIndeksuStartsWith(trimmedQuery, pageRequest);
        } else {
            pageData = studentService.searchByNazwiskoStartsWith(trimmedQuery, pageRequest);
        }
        model.addAttribute("studenci", pageData.getContent());
        model.addAttribute("page", pageData);
        model.addAttribute("q", trimmedQuery);
        return "studentList";
    }

    @GetMapping("/studentEdit")
    public String studentEdit(@RequestParam(name = "studentId", required = false) Integer studentId, Model model) {
        Student student;
        if (studentId != null) {
            student = studentService.getStudent(studentId).get();
        } else {
            student = new Student();
        }
        model.addAttribute("student", student);
        model.addAttribute("projekty", projektService.getProjekty(PageRequest.of(0, 100)).getContent());
        model.addAttribute("selectedProjektIds", getSelectedProjektIds(student));
        return "studentEdit";
    }

    @SuppressWarnings("null")
    @PostMapping(path = "/studentEdit")
    public String studentEditSave(@ModelAttribute @Valid Student student, BindingResult bindingResult, Model model,
            @RequestParam(name = "projektIds", required = false) List<Integer> projektIds) {
        model.addAttribute("selectedProjektIds", getSelectedProjektIds(projektIds));
        if (bindingResult.hasErrors()) {
            model.addAttribute("projekty", projektService.getProjekty(PageRequest.of(0, 100)).getContent());
            return "studentEdit";
        }
        try {
            student.setProjekty(resolveProjekty(projektIds));
            student = studentService.setStudent(student);
        } catch (HttpStatusCodeException e) {
            bindingResult.rejectValue(Strings.EMPTY, String.valueOf(e.getStatusCode().value()),
                    e.getStatusCode().toString());
            model.addAttribute("projekty", projektService.getProjekty(PageRequest.of(0, 100)).getContent());
            return "studentEdit";
        }
        return "redirect:/studentList";
    }

    @PostMapping(params = "cancel", path = "/studentEdit")
    public String studentEditCancel() {
        return "redirect:/studentList";
    }

    @PostMapping(params = "delete", path = "/studentEdit")
    public String studentEditDelete(@ModelAttribute Student student) {
        studentService.deleteStudent(student.getStudentId());
        return "redirect:/studentList";
    }

    private Set<Integer> getSelectedProjektIds(Student student) {
        if (student == null || student.getProjekty() == null) {
            return Collections.emptySet();
        }

        return student.getProjekty().stream()
                .map(Projekt::getProjektId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<Integer> getSelectedProjektIds(List<Integer> projektIds) {
        if (projektIds == null || projektIds.isEmpty()) {
            return Collections.emptySet();
        }

        return new HashSet<>(projektIds);
    }

    private Set<Projekt> resolveProjekty(List<Integer> projektIds) {
        if (projektIds == null || projektIds.isEmpty()) {
            return Collections.emptySet();
        }

        return projektIds.stream()
                .map(id -> projektService.getProjekt(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

}
