package io.github.wy.sql.api;

import io.github.wy.sql.util;

import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * @author DELL
 * @since 2023-11-23
 */
public interface TableInfo<T> {
    Map<String, Object> paramMap();
    
    String tableAlias();
    
    String tableName();
    
    Class<T> modelClass();
    
    Map<String, String> mapped();
    
    String database();
    
    String q(String str);
    
    default String STAR() {
        return tableAlias() + ".*";
    }
    
    default String all() {
        Map<String, String> mapped = mapped();
        String alias = tableAlias();
        return mapped.keySet().stream().map(it->alias + "." + mapped.get(it) + (" AS " + q(it))).collect(joining(","));
    }
    
    default String all_() {
        Map<String, String> mapped = mapped();
        String alias = tableAlias();
        return mapped.keySet().stream().map(it->alias + "." + it + (" AS " + q(it))).collect(joining(","));
    }
    
    default String table() {
        return tableName() + " " + tableAlias();
    }
    
    default String withAlias(String field) {
        return tableAlias() + "." + q(field);
    }
    
    /**
     * 通用where条件组合
     *
     * @param fieldName
     * @param op
     * @param value
     * @return
     */
    default String fieldOf(String fieldName, Op op, Object value) {
        String nameKey = util.getNameKey();
        paramMap().put(nameKey, value);
        return op.get(tableAlias() + "." + fieldName, ":" + nameKey);
    }
    
    default String ops(String left, Op op, Object value) {
        String nameKey = util.getNameKey();
        paramMap().put(nameKey, value);
        return op.get(left, ":" + nameKey);
    }
}
