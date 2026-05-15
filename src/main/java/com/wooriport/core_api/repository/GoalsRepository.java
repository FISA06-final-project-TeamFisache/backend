package com.wooriport.core_api.repository;

import com.wooriport.core_api.domain.Goals;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GoalsRepository extends JpaRepository<Goals, UUID> {
}
