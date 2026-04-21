package com.duoc.frontend.controller;

import com.duoc.frontend.dto.request.PatientRequest;
import com.duoc.frontend.security.JwtCookieService;
import com.duoc.frontend.service.BackendApiService;
import com.duoc.frontend.service.BackendServiceException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PatientController {

    private final BackendApiService backendApiService;
    private final JwtCookieService jwtCookieService;

    public PatientController(BackendApiService backendApiService, JwtCookieService jwtCookieService) {
        this.backendApiService = backendApiService;
        this.jwtCookieService = jwtCookieService;
    }

    @GetMapping("/patients")
    public String patients(HttpServletRequest request, Model model) {
        String token = jwtCookieService.extractToken(request);

        try {
            model.addAttribute("patients", backendApiService.getPatients(token));
            return "patients/list";
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            model.addAttribute("patients", java.util.List.of());
            model.addAttribute("errorMessage", ex.getMessage());
            return "patients/list";
        }
    }

    @GetMapping("/patients/new")
    public String newPatient(Model model) {
        if (!model.containsAttribute("patientRequest")) {
            model.addAttribute("patientRequest", new PatientRequest());
        }
        return "patients/form";
    }

    @PostMapping("/patients")
    public String createPatient(@ModelAttribute PatientRequest patientRequest,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        String token = jwtCookieService.extractToken(request);

        try {
            backendApiService.createPatient(token, patientRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Paciente registrado correctamente");
            return "redirect:/patients";
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("patientRequest", patientRequest);
            return "redirect:/patients/new";
        }
    }
}
