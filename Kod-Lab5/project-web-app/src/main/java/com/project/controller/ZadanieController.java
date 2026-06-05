package com.project.controller;

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
import com.project.model.Zadanie;
import com.project.service.ProjektService;
import com.project.service.ZadanieService;

@Controller
public class ZadanieController {
    private ZadanieService zadanieService;
    private ProjektService projektService;

    public ZadanieController(ZadanieService zadanieService, ProjektService projektService) {
        this.zadanieService = zadanieService;
        this.projektService = projektService;
    }

    @GetMapping("/zadanieList")
    public String zadanieList(Model model,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        String trimmedQuery = query == null ? "" : query.trim();
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Zadanie> pageData = trimmedQuery.isEmpty()
                ? zadanieService.getZadania(pageRequest)
                : zadanieService.searchByNazwa(trimmedQuery, pageRequest);
        model.addAttribute("zadania", pageData.getContent());
        model.addAttribute("page", pageData);
        model.addAttribute("q", trimmedQuery);
        return "zadanieList";
    }

    @GetMapping("/zadanieEdit")
    public String zadaneieEdit(@RequestParam(name = "zadanieId", required = false) Integer zadanieId, Model model) {
        Zadanie zadanie;
        if (zadanieId != null) {
            zadanie = zadanieService.getZadanie(zadanieId).get();
        } else {
            zadanie = new Zadanie();
        }
        model.addAttribute("zadanie", zadanie);
        model.addAttribute("selectedProjektId",
                zadanie.getProjekt() != null ? zadanie.getProjekt().getProjektId() : null);
        model.addAttribute("projekty", projektService.getProjekty(PageRequest.of(0, 100)).getContent());

        return "zadanieEdit";
    }

    @SuppressWarnings("null")
    @PostMapping(path = "/zadanieEdit")
    public String zadanieEditSave(@ModelAttribute @Valid Zadanie zadanie, BindingResult bindingResult, Model model,
            @RequestParam(name = "projektId", required = false) Integer projektId) {
        zadanie.setProjekt(resolveProjekt(projektId));
        if (bindingResult.hasErrors()) {
            model.addAttribute("selectedProjektId", projektId);
            model.addAttribute("projekty", projektService.getProjekty(PageRequest.of(0, 100)).getContent());
            return "zadanieEdit";
        }
        try {
            zadanie = zadanieService.setZadanie(zadanie);
        } catch (HttpStatusCodeException e) {
            bindingResult.rejectValue(Strings.EMPTY, String.valueOf(e.getStatusCode().value()),
                    e.getStatusCode().toString());
            model.addAttribute("selectedProjektId", projektId);
            model.addAttribute("projekty", projektService.getProjekty(PageRequest.of(0, 100)).getContent());
            return "zadanieEdit";
        }
        return "redirect:/zadanieList";
    }

    @PostMapping(params = "cancel", path = "/zadanieEdit")
    public String zadanieEditCancel() {
        return "redirect:/zadanieList";
    }

    @PostMapping(params = "delete", path = "/zadanieEdit")
    public String zadanieEditDelete(@ModelAttribute Zadanie zadanie) {
        zadanieService.deleteZadanie(zadanie.getZadanieId());
        return "redirect:/zadanieList";
    }

    private Projekt resolveProjekt(Integer projektId) {
        if (projektId == null) {
            return null;
        }

        return projektService.getProjekt(projektId).orElse(null);
    }
}
