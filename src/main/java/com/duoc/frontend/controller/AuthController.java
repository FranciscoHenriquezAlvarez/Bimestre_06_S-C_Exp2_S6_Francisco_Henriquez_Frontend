package com.duoc.frontend.controller;

import com.duoc.frontend.dto.request.LoginRequest;
import com.duoc.frontend.dto.response.LoginResponse;
import com.duoc.frontend.security.JwtCookieService;
import com.duoc.frontend.service.BackendApiService;
import com.duoc.frontend.service.BackendServiceException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final BackendApiService backendApiService;
    private final JwtCookieService jwtCookieService;

    public AuthController(BackendApiService backendApiService, JwtCookieService jwtCookieService) {
        this.backendApiService = backendApiService;
        this.jwtCookieService = jwtCookieService;
    }

    @GetMapping("/login")
    public String loginPage(Authentication authentication, Model model) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }

        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }

        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@ModelAttribute("loginRequest") LoginRequest loginRequest,
                          HttpServletResponse response,
                          Model model) {
        try {
            LoginResponse loginResponse = backendApiService.login(loginRequest);
            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookieService.createAuthCookie(loginResponse.getToken()).toString());
            return "redirect:/dashboard";
        } catch (BackendServiceException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response, RedirectAttributes redirectAttributes) {
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookieService.clearAuthCookie().toString());
        SecurityContextHolder.clearContext();
        redirectAttributes.addFlashAttribute("successMessage", "Sesion cerrada correctamente");
        return "redirect:/login";
    }
}
