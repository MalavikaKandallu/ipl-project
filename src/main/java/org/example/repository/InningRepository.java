package org.example.repository;

import org.example.entity.Inning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InningRepository extends JpaRepository<Inning, Long> {
    // Custom queries can be added here if needed
}
