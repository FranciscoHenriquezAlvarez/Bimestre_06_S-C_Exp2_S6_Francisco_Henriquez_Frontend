package com.duoc.frontend.controller;

import com.duoc.frontend.dto.request.InvoiceFromAppointmentRequest;
import com.duoc.frontend.dto.response.AppointmentResponse;
import com.duoc.frontend.security.JwtCookieService;
import com.duoc.frontend.service.BackendApiService;
import com.duoc.frontend.service.BackendServiceException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;

@Controller
public class InvoiceController {

    private final BackendApiService backendApiService;
    private final JwtCookieService jwtCookieService;

    public InvoiceController(BackendApiService backendApiService, JwtCookieService jwtCookieService) {
        this.backendApiService = backendApiService;
        this.jwtCookieService = jwtCookieService;
    }

    @GetMapping("/invoices")
    public String invoices(@RequestParam(required = false) Long appointmentId,
                           HttpServletRequest request,
                           Model model) {
        String token = jwtCookieService.extractToken(request);

        try {
            model.addAttribute("appointmentId", appointmentId);
            if (appointmentId != null) {
                model.addAttribute("invoices", backendApiService.getInvoicesByAppointment(token, appointmentId));
            } else {
                model.addAttribute("invoices", backendApiService.getInvoices(token));
            }
            return "invoices/list";
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            model.addAttribute("invoices", java.util.List.of());
            model.addAttribute("errorMessage", ex.getMessage());
            return "invoices/list";
        }
    }

    @GetMapping("/invoices/{invoiceId}")
    public String invoiceDetail(@PathVariable Long invoiceId,
                                HttpServletRequest request,
                                Model model) {
        String token = jwtCookieService.extractToken(request);

        try {
            model.addAttribute("invoice", backendApiService.getInvoiceById(token, invoiceId));
            return "invoices/detail";
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            model.addAttribute("errorMessage", ex.getMessage());
            return "invoices/detail";
        }
    }

    @GetMapping("/invoices/{invoiceId}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long invoiceId,
                                                     HttpServletRequest request) {
        String token = jwtCookieService.extractToken(request);

        try {
            // El frontend actua como intermediario para que la cookie HttpOnly no tenga que exponerse al navegador.
            ResponseEntity<byte[]> backendResponse = backendApiService.downloadInvoicePdf(token, invoiceId);
            return buildPdfResponse(backendResponse, invoiceId);
        } catch (BackendServiceException ex) {
            return buildBinaryErrorResponse(ex);
        }
    }

    @GetMapping("/invoices/new/{appointmentId}")
    public String newInvoice(@PathVariable Long appointmentId,
                             HttpServletRequest request,
                             Model model) {
        String token = jwtCookieService.extractToken(request);

        try {
            AppointmentResponse appointment = backendApiService.getAppointmentById(token, appointmentId);
            model.addAttribute("appointment", appointment);
            model.addAttribute("cares", backendApiService.getCares(token));
            model.addAttribute("medications", backendApiService.getMedications(token));
            model.addAttribute("existingInvoices", backendApiService.getInvoicesByAppointment(token, appointmentId));
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            model.addAttribute("errorMessage", ex.getMessage());
        }

        if (!model.containsAttribute("invoiceRequest")) {
            model.addAttribute("invoiceRequest", new InvoiceFromAppointmentRequest());
        }

        return "invoices/form";
    }

    @PostMapping("/invoices/new/{appointmentId}")
    public String createInvoice(@PathVariable Long appointmentId,
                                @ModelAttribute InvoiceFromAppointmentRequest invoiceRequest,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        String token = jwtCookieService.extractToken(request);

        try {
            backendApiService.createInvoice(token, appointmentId, invoiceRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Factura creada correctamente");
            return "redirect:/invoices?appointmentId=" + appointmentId;
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("invoiceRequest", invoiceRequest);
            return "redirect:/invoices/new/" + appointmentId;
        }
    }

    private ResponseEntity<byte[]> buildPdfResponse(ResponseEntity<byte[]> backendResponse,
                                                    Long invoiceId) {
        byte[] body = backendResponse.getBody() != null ? backendResponse.getBody() : new byte[0];
        HttpHeaders headers = new HttpHeaders();
        MediaType contentType = backendResponse.getHeaders().getContentType();

        headers.setContentType(contentType != null ? contentType : MediaType.APPLICATION_PDF);
        headers.setContentLength(body.length);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("factura-" + invoiceId + ".pdf")
                .build());

        return new ResponseEntity<>(body, headers, backendResponse.getStatusCode());
    }

    private ResponseEntity<byte[]> buildBinaryErrorResponse(BackendServiceException ex) {
        if (ex.isUnauthorized()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        return ResponseEntity.status(ex.getStatusCode())
                .contentType(MediaType.TEXT_PLAIN)
                .body(ex.getMessage().getBytes(StandardCharsets.UTF_8));
    }
}
