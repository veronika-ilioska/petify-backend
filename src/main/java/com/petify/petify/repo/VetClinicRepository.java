package com.petify.petify.repo;

import com.petify.petify.domain.VetClinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VetClinicRepository extends JpaRepository<VetClinic, Long> {
    java.util.List<VetClinic> findAllByOrderByNameAsc();
    Optional<VetClinic> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
