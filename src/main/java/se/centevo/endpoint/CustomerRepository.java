package se.centevo.endpoint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;


public interface CustomerRepository extends EditableRepository<Customer, Long> {
}

@AllArgsConstructor
@Data
class Customer {
    @Id()
    Long customerId;
    @NotNull
    @Size(min = 1, max = 50)
    String code;
    @ReadOnlyProperty
    String description;
    @Size(min = 1, max = 100)
    String firstName;
    @Size(min = 1, max = 100)
    String surName;
    @Size(min = 1, max = 50)
    String organizationId;
    @NotNull
    String customerTypeCode;
    @NotNull
    String regionCode;
    String managerCode;
    @NotNull
    String languageCode;
    String targetPortfolioCode;
    String targetAccountCode;
    Boolean vat;
    Boolean tin;
    String mifidInvestorTypeCode;
    String mifidKnowledgeAndExperienceCode;
    String mifidAbilityToBearLossesCode;
    LocalDate lastReportDate;
    BigDecimal mifidLastReportedDrawdown;
    String lei;
    LocalDate dateOfBirth;
    LocalDate startDate;
    LocalDate endDate;
    String citizenshipRegionCode;
    LocalDate pepStatusDate;
    String postalCode;
    String city;
    String phoneNumber;
    String mobile;
    String email;

    @LastModifiedDate LocalDateTime lastModifiedDate;
    @Version Long version;
}


