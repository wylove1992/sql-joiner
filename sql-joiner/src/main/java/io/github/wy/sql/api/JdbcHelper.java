package io.github.wy.sql.api;

import io.github.wy.sql.util;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.wy.sql.util.stream;
import static java.util.stream.Collectors.joining;

/**
 * @author rycat
 * @since 2023/11/24
 */
public class JdbcHelper {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final String sql;
    private Map<String, Object> paramMap = new HashMap<>(0);
    
    public JdbcHelper(NamedParameterJdbcTemplate jdbcTemplate, String sql) {
        this.jdbcTemplate = jdbcTemplate;
        this.sql = sql;
    }
    
    public JdbcHelper withTables(TableInfo<?>... tableInfos) {
        return withTables(util.stream(tableInfos).collect(Collectors.toList()));
    }
    
    public JdbcHelper withTables(Collection<TableInfo<?>> tables) {
        return withTables(util.mergeParam(tables));
    }
    
    public JdbcHelper withTables(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
        return this;
    }
    
    public List<Map<String, Object>> map() {
        return jdbcTemplate.queryForList(sql, paramMap);
    }
    
    public <T> List<T> list(Class<T> resultClass) {
        return jdbcTemplate.query(sql, paramMap, new BeanPropertyRowMapper<>(resultClass));
    }
    
    public List<Map<String, Object>> page(int limit, int offset) {
        return jdbcTemplate.queryForList("SELECT __.* FROM (" + sql + ") AS __ LIMIT " + limit + " OFFSET " + offset, paramMap);
    }
    
    public <T> List<T> page(int limit, int offset, Class<T> resultClass) {
        return jdbcTemplate.query("SELECT __.* FROM (" + sql + ") AS __ LIMIT " + limit + " OFFSET " + offset, paramMap, new BeanPropertyRowMapper<>(resultClass));
    }
    
    public Long count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(1) FROM (" + sql + ") AS __", paramMap, Long.class);
    }
    
    public void test() {
        System.out.println("***********************************");
        System.out.println("执行语句：");
        System.out.println(replaceVariables(sql, paramMap));
        System.out.println("***********************************");
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql, paramMap);
        System.out.println("执行结果：");
        stream(maps).forEach(System.out::println);
        System.out.println("***********************************");
    }
    
    private static String replaceVariables(String template, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            CharSequence placeholder = ":" + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Number) {
                Number number = (Number) value;
                template = template.replace(placeholder, value.toString());
            } else if (value instanceof Collection) {
                Collection<?> objects = (Collection<?>) value;
                String collect = objects.stream().map(it->{
                    if (it instanceof Number) {
                        return ((Number) it).toString();
                    }
                    return "'" + it.toString() + "'";
                }).collect(joining(","));
                template = template.replace(placeholder, collect);
            } else {
                template = template.replace(placeholder, "'" + value + "'");
            }
        }
        return template;
    }
    
    
}
