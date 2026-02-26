package ru.practicum.main.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageRequestUtil {

    private PageRequestUtil() {
    }

    public static Pageable from(int from, int size) {
        return new OffsetBasedPageRequest(from, size, Sort.unsorted());
    }

    public static Pageable from(int from, int size, Sort sort) {
        return new OffsetBasedPageRequest(from, size, sort == null ? Sort.unsorted() : sort);
    }
}