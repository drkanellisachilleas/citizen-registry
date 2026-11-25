package gr.assignment.citizens.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Pattern;

@JsonIgnoreProperties(ignoreUnknown = false)
public class CitizenUpdate {

    @Pattern(regexp = "[0-9]{9}", message = "Tax ID must contain exactly 9 digits")
    private String taxId;

    private String address;

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
