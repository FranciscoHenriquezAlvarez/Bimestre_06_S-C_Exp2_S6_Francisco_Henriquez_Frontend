package com.duoc.frontend.controller;

import com.duoc.frontend.dto.request.AppointmentRequest;
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
public class AppointmentController {

    private final BackendApiService backendApiService;
    private final JwtCookieService jwtCookieService;

    public AppointmentController(BackendApiService backendApiService, JwtCookieService jwtCookieService) {
        this.backendApiService = backendApiService;
        this.jwtCookieService = jwtCookieService;
    }

    @GetMapping("/appointments")
    public String appointments(HttpServletRequest request, Model model) {
        String token = jwtCookieService.extractToken(request);

        try {
            model.addAttribute("appointments", backendApiService.getAppointments(token));
            return "appointments/list";
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            model.addAttribute("appointments", java.util.List.of());
            model.addAttribute("errorMessage", ex.getMessage());
            return "appointments/list";
        }
    }

    @GetMapping("/appointments/new")
    public String newAppointment(HttpServletRequest request, Model model) {
        String token = jwtCookieService.extractToken(request);

        try {
            model.addAttribute("patients", backendApiService.getPatients(token));
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            model.addAttribute("patients", java.util.List.of());
            model.addAttribute("errorMessage", ex.getMessage());
        }

        if (!model.containsAttribute("appointmentRequest")) {
            model.addAttribute("appointmentRequest", new AppointmentRequest());
        }

        return "appointments/form";
    }

    @PostMapping("/appointments")
    public String createAppointment(@ModelAttribute AppointmentRequest appointmentRequest,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        String token = jwtCookieService.extractToken(request);

        try {
            backendApiService.createAppointment(token, appointmentRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Cita creada correctamente");
            return "redirect:/appointments";
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("appointmentRequest", appointmentRequest);
            return "redirect:/appointments/new";
        }
    }
}
