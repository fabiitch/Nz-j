package com.nz.nzj.utils;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class BigCollectionUtils {

    /**
     *  warning NOT USE ON HOTCODE
     */
    public static <T> boolean containsAny(Collection<T> a, Collection<T> b) {
        Set<T> set = new HashSet<>(b);
        return a.stream().anyMatch(set::contains);
    }
}
