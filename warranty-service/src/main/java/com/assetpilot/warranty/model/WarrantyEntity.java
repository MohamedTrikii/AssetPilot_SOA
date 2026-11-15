package com.assetpilot.warranty.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * Entité JPA représentant une garantie d'équipement dans la base de données dédiée du Warranty Service.
 */
@Entity
@Table(name = "warranties")
public class WarrantyEntity {

    @Id
    @Column(nullable = false, length = 64)
    private String id;

    @Column(nullable = false, unique = true, length = 64)
    private String assetId;

    @Column(nullable = false, unique = true, length = 64)
    private String contractNumber;

    @Column(nullable = false, length = 100)
    private String provider;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WarrantyStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CoverageType coverageType;

    @Column(nullable = false, length = 100)
    private String slaLevel;

    @Column(length = 500)
    private String terms;

    @Column(length = 100)
    private String supportContact;

    public enum WarrantyStatus {
        ACTIVE,
        EXPIRED,
        VOIDED,
        PENDING
    }

    public enum CoverageType {
        STANDARD,
        PREMIUM,
        EXTENDED,
        PARTS_ONLY,
        LABOR_ONLY
    }

    public WarrantyEntity() {
    }

    public WarrantyEntity(String id, String assetId, String contractNumber, String provider,
                          LocalDate startDate, LocalDate endDate, WarrantyStatus status,
                          CoverageType coverageType, String slaLevel, String terms, String supportContact) {
        this.id = id;
        this.assetId = assetId;
        this.contractNumber = contractNumber;
        this.provider = provider;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.coverageType = coverageType;
        this.slaLevel = slaLevel;
        this.terms = terms;
        this.supportContact = supportContact;
    }

    public boolean isCovered() {
        LocalDate now = LocalDate.now();
        return this.status == WarrantyStatus.ACTIVE 
                && !now.isBefore(this.startDate) 
                && !now.isAfter(this.endDate);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public WarrantyStatus getStatus() {
        return status;
    }

    public void setStatus(WarrantyStatus status) {
        this.status = status;
    }

    public CoverageType getCoverageType() {
        return coverageType;
    }

    public void setCoverageType(CoverageType coverageType) {
        this.coverageType = coverageType;
    }

    public String getSlaLevel() {
        return slaLevel;
    }

    public void setSlaLevel(String slaLevel) {
        this.slaLevel = slaLevel;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public String getSupportContact() {
        return supportContact;
    }

    public void setSupportContact(String supportContact) {
        this.supportContact = supportContact;
    }
}
