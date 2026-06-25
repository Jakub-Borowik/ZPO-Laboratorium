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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;
import com.project.exception.HttpException;
import com.project.model.Projekt;
import com.project.model.Student;
import com.project.model.Zadanie;
import com.project.service.ProjektService;
import com.project.service.StudentService;
import com.project.service.ZadanieService;

@Controller
public class ProjektController {
    private ProjektService projektService;
    private StudentService studentService;
    private ZadanieService zadanieService;

    // @Autowired – przy jednym konstruktorze wstrzykiwanie jest zadaniem domyślnym,
    // adnotacji nie jest potrzebna
    public ProjektController(ProjektService projektService, StudentService studentService,
            ZadanieService zadanieService) {
        this.projektService = projektService;
        this.studentService = studentService;
        this.zadanieService = zadanieService;
    }

    @GetMapping("/projektList") // np.
                                // http://localhost:8081/projektList?page=0&size=10&sort=dataczasModyfikacji,desc
    public String projektList(Model model,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        String trimmedQuery = query == null ? "" : query.trim();
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Projekt> pageData = trimmedQuery.isEmpty()
                ? projektService.getProjekty(pageRequest)
                : projektService.searchByNazwa(trimmedQuery, pageRequest);
        List<Projekt> projekty = pageData.getContent();
        for (Projekt projekt : projekty) {
            if (projekt.getProjektId() != null) {
                projekt.setZadania(getZadaniaProjektu(projekt.getProjektId()));
            }
        }
        model.addAttribute("projekty", projekty);
        model.addAttribute("page", pageData);
        model.addAttribute("q", trimmedQuery);
        return "projektList";
    }

    @GetMapping("/projektEdit")
    public String projektEdit(@RequestParam(name = "projektId", required = false) Integer projektId, Model model) {
        Projekt projekt;
        if (projektId != null) {
            projekt = projektService.getProjekt(projektId).get();
        } else {
            projekt = new Projekt();
        }
        model.addAttribute("projekt", projekt);
        model.addAttribute("studenci", studentService.getStudenci(Pageable.ofSize(100)).getContent());
        model.addAttribute("selectedStudentIds", getSelectedStudentIds(projekt));
        List<Zadanie> zadaniaProjektu = getZadaniaProjektu(projektId);
        model.addAttribute("zadaniaProjektu", zadaniaProjektu);
        model.addAttribute("selectedZadanieIds", getSelectedZadanieIdsFromZadania(zadaniaProjektu));
        return "projektEdit";
    }

    @SuppressWarnings("null")
    @PostMapping(path = "/projektEdit")
    public String projektEditSave(@ModelAttribute @Valid Projekt projekt, BindingResult bindingResult, Model model,
            @RequestParam(name = "studentIds", required = false) List<Integer> studentIds,
            @RequestParam(name = "zadanieIds", required = false) List<Integer> zadanieIds) {
        model.addAttribute("studenci", studentService.getStudenci(Pageable.ofSize(100)).getContent());
        model.addAttribute("selectedStudentIds", getSelectedStudentIds(studentIds));
        model.addAttribute("zadaniaProjektu", getZadaniaProjektu(projekt.getProjektId()));
        model.addAttribute("selectedZadanieIds", getSelectedZadanieIdsFromIds(zadanieIds));
        // parametr BindingResult powinien wystąpić zaraz za parametrem opatrzonym
        // adnotacją @Valid
        if (bindingResult.hasErrors()) {
            return "projektEdit";
        }
        try {
            projekt.setStudenci(resolveStudenci(studentIds));
            projekt = projektService.setProjekt(projekt);
            syncZadaniaWithProjekt(projekt.getProjektId(), getSelectedZadanieIdsFromIds(zadanieIds));
        } catch (HttpStatusCodeException e) {
            bindingResult.rejectValue(Strings.EMPTY, String.valueOf(e.getStatusCode().value()),
                    e.getStatusCode().toString());
            return "projektEdit";
        } catch (HttpException e) {
            bindingResult.reject("httpError", e.getMessage());
            return "projektEdit";
        }
        return "redirect:/projektList";
    }

    @PostMapping(params = "cancel", path = "/projektEdit")
    public String projektEditCancel() {
        return "redirect:/projektList";
    }

    @PostMapping(params = "delete", path = "/projektEdit")
    public String projektEditDelete(@ModelAttribute Projekt projekt) {
        projektService.deleteProjekt(projekt.getProjektId());
        return "redirect:/projektList";
    }

    private Set<Integer> getSelectedStudentIds(Projekt projekt) {
        if (projekt == null || projekt.getStudenci() == null) {
            return Collections.emptySet();
        }

        return projekt.getStudenci().stream()
                .map(Student::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<Integer> getSelectedStudentIds(List<Integer> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Collections.emptySet();
        }

        return new HashSet<>(studentIds);
    }

    private Set<Student> resolveStudenci(List<Integer> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Collections.emptySet();
        }

        return studentIds.stream()
                .map(id -> studentService.getStudent(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<Integer> getSelectedZadanieIdsFromIds(List<Integer> zadanieIds) {
        if (zadanieIds == null || zadanieIds.isEmpty()) {
            return Collections.emptySet();
        }

        return new HashSet<>(zadanieIds);
    }

    private Set<Integer> getSelectedZadanieIdsFromZadania(List<Zadanie> zadania) {
        if (zadania == null || zadania.isEmpty()) {
            return Collections.emptySet();
        }

        return zadania.stream()
                .map(Zadanie::getZadanieId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private List<Zadanie> getZadaniaProjektu(Integer projektId) {
        if (projektId == null) {
            return Collections.emptyList();
        }

        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "kolejnosc"));
        return zadanieService.getZadaniaProjektu(projektId, pageable).getContent();
    }

    private void syncZadaniaWithProjekt(Integer projektId, Set<Integer> selectedZadanieIds) {
        if (projektId == null) {
            return;
        }

        List<Zadanie> currentZadania = getZadaniaProjektu(projektId);
        for (Zadanie zadanie : currentZadania) {
            Integer zadanieId = zadanie.getZadanieId();
            if (zadanieId == null) {
                continue;
            }
            if (selectedZadanieIds.contains(zadanieId)) {
                continue;
            }
            zadanie.setProjekt(null);
            zadanieService.setZadanie(zadanie);
        }
    }
}