package ru.practicum.main.locations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationAreaDto {

    private Long id;
    private String name;
    private String type;
    private BigDecimal lat;
    private BigDecimal lon;
    private BigDecimal radiusKm;
    private String createdOn;
}