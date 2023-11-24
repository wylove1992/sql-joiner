package io.github.wy.sql.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.*;

import static io.github.wy.sql.util.TRY;
import static io.github.wy.sql.util.stream;
import static java.util.stream.Collectors.joining;

public class Insert<T> {
    private final TableInfo<T> table;
    private T value;
    private boolean ignoreNulls = true;
    
    public Insert(TableInfo<T> table) {
        this.table = table;
    }
    
    public Insert<T> VALUE(T value) {
        this.value = value;
        return this;
    }
    
    public Insert<T> notIgnoreNulls() {
        this.ignoreNulls = false;
        return this;
    }
    
    public int jdbc(JdbcTemplate jdbcTemplate) {
        SqlAndParam sqlAndParam = buildSql();
        return new NamedParameterJdbcTemplate(jdbcTemplate).update(sqlAndParam.sql.toString(), sqlAndParam.paramMap);
    }
    
    public Optional<Number> jdbcReturnKey(JdbcTemplate jdbcTemplate) {
        SqlAndParam sqlAndParam = buildSql();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        new NamedParameterJdbcTemplate(jdbcTemplate).update(sqlAndParam.sql.toString(), new MapSqlParameterSource(sqlAndParam.paramMap), keyHolder);
        return Optional.ofNullable(keyHolder.getKey());
    }
    
    private SqlAndParam buildSql() {
        Map<String, String> mapped = table.mapped();
        if (value == null) throw new RuntimeException("value is null");
        
        Map<String, Object> paramMap = new HashMap<>();
        List<String> fields = new ArrayList<>();
        List<String> columns = new ArrayList<>();
        stream(value.getClass().getDeclaredFields()).forEach(field->{
            field.setAccessible(true);
            Object v = TRY(()->field.get(this.value));
            if (v != null || !ignoreNulls) {
                //需要insert的字段
                fields.add(field.getName());
                columns.add(mapped.get(field.getName()));
                //插入的值
                paramMap.put(field.getName(), v);
            }
        });
        //组合SQL
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(table.tableName()).append(" ");
        String columnSql = String.join(",", columns);
        sql.append("(").append(columnSql).append(") VALUES ");
        String values = stream(fields).map(it->":" + it).collect(joining(","));
        sql.append("(").append(values).append(")");
        System.out.println(sql);
        SqlAndParam result = new SqlAndParam(paramMap, sql);
        return result;
    }
    
    private static class SqlAndParam {
        public final Map<String, Object> paramMap;
        public final StringBuilder sql;
        
        public SqlAndParam(final Map<String, Object> paramMap, final StringBuilder sql) {
            this.paramMap = paramMap;
            this.sql = sql;
        }
    }
}
