package se.centevo.endpoint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

import org.hibernate.annotations.Formula;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@RepositoryRestResource
interface AccountRepository extends ReadRepository<Account, Long>, EditableRepository<Account, Long> {
}

@Entity
@Table(schema = "PublicApi")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long accountId;

    @Size(min = 1, max = 50)
    @NotNull
    String code;

    @Size(min = 1, max = 100)
    @NotNull
    String description;

    @Size(min = 1, max = 50)
    @NotNull
    String accountTypeCode;

    @Size(min = 1, max = 50)
    String custodianCode;

    @Size(min = 1, max = 34)
    String iban;

    @Size(min = 1, max = 50)
    String currencyCode;

    @NotNull
    LocalDate startDate;

    LocalDate endDate;

    @NotNull
    boolean collateral;

    @NotNull
    boolean reinvestFundDividend;

    @LastModifiedDate
    @Formula("COALESCE(UpdateDate, CreateDate)")
    LocalDateTime lastModifiedDate;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "accountId", updatable = false, nullable = false)
    Set<AccountOwner> accountOwners;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "accountId", updatable = false, nullable = false)
    Set<AccountOmniAccount> omniAccounts;

    @Version
    @Column(name = "VersionId")
    Long version;

    public Set<AccountOwner> getAccountOwners() {
        return accountOwners;
    }

    public void setAccountOwners(Set<AccountOwner> accountOwners) {
        if (this.accountOwners == null
                || this.accountOwners == accountOwners
                || accountOwners instanceof PersistentCollection) {
            this.accountOwners = accountOwners;

        } else {
            this.accountOwners.clear();
            this.accountOwners.addAll(accountOwners);
        }
    }

    public Set<AccountOmniAccount> getOmniAccounts() {
        return omniAccounts;
    }

    public void setOmniAccounts(Set<AccountOmniAccount> omniAccounts) {
        if (this.omniAccounts == null
                || this.omniAccounts == omniAccounts
                || omniAccounts instanceof PersistentCollection) {
            this.omniAccounts = omniAccounts;

        } else {
            this.omniAccounts.clear();
            this.omniAccounts.addAll(omniAccounts);
        }
    }
}

@Entity
@Table(schema = "PublicApi")
@Getter
@Setter
class AccountOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    Long accountOwnerId;

    String customerCode;
    BigDecimal weight;

    @Override
    public final boolean equals(Object that) {
        if (this == that)
            return true;
        if (that == null)
            return false;
        Class<?> oEffectiveClass = that instanceof HibernateProxy
                ? ((HibernateProxy) that).getHibernateLazyInitializer().getPersistentClass()
                : that.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass)
            return false;
        return getAccountOwnerId() != null && Objects.equals(getAccountOwnerId(), ((AccountOwner) that).getAccountOwnerId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}

@Entity
@Table(schema = "PublicApi")
@Getter
@Setter
class AccountOmniAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    Long accountOmniAccountId;

    String omniAccountCode;

    @Override
    public final boolean equals(Object that) {
        if (this == that)
            return true;
        if (that == null)
            return false;
        Class<?> oEffectiveClass = that instanceof HibernateProxy
                ? ((HibernateProxy) that).getHibernateLazyInitializer().getPersistentClass()
                : that.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass)
            return false;
        return getAccountOmniAccountId() != null && Objects.equals(getAccountOmniAccountId(), ((AccountOmniAccount) that).getAccountOmniAccountId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}