package com.duoc.frontend.controller;

import com.duoc.frontend.security.JwtCookieService;
import com.duoc.frontend.service.BackendApiService;
import com.duoc.frontend.service.BackendServiceException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final BackendApiService backendApiService;
    private final JwtCookieService jwtCookieService;

    public DashboardController(BackendApiService backendApiService, JwtCookieService jwtCookieService) {
        this.backendApiService = backendApiService;
        this.jwtCookieService = jwtCookieService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        String token = jwtCookieService.extractToken(request);

        try {
            model.addAttribute("patientsCount", backendApiService.getPatients(token).size());
            model.addAttribute("appointmentsCount", backendApiService.getAppointments(token).size());
            model.addAttribute("invoicesCount", backendApiService.getInvoices(token).size());
            model.addAttribute("catalogCount", backendApiService.getCatalog(null, null, null, null).size());
            model.addAttribute("adoptionRecordsCount", backendApiService.getAdoptionRecords(token).size());
            return "dashboard";
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            model.addAttribute("errorMessage", ex.getMessage());
            return "dashboard";
        }
    }
}
