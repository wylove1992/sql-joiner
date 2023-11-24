package io.github.wy.sql;

import io.github.wy.sql.api.TableInfo;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * @author rycat
 * @since 2023/11/22
 */
public interface util {
    AtomicLong inc = new AtomicLong(0);
    
    static <E> Stream<E> stream(Collection<E> collection) {
        return collection == null ? Stream.empty() : collection.stream();
    }
    
    static <E> Stream<E> stream(E[] array) {
        return array == null ? Stream.empty() : Arrays.stream(array);
    }
    
    static <E extends CharSequence> Optional<E> notEmpty(E value) {
        if (value != null && value.length() > 0) return Optional.of(value);
        return Optional.empty();
    }
    
    static String getNameKey() {
        return "name_key_" + Math.abs(inc.incrementAndGet());
    }
    
    static Map<String, Object> mergeParam(TableInfo<?> table, TableInfo<?>... otherTable) {
        Map<String, Object> all = new HashMap<>(table.paramMap());
        stream(otherTable).forEach(it->all.putAll(it.paramMap()));
        return all;
    }
    
    static Map<String, Object> mergeParam(TableInfo<?>[] otherTable) {
        Map<String, Object> all = new HashMap<>();
        stream(otherTable).forEach(it->all.putAll(it.paramMap()));
        return all;
    }
    
    static Map<String, Object> mergeParam(Collection<TableInfo<?>> otherTable) {
        Map<String, Object> all = new HashMap<>();
        stream(otherTable).forEach(it->all.putAll(it.paramMap()));
        return all;
    }
    
    static String q(String str) {
        //return "`"+str+"`";
        return "\""+str+"\"";
    }
    
    static <R> R TRY(CheckedSupplier<R> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            sneakyThrow(e);
            return null;
        }
    }
    interface CheckedSupplier<E> {
        E get() throws Exception;
    }
    @SuppressWarnings("unchecked")
    static <T extends Exception, R> R sneakyThrow(Exception t) throws T {
        throw (T) t;
    }
}
