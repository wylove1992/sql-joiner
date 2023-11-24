package io.github.wy.sql.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.List;

import static io.github.wy.sql.util.stream;
import static java.util.stream.Collectors.joining;

/**
 * @author rycat
 * @since 2023/11/24
 */
public class SQL {
    private final List<String> PARTS = new ArrayList<>(0);
    
    public static SQL SELECT(String... parts) {
        SQL sql = new SQL();
        sql.PARTS.add("SELECT " + (parts.length == 0 ? "*" : stream(parts).collect(joining(", "))));
        return sql;
    }
    
    public String end() {
        return stream(PARTS).collect(joining(" "));
    }
    
    public JdbcHelper jdbc(JdbcTemplate jdbcTemplate) {
        String sql = end();
        return new JdbcHelper(new NamedParameterJdbcTemplate(jdbcTemplate), sql);
    }
    
    
    public SQL FROM(String... parts) {
        PARTS.add("FROM " + stream(parts).collect(joining(" ")));
        return this;
    }
    
    public SQL FROM_SUB(String... parts) {
        PARTS.add("FROM (" + stream(parts).collect(joining(" ")) + ")");
        return this;
    }
    
    public SQL AS(TableInfo<?> table) {
        PARTS.add("AS " + table.tableAlias());
        return this;
    }
    
    public SQL WHERE(String... parts) {
        PARTS.add("WHERE " + stream(parts).collect(joining("")));
        return this;
    }
    
    public SQL ON(String... parts) {
        PARTS.add("ON " + stream(parts).collect(joining("")));
        return this;
    }
    
    public SQL ORDER_BY(String... parts) {
        PARTS.add("ORDER BY " + stream(parts).collect(joining(", ")));
        return this;
    }
    
    public SQL LIMIT(int limit) {
        PARTS.add("LIMIT " + limit);
        return this;
    }
    
    public SQL OFFSET(int offset) {
        PARTS.add("OFFSET " + offset);
        return this;
    }
    
    // 其他操作
    public static <T> Insert<T> INSERT(TableInfo<T> table) {
        return new Insert<>(table);
    }
    
    public static <T> Update<T> UPDATE(TableInfo<T> table) {
        return new Update<>(table);
    }
    
    public static <T> Delete<T> DELETE(TableInfo<T> table) {
        return new Delete<>(table);
    }
    
    public static final String SELECT = " SELECT ";
    public static final String FROM = " FROM ";
    public static final String WHERE = " WHERE ";
    public static final String AND = " AND ";
    public static final String OR = " OR ";
    public static final String JOIN = " JOIN ";
    public static final String LEFT_JOIN = " LEFT JOIN ";
    public static final String INNER_JOIN = " INNER JOIN ";
    public static final String ON = " ON ";
    public static final String UNION_ALL = " UNION ALL ";
    public static final String DISTINCT = " DISTINCT ";
    public static final String GROUP_BY = " GROUP BY ";
    public static final String HAVING = " HAVING ";
}
