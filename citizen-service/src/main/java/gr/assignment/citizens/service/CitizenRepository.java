package gr.assignment.citizens.service;

import gr.assignment.citizens.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitizenRepository extends JpaRepository<Citizen, String> {
}
