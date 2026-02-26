package ru.practicum.main.locations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.main.locations.model.LocationArea;

public interface LocationAreaRepository extends JpaRepository<LocationArea, Long>, JpaSpecificationExecutor<LocationArea> {
}