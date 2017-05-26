package com.beautifulbeanbuilder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;

/**
 * Created by dphillips on 5/25/17.
 */
public class Collector {

    public static final Multimap<String, Object> COLLECTOR = HashMultimap.create();


    public static <T> Set<T> get(String key) {
        final Collection<T> objects = (Collection<T>) COLLECTOR.get(key);
        return Sets.newHashSet(objects);
    }

}
