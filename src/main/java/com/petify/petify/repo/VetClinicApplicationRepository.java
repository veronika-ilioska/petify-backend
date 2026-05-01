package com.petify.petify.repo;

import com.petify.petify.domain.VetClinicApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VetClinicApplicationRepository extends JpaRepository<VetClinicApplication, Long> {
    List<VetClinicApplication> findAllByOrderBySubmittedAtDesc();
}
