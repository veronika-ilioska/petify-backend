package com.petify.petify.repo;

import com.petify.petify.domain.VetClinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VetClinicRepository extends JpaRepository<VetClinic, Long> {
    java.util.List<VetClinic> findAllByOrderByNameAsc();
}
