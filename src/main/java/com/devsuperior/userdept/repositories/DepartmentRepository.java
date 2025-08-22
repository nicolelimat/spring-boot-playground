package com.devsuperior.userdept.repositories;

import com.devsuperior.userdept.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
