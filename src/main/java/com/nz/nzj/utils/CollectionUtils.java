package com.nz.nzj.utils;

import lombok.experimental.UtilityClass;

import java.util.Collection;

@UtilityClass
public class CollectionUtils {
    public static <T> boolean intersect(Collection<T> a, Collection<T> b) {
        return a.stream().anyMatch(b::contains);
    }
}
