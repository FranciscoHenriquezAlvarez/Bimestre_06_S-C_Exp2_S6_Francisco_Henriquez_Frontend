package com.duoc.frontend.dto.request;

import java.util.ArrayList;
import java.util.List;

public class InvoiceFromAppointmentRequest {
    private List<Long> careIds = new ArrayList<>();
    private List<Long> medicationIds = new ArrayList<>();

    public List<Long> getCareIds() {
        return careIds;
    }

    public void setCareIds(List<Long> careIds) {
        this.careIds = careIds;
    }

    public List<Long> getMedicationIds() {
        return medicationIds;
    }

    public void setMedicationIds(List<Long> medicationIds) {
        this.medicationIds = medicationIds;
    }
}
