package io.github.wy.sql.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Objects;

import static io.github.wy.sql.util.stream;
import static java.util.stream.Collectors.joining;

public class Delete<T> {
    private final TableInfo<T> table;
    private String whereCondition;
    
    public Delete(final TableInfo<T> table) {
        this.table = table;
    }
    
    public Delete<T> WHERE(String... parts) {
        whereCondition = "WHERE " + stream(parts).collect(joining(""));
        return this;
    }
    
    public int jdbc(JdbcTemplate jdbcTemplate) {
        Objects.requireNonNull(whereCondition, "语句错误");
        StringBuilder sql = new StringBuilder("DELETE FROM ").append(table.tableName()).append(" ").append(whereCondition);
        System.out.println(sql);
        return new NamedParameterJdbcTemplate(jdbcTemplate).update(sql.toString(), table.paramMap());
    }
    
}
