package org.example.repository;

import org.example.entity.Powerplay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PowerplayRepository extends JpaRepository<Powerplay, Long> {
    // Custom queries can be added here if needed
}

