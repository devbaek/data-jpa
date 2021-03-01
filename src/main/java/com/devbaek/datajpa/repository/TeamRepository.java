package com.devbaek.datajpa.repository;

import com.devbaek.datajpa.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
