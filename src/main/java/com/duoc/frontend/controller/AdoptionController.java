package com.duoc.frontend.controller;

import com.duoc.frontend.dto.request.AdoptionPetRequest;
import com.duoc.frontend.dto.request.AdoptionRequest;
import com.duoc.frontend.dto.response.AdoptionPetResponse;
import com.duoc.frontend.security.JwtCookieService;
import com.duoc.frontend.service.BackendApiService;
import com.duoc.frontend.service.BackendServiceException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdoptionController {

    private final BackendApiService backendApiService;
    private final JwtCookieService jwtCookieService;

    public AdoptionController(BackendApiService backendApiService, JwtCookieService jwtCookieService) {
        this.backendApiService = backendApiService;
        this.jwtCookieService = jwtCookieService;
    }

    @GetMapping("/adoption/catalog")
    public String catalog(@RequestParam(required = false) String species,
                          @RequestParam(required = false) String location,
                          @RequestParam(required = false) String gender,
                          @RequestParam(required = false) String age,
                          Model model) {
        try {
            model.addAttribute("pets", backendApiService.getCatalog(species, location, gender, age));
        } catch (BackendServiceException ex) {
            model.addAttribute("pets", java.util.List.of());
            model.addAttribute("errorMessage", ex.getMessage());
        }

        model.addAttribute("species", species);
        model.addAttribute("location", location);
        model.addAttribute("gender", gender);
        model.addAttribute("age", age);
        return "adoption/catalog";
    }

    @GetMapping("/adoption/manage")
    public String managePets(HttpServletRequest request, Model model) {
        String token = jwtCookieService.extractToken(request);

        try {
            model.addAttribute("pets", backendApiService.getManagedPets(token));
            return "adoption/manage";
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            model.addAttribute("pets", java.util.List.of());
            model.addAttribute("errorMessage", ex.getMessage());
            return "adoption/manage";
        }
    }

    @GetMapping("/adoption/manage/new")
    public String newPetForm(Model model) {
        if (!model.containsAttribute("petRequest")) {
            model.addAttribute("petRequest", new AdoptionPetRequest());
        }

        model.addAttribute("formTitle", "Registrar mascota en adopcion");
        model.addAttribute("formAction", "/adoption/manage");
        return "adoption/pet-form";
    }

    @GetMapping("/adoption/manage/{petId}")
    public String editPetForm(@PathVariable Long petId,
                              HttpServletRequest request,
                              Model model) {
        String token = jwtCookieService.extractToken(request);

        try {
            AdoptionPetResponse pet = backendApiService.getManagedPet(token, petId);
            if (!model.containsAttribute("petRequest")) {
                AdoptionPetRequest petRequest = new AdoptionPetRequest();
                petRequest.setName(pet.getName());
                petRequest.setSpecies(toPetFormSpecies(pet.getSpecies()));
                petRequest.setBreed(pet.getBreed());
                petRequest.setAge(pet.getAge());
                petRequest.setGender(toPetFormGender(pet.getGender()));
                petRequest.setLocation(pet.getLocation());
                petRequest.setStatus(toPetFormStatus(pet.getStatus()));
                petRequest.setPhotoUrl(pet.getPhotoUrl());
                model.addAttribute("petRequest", petRequest);
            }
            model.addAttribute("pet", pet);
            model.addAttribute("formTitle", "Editar mascota en adopcion");
            model.addAttribute("formAction", "/adoption/manage/" + petId);
            return "adoption/pet-form";
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            model.addAttribute("errorMessage", ex.getMessage());
            return "redirect:/adoption/manage";
        }
    }

    @PostMapping("/adoption/manage")
    public String createPet(@ModelAttribute("petRequest") AdoptionPetRequest petRequest,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        String token = jwtCookieService.extractToken(request);

        try {
            backendApiService.createPet(token, petRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Mascota en adopcion registrada correctamente");
            return "redirect:/adoption/manage";
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("petRequest", petRequest);
            return "redirect:/adoption/manage/new";
        }
    }

    @PostMapping("/adoption/manage/{petId}")
    public String updatePet(@PathVariable Long petId,
                            @ModelAttribute("petRequest") AdoptionPetRequest petRequest,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        String token = jwtCookieService.extractToken(request);

        try {
            backendApiService.updatePet(token, petId, petRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Mascota en adopcion actualizada correctamente");
            return "redirect:/adoption/manage";
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("petRequest", petRequest);
            return "redirect:/adoption/manage/" + petId;
        }
    }

    @PostMapping("/adoption/manage/{petId}/delete")
    public String deletePet(@PathVariable Long petId,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        String token = jwtCookieService.extractToken(request);

        try {
            backendApiService.deletePet(token, petId);
            redirectAttributes.addFlashAttribute("successMessage", "Mascota en adopcion eliminada correctamente");
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/adoption/manage";
    }

    @GetMapping("/adoption/adopt/{petId}")
    public String adoptionForm(@PathVariable Long petId,
                               HttpServletRequest request,
                               Model model) {
        String token = jwtCookieService.extractToken(request);

        try {
            model.addAttribute("pet", backendApiService.getManagedPet(token, petId));
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            model.addAttribute("errorMessage", ex.getMessage());
        }

        if (!model.containsAttribute("adoptionRequest")) {
            model.addAttribute("adoptionRequest", new AdoptionRequest());
        }

        return "adoption/adoption-form";
    }

    @PostMapping("/adoption/adopt/{petId}")
    public String registerAdoption(@PathVariable Long petId,
                                   @ModelAttribute("adoptionRequest") AdoptionRequest adoptionRequest,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {
        String token = jwtCookieService.extractToken(request);

        try {
            backendApiService.registerAdoption(token, petId, adoptionRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Adopcion registrada correctamente");
            return "redirect:/adoption/records";
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("adoptionRequest", adoptionRequest);
            return "redirect:/adoption/adopt/" + petId;
        }
    }

    @GetMapping("/adoption/records")
    public String adoptionRecords(HttpServletRequest request, Model model) {
        String token = jwtCookieService.extractToken(request);

        try {
            model.addAttribute("records", backendApiService.getAdoptionRecords(token));
            return "adoption/records";
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            model.addAttribute("records", java.util.List.of());
            model.addAttribute("errorMessage", ex.getMessage());
            return "adoption/records";
        }
    }

    @GetMapping("/adoption/records/{recordId}")
    public String adoptionRecordDetail(@PathVariable Long recordId,
                                       HttpServletRequest request,
                                       Model model) {
        String token = jwtCookieService.extractToken(request);

        try {
            model.addAttribute("record", backendApiService.getAdoptionRecord(token, recordId));
            return "adoption/record-detail";
        } catch (BackendServiceException ex) {
            if (ex.isUnauthorized()) {
                return "redirect:/login";
            }

            model.addAttribute("errorMessage", ex.getMessage());
            return "adoption/record-detail";
        }
    }

    private String toPetFormSpecies(String value) {
        if (value == null) {
            return null;
        }
        return switch (value.trim().toLowerCase()) {
            case "dog", "perro" -> "Dog";
            case "cat", "gato" -> "Cat";
            default -> value;
        };
    }

    private String toPetFormGender(String value) {
        if (value == null) {
            return null;
        }
        return switch (value.trim().toLowerCase()) {
            case "male", "macho" -> "Male";
            case "female", "hembra" -> "Female";
            default -> value;
        };
    }

    private String toPetFormStatus(String value) {
        if (value == null) {
            return null;
        }
        return switch (value.trim().replace("_", " ").toLowerCase()) {
            case "available", "disponible" -> "AVAILABLE";
            case "unavailable", "no disponible" -> "UNAVAILABLE";
            case "adopted", "adoptado", "adoptada" -> "ADOPTED";
            default -> value;
        };
    }
}
