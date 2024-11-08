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
interface MifidAbilityToBearLossesRepository extends ReadRepository<MifidAbilityToBearLosses, Long>  {
}


@Entity
@Table(schema="Authority")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class MifidAbilityToBearLosses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long mifidAbilityToBearLossesId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 150)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface MifidDistributionStrategyRepository extends ReadRepository<MifidDistributionStrategy, Long>  {
}


@Entity
@Table(schema="Authority")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class MifidDistributionStrategy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long mifidDistributionStrategyId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 150)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface MifidInvestorTypeRepository extends ReadRepository<MifidInvestorType, Long>  {
}


@Entity
@Table(schema="Authority")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class MifidInvestorType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long mifidInvestorTypeId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 150)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface MifidKnowledgeAndExperienceRepository extends ReadRepository<MifidKnowledgeAndExperience, Long>  {
}


@Entity
@Table(schema="Authority")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class MifidKnowledgeAndExperience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long mifidKnowledgeAndExperienceId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 150)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface MifidReturnProfileRepository extends ReadRepository<MifidReturnProfile, Long>  {
}


@Entity
@Table(schema="Authority")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class MifidReturnProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long mifidReturnProfileId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 150)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface MifidRiskToleranceRepository extends ReadRepository<MifidRiskTolerance, Long>  {
}


@Entity
@Table(schema="Authority")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class MifidRiskTolerance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long mifidRiskToleranceId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 150)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface MifidTimeHorizonRepository extends ReadRepository<MifidTimeHorizon, Long>  {
}


@Entity
@Table(schema="Authority")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class MifidTimeHorizon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long mifidTimeHorizonId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 150)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface AccountTypeRepository extends ReadRepository<AccountType, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class AccountType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long accountTypeId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface AssetClassRepository extends ReadRepository<AssetClass, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class AssetClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long assetClassId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface BookValueMethodRepository extends ReadRepository<BookValueMethod, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class BookValueMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long bookValueMethodId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface CalendarTypeRepository extends ReadRepository<CalendarType, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class CalendarType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long calendarTypeId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface ContactTypeRepository extends ReadRepository<ContactType, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class ContactType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long contactTypeId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface CustodianRepository extends ReadRepository<Custodian, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class Custodian {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long custodianId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface CustomerTypeRepository extends ReadRepository<CustomerType, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class CustomerType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long customerTypeId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface DateStepConventionRepository extends ReadRepository<DateStepConvention, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class DateStepConvention {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long dateStepConventionId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface DayCountMethodRepository extends ReadRepository<DayCountMethod, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class DayCountMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long dayCountMethodId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface DiscountTemplateRepository extends ReadRepository<DiscountTemplate, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class DiscountTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long discountTemplateId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface FinancialInstitutionRepository extends ReadRepository<FinancialInstitution, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class FinancialInstitution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long financialInstitutionId;

    @Size(min = 1, max = 50)
    String code;

    @Size(min = 1, max = 50)
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface InstrumentTypeRepository extends ReadRepository<InstrumentType, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class InstrumentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long instrumentTypeId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface InterestFrequencyRepository extends ReadRepository<InterestFrequency, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class InterestFrequency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long interestFrequencyId;

    @Size(min = 1, max = 50)
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface LanguageRepository extends ReadRepository<Language, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long languageId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface PortfolioTypeRepository extends ReadRepository<PortfolioType, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class PortfolioType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long portfolioTypeId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface RegionRepository extends ReadRepository<Region, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long regionId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface SectorRepository extends ReadRepository<Sector, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class Sector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long sectorId;

    @Size(min = 1, max = 50)
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface SourceRepository extends ReadRepository<Source, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long sourceId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface SwiftCodeRepository extends ReadRepository<SwiftCode, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class SwiftCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long swiftCodeId;

    @Size(min = 1, max = 50)
    String code;

    @Size(min = 1, max = 50)
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface TradeFrequencyRepository extends ReadRepository<TradeFrequency, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class TradeFrequency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long tradeFrequencyId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 100)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}

@RepositoryRestResource
interface TransactionTypeRepository extends ReadRepository<TransactionType, Long>  {
}


@Entity
@Table(schema="Core")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
class TransactionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long transactionTypeId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 50)
    @NotNull
    String description;

    @LastModifiedDate 
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;
    
    @Version 
    @Column(name="VersionId") 
    Long version;
}


