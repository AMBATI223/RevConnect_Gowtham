package com.revconnect.app.repository;

import com.revconnect.app.entity.BusinessProfile;
import com.revconnect.app.entity.ProductServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductServiceItemRepository extends JpaRepository<ProductServiceItem, Long> {
    List<ProductServiceItem> findByBusinessProfile(BusinessProfile businessProfile);
}
