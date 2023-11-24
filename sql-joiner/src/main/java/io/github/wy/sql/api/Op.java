package io.github.wy.sql.api;

import java.util.function.BiFunction;

public enum Op {
        //LambdaWhereItemSingleValue
        EQ((l, r)->l + "=" + r), LT((l, r)->l + "<" + r), GT((l, r)->l + ">" + r), NE((l, r)->l + "!=" + r), LE((l, r)->l + "<=" + r), GE((l, r)->l + ">=" + r), LIKE((l, r)->l + " LIKE " + r),
        
        IN((l, r)->l + " IN (" + r + ")"),
        //for LambdaWhereItemNoValue
        IS_NULL((l, __)->l + " IS NULL"),
        NOT_NULL((l, __)->l + " IS NOT NULL");
        
        private BiFunction<String, String, String> sql;
        
        Op(BiFunction<String, String, String> sql) {
            this.sql = sql;
        }
        
        public String get(String l, String r) {
            return sql.apply(l, r);
        }
    }
