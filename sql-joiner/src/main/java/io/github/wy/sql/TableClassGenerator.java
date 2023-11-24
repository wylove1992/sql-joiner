package io.github.wy.sql;

import io.github.wy.sql.api.Op;
import io.github.wy.sql.api.Order;
import io.github.wy.sql.api.TableInfo;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.github.wy.sql.util.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * @author rycat
 * @since 2023/11/22
 */
public class TableClassGenerator {
    public static void write(Filer filer, EntityClass entityClass) throws IOException {
        String classFullyName = entityClass.getPackageName() + "." + entityClass.getSimpleName() + "Table";
        //生成类
        JavaFileObject sourceFile = filer.createSourceFile(classFullyName);
        Writer writer = sourceFile.openWriter();
        String noPackageHeaderSource = gen(entityClass);
        writer.append("package " + entityClass.getPackageName() + ";\n" + noPackageHeaderSource);
        writer.close();
    }
    
    private static final Function<EntityClass.Field, String> DEFAULT_NAME_MAPPER = field->ofNullable(field.getColumnAnnotation()).map(Column::name).orElse(toSymbolCase(field.getName(), '_'));
    
    private static String gen(EntityClass entityClass) {
        final String tableName = ofNullable(entityClass.getTableAnnotation()).map(Table::name).orElse(toSymbolCase(entityClass.getSimpleName(), '_'));
        final String alias = ofNullable(entityClass.getTableAnnotation()).map(Table::alias).filter(s->!s.isEmpty()).orElse(tableName);
        final String database = ofNullable(entityClass.getTableAnnotation()).map(Table::database).orElse("MYSQL");
        final String THE_CLASS_NAME = entityClass.getSimpleName() + "Table";
        
        final Function<EntityClass.Field, String> mapper = DEFAULT_NAME_MAPPER;
        final String DOUBLE_QUOTE = "\"";
        final String Q = database.equals("MYSQL") ? "`" : "\\" + DOUBLE_QUOTE;
        
        final Map<String, String> fieldToColumn = new HashMap<>();
        stream(entityClass.getDeclaredFields()).filter(field->!field.getModifiers().contains(Modifier.STATIC)).forEach(f->fieldToColumn.put(f.getName(), mapper.apply(f)));
        
        StringBuilder s = new StringBuilder();
        s.append("\n");
        s.append(writeImport(
                entityClass.getCanonicalName(),
                Op.class.getCanonicalName(),
                Order.class.getCanonicalName(),
                HashMap.class.getName(),
                Map.class.getName(),
                TableInfo.class.getName()
        ));
        s.append("\n");
        s.append(writeComment("对应实体类：" + entityClass.getCanonicalName() + ", 时间：" + new Date().toString()));
        
        s.append("public final class ").append(THE_CLASS_NAME).append(interfaceList(entityClass)).append("{\n");
        s.append("    ").append(writeField("public static final Class<" + entityClass.getSimpleName() + ">", "modelClass", entityClass.getSimpleName() + ".class"));
        s.append("    ").append(writeField("private static final HashMap<String, Object>", "paramMap", "new HashMap<>()"));
        s.append("    ").append(writeField("private String", "_tableName", quote(tableName)));
        s.append("    ").append(writeField("private String", "_tableAlias", quote(alias)));
        
        s.append("    ").append("public ").append(THE_CLASS_NAME).append("(){}\n");
        s.append("    ").append("public ").append(THE_CLASS_NAME).append("(String tableName){\n");
        s.append("    ").append("    ").append("_tableName = tableName").append(";\n");
        s.append("    ").append("}\n");
        s.append("    ").append("public ").append(THE_CLASS_NAME).append("(String tableName, String alias){\n");
        s.append("    ").append("    ").append("_tableName = tableName").append(";\n");
        s.append("    ").append("    ").append("_tableAlias = alias").append(";\n");
        s.append("    ").append("}\n");
        s.append("    ").append("public ").append(THE_CLASS_NAME).append(" as(String alias){\n");
        s.append("    ").append("    ").append("_tableAlias = alias").append(";\n");
        s.append("    ").append("    ").append("return this").append(";\n");
        s.append("    ").append("}\n");
        s.append("    ").append("public ").append(THE_CLASS_NAME).append(" changeAlias(String alias){\n");
        s.append("    ").append("    ").append("return new ").append(THE_CLASS_NAME).append("(_tableName, alias)").append(";\n");
        s.append("    ").append("}\n");
        
        s.append("    ").append(writeField("public final static Map<String, String>", "MAPPED", "new HashMap<>()"));
        s.append("    ").append("static {\n");
        fieldToColumn.forEach((field, column)->s.append("    ").append("    ").append("MAPPED.put(" + quote(field) + ", " + quote(column) + ")").append(";\n"));
        s.append("    ").append("}\n");
        s.append("\n");
        s.append(tableInfoInterfaceImpl(entityClass));
        s.append("\n");
        s.append("//以上公共部分\n");
        
        s.append("//所有数据库字段名字\n");
        fieldToColumn.forEach((field, column)->s.append("    ").append(writeField("public final String", field, quote(column))));
        s.append("//所有实体类中的字段名字\n");
        fieldToColumn.forEach((field, column)->s.append("    ").append(writeField("public final String", field + "_", quote(field))));
        s.append("\n");
        s.append("//所有字段\n");
        List<String> fields = fieldToColumn.keySet().stream().sorted().collect(toList());
        s.append("    ").append(writeField("public static final String[]", "ALL", "new String[]{" + stream(fields).map(it->quote(fieldToColumn.get(it))).collect(joining(",")) + "}"));
        s.append("    ").append(writeField("public static final String[]", "ALL_", "new String[]{" + stream(fields).map(TableClassGenerator::quote).collect(joining(",")) + "}"));
        
        s.append("\n");
        fieldToColumn.forEach((field, column)->{
            s.append("    ").append("public String ").append(field).append("(Op op, Object value)").append("{\n");
            s.append("    ").append("    String nameKey = " + util.class.getName() + ".getNameKey();\n");
            s.append("    ").append("    paramMap.put(nameKey, value);\n");
            String p = "_tableAlias+" + quote(".") + "+" + field;
            s.append("    ").append("    return  op.get(" + p + ", " + quote(":") + "+nameKey" + ");\n");
            s.append("    ").append("}\n");
        });
        s.append("\n");
        fieldToColumn.forEach((field, column)->{
            s.append("    ").append("public String ").append(field + "_").append("(Op op, Object value)").append("{\n");
            s.append("    ").append("    String nameKey = " + util.class.getName() + ".getNameKey();\n");
            s.append("    ").append("    paramMap.put(nameKey, value);\n");
            String p = "_tableAlias+" + quote(".") + "+" + field + "_";
            s.append("    ").append("    return  op.get(" + p + ", " + quote(":") + "+nameKey" + ");\n");
            s.append("    ").append("}\n");
        });
        s.append("\n");
        
        fieldToColumn.forEach((field, column)->{
            String p = "_tableAlias+" + quote(".") + "+" + field + "+" + quote(" ");
            String r = p + " + order.name();";
            s.append("    ").append("public String ").append(field).append("(Order order){ ")
             .append("return ").append(r)
             .append(" }\n");
        });
        
        fieldToColumn.forEach((field, column)->{
            String p = "_tableAlias+" + quote(".") + "+" + field + "_" + "+" + quote(" ");
            String r = p + " + order.name();";
            s.append("    ").append("public String ").append(field + "_").append("(Order order){ ")
             .append("return ").append(r)
             .append(" }\n");
        });
        
        s.append("//工具方法\n");
        s.append("    ").append("public String q(String str) {\n");
        if (database.equals("MYSQL")) {
            s.append("    ").append("    ").append("return \"`\"+str+\"`\";\n");
        } else {
            s.append("    ").append("    ").append("return \"\\\"\"+str+\"\\\"\";\n");
        }
        s.append("    ").append("}\n");
        
        
        //class 结束括号
        s.append("}\n");
        return s.toString();
    }
    
    
    private static String interfaceList(EntityClass entityClass) {
        return " implements " + TableInfo.class.getCanonicalName() + "<" + entityClass.getSimpleName() + ">";
    }
    
    private static String tableInfoInterfaceImpl(EntityClass entityClass) {
        return "    public Class<" + entityClass.getSimpleName() + "> modelClass() {return modelClass;}\n"
               + "    public Map<String, Object> paramMap() {return paramMap;}\n"
               + "    public String tableAlias() {return _tableAlias;}\n"
               + "    public Map<String, String> mapped() {return MAPPED;}\n"
               + "    public String database() {return " + quote(entityClass.tableAnnotation.database()) + ";}\n"
               + "    public String tableName() {return _tableName;}\n";
    }
    
    private static String writeImport(String... name) {
        return stream(name).map(it->"import " + it + ";").collect(joining("\n")) + "\n";
    }
    
    private static String writeComment(String name) {
        return "/**\n" + name + "\n*/\n";
    }
    
    private static String writeField(final String modifierAndType, final String name, final String value) {
        return modifierAndType + " " + name + ofNullable(value).map(it->" = " + value).orElse("") + ";\n";
    }
    
    private static String writeMethod(final String modifierAndType, final String nameAndParam, String... lines) {
        return modifierAndType + " " + nameAndParam + "{\n" + stream(lines).map(it->"    " + it + ";\n").collect(joining()) + "}\n";
    }
    
    
    private static String quote(String str) {
        return "\"" + str + "\"";
    }
    
    private static String toSymbolCase(CharSequence str, char symbol) {
        if (str == null) {
            return null;
        }
        
        final int length = str.length();
        final StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0; i < length; i++) {
            c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                final Character preChar = (i > 0) ? str.charAt(i - 1) : null;
                final Character nextChar = (i < str.length() - 1) ? str.charAt(i + 1) : null;
                
                if (null != preChar) {
                    if (symbol == preChar) {
                        // 前一个为分隔符
                        if (null == nextChar || Character.isLowerCase(nextChar)) {
                            //普通首字母大写，如_Abb -> _abb
                            c = Character.toLowerCase(c);
                        }
                        //后一个为大写，按照专有名词对待，如_AB -> _AB
                    } else if (Character.isLowerCase(preChar)) {
                        // 前一个为小写
                        sb.append(symbol);
                        
                        if (null == nextChar || Character.isLowerCase(nextChar) || Character.isDigit(nextChar)) {
                            //普通首字母大写，如aBcc -> a_bcc
                            c = Character.toLowerCase(c);
                        }
                        // 后一个为大写，按照专有名词对待，如aBC -> a_BC
                    } else {
                        //前一个为大写
                        if (null != nextChar && Character.isLowerCase(nextChar)) {
                            // 普通首字母大写，如ABcc -> A_bcc
                            sb.append(symbol);
                            c = Character.toLowerCase(c);
                        }
                        // 后一个为大写，按照专有名词对待，如ABC -> ABC
                    }
                } else {
                    // 首字母，需要根据后一个判断是否转为小写
                    if (null == nextChar || Character.isLowerCase(nextChar)) {
                        // 普通首字母大写，如Abc -> abc
                        c = Character.toLowerCase(c);
                    }
                    // 后一个为大写，按照专有名词对待，如ABC -> ABC
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }
    
}
