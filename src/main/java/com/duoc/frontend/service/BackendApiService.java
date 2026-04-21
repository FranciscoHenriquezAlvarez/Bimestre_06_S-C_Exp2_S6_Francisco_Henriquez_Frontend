package com.duoc.frontend.service;

import com.duoc.frontend.dto.request.AdoptionPetRequest;
import com.duoc.frontend.dto.request.AdoptionRequest;
import com.duoc.frontend.dto.request.AppointmentRequest;
import com.duoc.frontend.dto.request.InvoiceFromAppointmentRequest;
import com.duoc.frontend.dto.request.LoginRequest;
import com.duoc.frontend.dto.request.PatientRequest;
import com.duoc.frontend.dto.response.AdoptionPetResponse;
import com.duoc.frontend.dto.response.AdoptionResponse;
import com.duoc.frontend.dto.response.ApiErrorResponse;
import com.duoc.frontend.dto.response.AppointmentResponse;
import com.duoc.frontend.dto.response.CareResponse;
import com.duoc.frontend.dto.response.InvoiceResponse;
import com.duoc.frontend.dto.response.LoginResponse;
import com.duoc.frontend.dto.response.MedicationResponse;
import com.duoc.frontend.dto.response.PatientResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

@Service
public class BackendApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String backendBaseUrl;

    public BackendApiService(RestTemplate restTemplate,
                             ObjectMapper objectMapper,
                             @Value("${backend.base-url}") String backendBaseUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.backendBaseUrl = backendBaseUrl;
    }

    public LoginResponse login(LoginRequest request) {
        return exchange(null, HttpMethod.POST, backendBaseUrl + "/login", request, LoginResponse.class);
    }

    public List<PatientResponse> getPatients(String token) {
        return exchangeList(token, HttpMethod.GET, backendBaseUrl + "/patient", null,
                new ParameterizedTypeReference<>() {
                });
    }

    public PatientResponse createPatient(String token, PatientRequest request) {
        return exchange(token, HttpMethod.POST, backendBaseUrl + "/patient", request, PatientResponse.class);
    }

    public List<AppointmentResponse> getAppointments(String token) {
        return exchangeList(token, HttpMethod.GET, backendBaseUrl + "/appointment", null,
                new ParameterizedTypeReference<>() {
                });
    }

    public AppointmentResponse getAppointmentById(String token, Long appointmentId) {
        return exchange(token, HttpMethod.GET, backendBaseUrl + "/appointment/" + appointmentId, null, AppointmentResponse.class);
    }

    public AppointmentResponse createAppointment(String token, AppointmentRequest request) {
        return exchange(token, HttpMethod.POST, backendBaseUrl + "/appointment", request, AppointmentResponse.class);
    }

    public List<InvoiceResponse> getInvoices(String token) {
        return exchangeList(token, HttpMethod.GET, backendBaseUrl + "/invoice", null,
                new ParameterizedTypeReference<>() {
                });
    }

    public List<InvoiceResponse> getInvoicesByAppointment(String token, Long appointmentId) {
        return exchangeList(token, HttpMethod.GET, backendBaseUrl + "/invoice/appointment/" + appointmentId, null,
                new ParameterizedTypeReference<>() {
                });
    }

    public InvoiceResponse getInvoiceById(String token, Long invoiceId) {
        return exchange(token, HttpMethod.GET, backendBaseUrl + "/invoice/" + invoiceId, null, InvoiceResponse.class);
    }

    public InvoiceResponse createInvoice(String token, Long appointmentId, InvoiceFromAppointmentRequest request) {
        return exchange(token, HttpMethod.POST, backendBaseUrl + "/invoice/appointment/" + appointmentId, request, InvoiceResponse.class);
    }

    public ResponseEntity<byte[]> downloadInvoicePdf(String token, Long invoiceId) {
        return exchangeBinary(token, backendBaseUrl + "/invoice/pdf/" + invoiceId);
    }

    public List<CareResponse> getCares(String token) {
        return exchangeList(token, HttpMethod.GET, backendBaseUrl + "/care", null,
                new ParameterizedTypeReference<>() {
                });
    }

    public List<MedicationResponse> getMedications(String token) {
        return exchangeList(token, HttpMethod.GET, backendBaseUrl + "/medication", null,
                new ParameterizedTypeReference<>() {
                });
    }

    public List<AdoptionPetResponse> getCatalog(String species, String location, String gender, String age) {
        String url = UriComponentsBuilder.fromUriString(backendBaseUrl + "/adoption/pets")
                .queryParamIfPresent("species", optionalText(species))
                .queryParamIfPresent("location", optionalText(location))
                .queryParamIfPresent("gender", optionalText(gender))
                .queryParamIfPresent("age", optionalText(age))
                .build()
                .toUriString();

        return exchangeList(null, HttpMethod.GET, url, null, new ParameterizedTypeReference<>() {
        });
    }

    public List<AdoptionPetResponse> getManagedPets(String token) {
        return exchangeList(token, HttpMethod.GET, backendBaseUrl + "/adoption/pets/manage", null,
                new ParameterizedTypeReference<>() {
                });
    }

    public AdoptionPetResponse getManagedPet(String token, Long petId) {
        return exchange(token, HttpMethod.GET, backendBaseUrl + "/adoption/pets/manage/" + petId, null, AdoptionPetResponse.class);
    }

    public AdoptionPetResponse createPet(String token, AdoptionPetRequest request) {
        return exchange(token, HttpMethod.POST, backendBaseUrl + "/adoption/pets", request, AdoptionPetResponse.class);
    }

    public AdoptionPetResponse updatePet(String token, Long petId, AdoptionPetRequest request) {
        return exchange(token, HttpMethod.PUT, backendBaseUrl + "/adoption/pets/" + petId, request, AdoptionPetResponse.class);
    }

    public void deletePet(String token, Long petId) {
        exchange(token, HttpMethod.DELETE, backendBaseUrl + "/adoption/pets/" + petId, null, Void.class);
    }

    public AdoptionResponse registerAdoption(String token, Long petId, AdoptionRequest request) {
        return exchange(token, HttpMethod.POST, backendBaseUrl + "/adoption/pets/" + petId + "/adoptions", request, AdoptionResponse.class);
    }

    public List<AdoptionResponse> getAdoptionRecords(String token) {
        return exchangeList(token, HttpMethod.GET, backendBaseUrl + "/adoption/records", null,
                new ParameterizedTypeReference<>() {
                });
    }

    public AdoptionResponse getAdoptionRecord(String token, Long recordId) {
        return exchange(token, HttpMethod.GET, backendBaseUrl + "/adoption/records/" + recordId, null, AdoptionResponse.class);
    }

    private java.util.Optional<String> optionalText(String value) {
        return StringUtils.hasText(value) ? java.util.Optional.of(value.trim()) : java.util.Optional.empty();
    }

    private <T> T exchange(String token, HttpMethod method, String url, Object body, Class<T> responseType) {
        try {
            HttpEntity<?> entity = new HttpEntity<>(body, buildHeaders(token));
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw buildBackendException(ex);
        } catch (RestClientException ex) {
            throw new BackendServiceException(HttpStatus.SERVICE_UNAVAILABLE, "No fue posible conectar con el servicio del sistema. Intente nuevamente.");
        }
    }

    private ResponseEntity<byte[]> exchangeBinary(String token, String url) {
        try {
            HttpHeaders headers = buildBinaryHeaders(token);
            HttpEntity<?> entity = new HttpEntity<>(null, headers);
            return restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
        } catch (HttpStatusCodeException ex) {
            throw buildBackendException(ex);
        } catch (RestClientException ex) {
            throw new BackendServiceException(HttpStatus.SERVICE_UNAVAILABLE, "No fue posible conectar con el servicio del sistema. Intente nuevamente.");
        }
    }

    private <T> List<T> exchangeList(String token, HttpMethod method, String url, Object body,
                                     ParameterizedTypeReference<List<T>> responseType) {
        try {
            HttpEntity<?> entity = new HttpEntity<>(body, buildHeaders(token));
            ResponseEntity<List<T>> response = restTemplate.exchange(url, method, entity, responseType);
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (HttpStatusCodeException ex) {
            throw buildBackendException(ex);
        } catch (RestClientException ex) {
            throw new BackendServiceException(HttpStatus.SERVICE_UNAVAILABLE, "No fue posible conectar con el servicio del sistema. Intente nuevamente.");
        }
    }

    private HttpHeaders buildHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (StringUtils.hasText(token)) {
            headers.setBearerAuth(token);
        }

        return headers;
    }

    private HttpHeaders buildBinaryHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_PDF, MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));

        if (StringUtils.hasText(token)) {
            headers.setBearerAuth(token);
        }

        return headers;
    }

    private BackendServiceException buildBackendException(HttpStatusCodeException ex) {
        return new BackendServiceException(ex.getStatusCode(), resolveMessage(ex));
    }

    private String resolveMessage(HttpStatusCodeException ex) {
        String responseBody = ex.getResponseBodyAsString();

        if (StringUtils.hasText(responseBody)) {
            try {
                ApiErrorResponse errorResponse = objectMapper.readValue(responseBody, ApiErrorResponse.class);
                if (StringUtils.hasText(errorResponse.getMessage())) {
                    return errorResponse.getMessage();
                }
            } catch (Exception ignored) {
                return responseBody;
            }
        }

        HttpStatusCode statusCode = ex.getStatusCode();
        return "No fue posible completar la solicitud. Intente nuevamente.";
    }
}
