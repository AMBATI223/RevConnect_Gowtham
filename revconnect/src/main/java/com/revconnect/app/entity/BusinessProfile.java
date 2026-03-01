package com.revconnect.app.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "business_profiles")
public class BusinessProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "USER_ID", nullable = false)
    private User user;

    @Column(nullable = false)
    private String businessName;

    private String category;
    private String address;
    private String businessHours;
    private String contactLinks;

    public BusinessProfile() {
    }

    public BusinessProfile(User user, String businessName, String category, String address, String businessHours,
            String contactLinks) {
        this.user = user;
        this.businessName = businessName;
        this.category = category;
        this.address = address;
        this.businessHours = businessHours;
        this.contactLinks = contactLinks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }

    public String getContactLinks() {
        return contactLinks;
    }

    public void setContactLinks(String contactLinks) {
        this.contactLinks = contactLinks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BusinessProfile that = (BusinessProfile) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
