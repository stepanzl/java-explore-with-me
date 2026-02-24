package ru.practicum.main.util;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import ru.practicum.main.exception.BadRequestException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Mapper(componentModel = "spring")
public class DateTimeMapper {

    @Named("asLocalDateTime")
    public LocalDateTime asLocalDateTime(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DateTimeFormat.FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Invalid date time: " + value);
        }
    }

    @Named("asString")
    public String asString(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.format(DateTimeFormat.FORMATTER);
    }
}