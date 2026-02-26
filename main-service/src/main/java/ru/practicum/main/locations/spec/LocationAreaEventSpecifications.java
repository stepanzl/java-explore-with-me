package ru.practicum.main.locations.spec;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.main.events.model.Event;
import ru.practicum.main.locations.model.LocationArea;

import java.math.BigDecimal;

public final class LocationAreaEventSpecifications {

    private LocationAreaEventSpecifications() {
    }

    public static Specification<Event> within(LocationArea area) {
        if (area == null) return null;

        BigDecimal lat = area.getLat();
        BigDecimal lon = area.getLon();
        BigDecimal radiusKm = area.getRadiusKm();

        return (root, query, cb) -> cb.lessThanOrEqualTo(
                cb.function(
                        "distance",
                        Double.class,
                        root.get("location").get("lat"),
                        root.get("location").get("lon"),
                        cb.literal(lat),
                        cb.literal(lon)
                ),
                cb.literal(radiusKm.doubleValue())
        );
    }
}