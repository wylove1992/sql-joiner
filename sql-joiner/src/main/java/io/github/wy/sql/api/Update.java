package io.github.wy.sql.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.*;

import static io.github.wy.sql.util.TRY;
import static io.github.wy.sql.util.stream;
import static java.util.stream.Collectors.joining;

public class Update<T> {
    private final TableInfo<T> table;
    private T value;
    private boolean ignoreNulls = true;
    private String whereCondition = null;
    private String sets = null;
    private final Map<String, Object> paramMap = new HashMap<>();
    
    public Update(final TableInfo<T> table) {
        this.table = table;
    }
    
    public Update<T> VALUE(T value) {
        this.value = value;
        Map<String, String> mapped = table.mapped();
        if (value == null) throw new RuntimeException("value is null");
        
        Map<String, Object> paramMap = new HashMap<>();
        List<String> columns = new ArrayList<>();
        stream(value.getClass().getDeclaredFields()).forEach(field->{
            field.setAccessible(true);
            Object v = TRY(()->field.get(this.value));
            if (v != null) {
                columns.add(mapped.get(field.getName()) + "=:" + field.getName());
                //插入的值
                paramMap.put(field.getName(), v);
            } else if (!ignoreNulls) {
                columns.add(mapped.get(field.getName()) + "= NULL");
            }
        });
        this.sets = String.join(",", columns);
        this.paramMap.putAll(paramMap);
        return this;
    }
    
    public Update<T> SET(String... parts) {
        this.sets = stream(parts).collect(joining(","));
        return this;
    }
    
    public Update<T> notIgnoreNulls() {
        this.ignoreNulls = false;
        return this;
    }
    
    public Update<T> WHERE(String... parts) {
        whereCondition = " WHERE " + stream(parts).collect(joining(""));
        return this;
    }
    
    public int jdbc(JdbcTemplate jdbcTemplate) {
        Objects.requireNonNull(sets, "语句错误");
        StringBuilder sql = new StringBuilder("UPDATE ").append(table.tableName()).append(" ").append(table.tableAlias()).append(" ");
        sql.append("SET ").append(sets).append(" ").append(whereCondition);
        System.out.println(sql);
        paramMap.putAll(table.paramMap());
        return new NamedParameterJdbcTemplate(jdbcTemplate).update(sql.toString(), paramMap);
    }
}
