package se.centevo.endpoint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.Formula;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@RepositoryRestResource
interface BrokerRepository extends ReadRepository<Broker, Long>, EditableRepository<Broker, Long>  {
}


@Entity
@Table(schema="PublicApi")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class Broker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long brokerId;

    @Size(min = 1, max = 50)
    String brokerParentCode;

    @Size(min = 1, max = 50)
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @Size(min = 1, max = 50)
    String financialInstitutionCode;

    @Size(min = 1, max = 20)
    String lei;

    @NotNull
    boolean canUpdatePortfolioOrder;

    @NotNull
    LocalDateTime createDate;

    LocalDateTime updateDate;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface CustomerRepository extends ReadRepository<Customer, Long>, EditableRepository<Customer, Long>  {
}


@Entity
@Table(schema="PublicApi")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long customerId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 201)
    String description;

    @Size(min = 1, max = 100)
    String firstName;

    @Size(min = 1, max = 100)
    String surName;

    @Size(min = 1, max = 50)
    String organizationId;

    @Size(min = 1, max = 50)
    @NotNull
    String customerTypeCode;

    @Size(min = 1, max = 50)
    @NotNull
    String regionCode;

    @Size(min = 1, max = 50)
    String managerCode;

    @Size(min = 1, max = 50)
    @NotNull
    String languageCode;

    @Size(min = 1, max = 50)
    String targetPortfolioCode;

    @Size(min = 1, max = 50)
    String targetAccountCode;

    @Size(min = 1, max = 20)
    String vat;

    @Size(min = 1, max = 20)
    String tin;

    @Size(min = 1, max = 50)
    String mifidInvestorTypeCode;

    @Size(min = 1, max = 50)
    String mifidKnowledgeAndExperienceCode;

    @Size(min = 1, max = 50)
    String mifidAbilityToBearLossesCode;

    LocalDate lastReportDate;

    Integer mifidLastReportedDrawdown;

    @Size(min = 1, max = 20)
    String lei;

    LocalDate dateOfBirth;

    LocalDate startDate;

    LocalDate endDate;

    @Size(min = 1, max = 50)
    String citizenshipRegionCode;

    LocalDateTime pEPStatusDate;

    @Size(min = 1, max = 100)
    String address;

    @Size(min = 1, max = 100)
    String postalCode;

    @Size(min = 1, max = 50)
    String city;

    @Size(min = 1, max = 50)
    String phoneNumber;

    @Size(min = 1, max = 50)
    String mobile;

    @Size(min = 1, max = 1024)
    String email;

    @NotNull
    LocalDateTime createDate;

    LocalDateTime updateDate;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface ExchangeRepository extends ReadRepository<Exchange, Long>, EditableRepository<Exchange, Long>  {
}


@Entity
@Table(schema="PublicApi")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class Exchange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long exchangeId;

    @Size(min = 1, max = 50)
    String exchangeParentCode;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @Size(min = 1, max = 50)
    @NotNull
    String regionCode;

    @Size(min = 1, max = 50)
    @NotNull
    String calendarTypeCode;

    @Size(min = 1, max = 50)
    String swiftCode;

    @Size(min = 1, max = 4)
    String mic;

    @NotNull
    LocalDateTime createDate;

    LocalDateTime updateDate;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface InstrumentRepository extends ReadRepository<Instrument, Long>, EditableRepository<Instrument, Long>  {
}


@Entity
@Table(schema="PublicApi")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class Instrument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long instrumentId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @Size(min = 1, max = 50)
    @NotNull
    String instrumentTypeCode;

    @Size(min = 1, max = 50)
    String instrumentParentCode;

    @Size(min = 1, max = 50)
    @NotNull
    String issuerCode;

    @Size(min = 1, max = 50)
    @NotNull
    String principalCurrencyCode;

    @Size(min = 1, max = 50)
    @NotNull
    String incomeCurrencyCode;

    @Size(min = 1, max = 50)
    @NotNull
    String regionCode;

    @Size(min = 1, max = 50)
    @NotNull
    String taxRegionCode;

    @Size(min = 1, max = 50)
    String sectorCode;

    @Size(min = 1, max = 50)
    String exchangeCode;

    @Digits(integer = 13, fraction = 9)
    @NotNull
    BigDecimal priceMultiplier;

    @Digits(integer = 7, fraction = 9)
    BigDecimal strikePrice;

    @Digits(integer = 13, fraction = 9)
    BigDecimal shareRatio;

    LocalDate issueDate;

    LocalDate expireDate;

    Integer sharesPerUnit;

    @Size(min = 1, max = 50)
    String portfolioCode;

    @Digits(integer = 7, fraction = 9)
    BigDecimal priceDeviation;

    @NotNull
    boolean ethical;

    @NotNull
    boolean environmental;

    @Size(min = 1, max = 50)
    String assetClassCode;

    @NotNull
    boolean subscriptionInUnits;

    @NotNull
    boolean subscriptionInAmount;

    LocalDateTime nextSubscriptionTradeDeadline;

    @NotNull
    boolean redemptionInUnits;

    @NotNull
    boolean redemptionInAmount;

    LocalDateTime nextRedemptionTradeDeadline;

    @Digits(integer = 7, fraction = 9)
    BigDecimal tradeLot;

    @Size(min = 1, max = 100)
    String manager;

    @Size(min = 1, max = 50)
    String benchmarkInstrumentCode;

    @Size(min = 1, max = 50)
    String organizationNumber;

    @Size(min = 1, max = 12)
    String isin;

    @Size(min = 1, max = 50)
    @NotNull
    String marketCalendarTypeCode;

    @Size(min = 1, max = 50)
    @NotNull
    String priceCalendarTypeCode;

    @Size(min = 1, max = 50)
    String tradeFrequencyCode;

    LocalDateTime cutOffTime;

    @Digits(integer = 10, fraction = 2)
    BigDecimal subscriptionFeeAmount;

    @Digits(integer = 3, fraction = 6)
    BigDecimal subscriptionFeePercent;

    @Digits(integer = 10, fraction = 2)
    BigDecimal redemptionFeeAmount;

    @Digits(integer = 3, fraction = 6)
    BigDecimal redemptionFeePercent;

    @Size(min = 1, max = 255)
    String additionalFeeDescription;

    @Digits(integer = 10, fraction = 2)
    BigDecimal minimumInitialSubscription;

    @Digits(integer = 10, fraction = 2)
    BigDecimal minimumMonthlySubscription;

    @Digits(integer = 10, fraction = 2)
    BigDecimal minimumSubscription;

    Integer calculationFractions;

    String extendedDescription;

    LocalDate subscriptionStartDate;

    LocalDate subscriptionEndDate;

    LocalDate subscriptionSettlementDate;

    @NotNull
    boolean priceIncludeInterest;

    @Digits(integer = 4, fraction = 2)
    BigDecimal leverage;

    @Size(min = 1, max = 1)
    String optionRight;

    @Digits(integer = 6, fraction = 6)
    BigDecimal votingRightRatio;

    @NotNull
    boolean leveragedFinancialInstrumentOrContingentLiabilityInstrument;

    @NotNull
    Integer settlementDaysSell;

    @NotNull
    Integer settlementDaysBuy;

    @Size(min = 1, max = 50)
    String couponDateStepConventionCode;

    @Size(min = 1, max = 50)
    String couponSettlementDateStepConventionCode;

    Integer recordDays;

    @Digits(integer = 7, fraction = 9)
    BigDecimal redemptionPrice;

    @Size(min = 1, max = 50)
    String dayCountMethodCode;

    @Size(min = 1, max = 50)
    String interestFrequencyCode;

    LocalDate firstCouponDate;

    @Digits(integer = 13, fraction = 9)
    BigDecimal couponRate;

    boolean investorTypeRetail;

    boolean investorTypeProfessional;

    boolean investorTypePerSeProfessional;

    boolean investorTypeElectiveProfessional;

    boolean investorTypeEligibleCounterparty;

    boolean knowledgeAndExperienceBasic;

    boolean knowledgeAndExperienceInformed;

    boolean knowledgeAndExperienceAdvanced;

    @Digits(integer = 3, fraction = 6)
    BigDecimal abilityToBearLimitedCapitalLossLevel;

    boolean abilityToBearLossesLimitedCapitalLoss;

    boolean abilityToBearLossesLossBeyondCapital;

    boolean abilityToBearLossesNoCapitalGuarantee;

    boolean abilityToBearLossesNoCapitalLoss;

    boolean returnProfilePreservation;

    boolean returnProfileGrowth;

    boolean returnProfileIncome;

    boolean returnProfileHedging;

    boolean returnProfileOptionOrLeveraged;

    boolean returnProfileOther;

    boolean returnProfileSpecificInvestmentNeed;

    @Size(min = 1, max = 50)
    String mifidTimeHorizonCode;

    @Size(min = 1, max = 50)
    String mifidRiskToleranceCode;

    @NotNull
    boolean mifidDistributionStrategyExecutionOnlyRetail;

    @NotNull
    boolean mifidDistributionStrategyExecutionOnlyProfessional;

    @NotNull
    boolean mifidDistributionStrategyExecutionAppropriatenessAssessmentRetail;

    @NotNull
    boolean mifidDistributionStrategyExecutionAppropriatenessAssessmentProfessional;

    @NotNull
    boolean mifidDistributionStrategyInvestmentAdviceRetail;

    @NotNull
    boolean mifidDistributionStrategyInvestmentAdviceProfessional;

    @NotNull
    boolean mifidDistributionStrategyPortfolioManagementRetail;

    @NotNull
    boolean mifidDistributionStrategyPortfolioManagementProfessional;

    @Size(min = 1, max = 50)
    String referenceRateInstrumentCode;

    @Digits(integer = 7, fraction = 9)
    BigDecimal referenceRateMargin;

    @NotNull
    boolean referenceRateDeemedToZero;

    @NotNull
    LocalDateTime createDate;

    LocalDateTime updateDate;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface IssuerRepository extends ReadRepository<Issuer, Long>, EditableRepository<Issuer, Long>  {
}


@Entity
@Table(schema="PublicApi")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class Issuer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long issuerId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @Size(min = 1, max = 50)
    @NotNull
    String regionCode;

    @Size(min = 1, max = 50)
    String financialInstitutionCode;

    @Size(min = 1, max = 50)
    String organizationNumber;

    @Size(min = 1, max = 20)
    String lei;

    @NotNull
    LocalDateTime createDate;

    LocalDateTime updateDate;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface ManagerRepository extends ReadRepository<Manager, Long>, EditableRepository<Manager, Long>  {
}


@Entity
@Table(schema="PublicApi")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class Manager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long managerId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @Size(min = 1, max = 50)
    @NotNull
    String regionCode;

    @Size(min = 1, max = 50)
    String citizenshipRegionCode;

    @Size(min = 1, max = 50)
    String organizationId;

    @Size(min = 1, max = 50)
    String mobil;

    @Size(min = 1, max = 20)
    String lei;

    @NotNull
    LocalDateTime createDate;

    LocalDateTime updateDate;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface PortfolioRepository extends ReadRepository<Portfolio, Long>, EditableRepository<Portfolio, Long>  {
}


@Entity
@Table(schema="PublicApi")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long portfolioId;

    @Size(min = 1, max = 50)
    String portfolioParentCode;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 100)
    @NotNull
    String description;

    @Size(min = 1, max = 50)
    @NotNull
    String currencyCode;

    @Size(min = 1, max = 50)
    @NotNull
    String portfolioTypeCode;

    @Size(min = 1, max = 50)
    String managerCode;

    @Size(min = 1, max = 50)
    String modelCode;

    @NotNull
    LocalDate startDate;

    LocalDate endDate;

    @Size(min = 1, max = 50)
    String bookValueMethodCode;

    @NotNull
    Integer scenarioCode;

    LocalDateTime performanceStart;

    LocalDateTime fullInvestedStart;

    @NotNull
    boolean blocked;

    @Size(min = 1, max = 50)
    String blockedText;

    @Size(min = 1, max = 50)
    String benchmarkPortfolioCode;

    @Size(min = 1, max = 50)
    String benchmarkInstrumentCode;

    @NotNull
    boolean performance;

    @NotNull
    boolean forceTransactions;

    @NotNull
    boolean limitToCash;

    @Size(min = 1, max = 50)
    String savingsplanPortfolioCode;

    String extendedDescription;

    @Size(min = 1, max = 50)
    String insuredCustomerCode;

    @Size(min = 1, max = 50)
    String investmentProfilePortfolioCode;

    @Size(min = 1, max = 50)
    String targetAccountCode;

    @Size(min = 1, max = 50)
    String mifidReturnProfileCode;

    @Size(min = 1, max = 50)
    String mifidTimeHorizonCode;

    @Size(min = 1, max = 50)
    String mifidRiskToleranceCode;

    @Size(min = 1, max = 50)
    String mifidDistributionStrategyCode;

    @Size(min = 1, max = 50)
    String discountTemplateCode;

    @NotNull
    LocalDateTime createDate;

    LocalDateTime updateDate;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}


