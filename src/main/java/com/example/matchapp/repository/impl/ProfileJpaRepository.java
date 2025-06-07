package com.example.matchapp.repository.impl;

import com.example.matchapp.model.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository interface for ProfileEntity.
 * This interface is used by JpaProfileRepository to access the database.
 */
@Repository
public interface ProfileJpaRepository extends JpaRepository<ProfileEntity, String> {
    // Spring Data JPA will provide implementations for common methods
    // Custom query methods can be added here if needed
}