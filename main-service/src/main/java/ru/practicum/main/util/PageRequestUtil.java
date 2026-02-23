package ru.practicum.main.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public final class PageRequestUtil {

    private PageRequestUtil() {
    }

    public static PageRequest from(int from, int size) {
        return PageRequest.of(from / size, size, Sort.unsorted());
    }

    public static PageRequest from(int from, int size, Sort sort) {
        return PageRequest.of(from / size, size, sort == null ? Sort.unsorted() : sort);
    }
}