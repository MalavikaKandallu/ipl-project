package org.example.repository;

import org.example.entity.Over;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OverRepository extends JpaRepository<Over, Long> {
    // Custom queries can be added here if needed
}
