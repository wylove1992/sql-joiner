package io.github.wy.sql;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author rycat
 * @since 2023/11/22
 */
public class EntityClass {
    final List<Field> fields = new ArrayList<>();
    final String canonicalName, simpleName, packageName;
    final Table tableAnnotation;
    
    public EntityClass(String packageName, String simpleName, String canonicalName, Table tableAnnotation) {
        this.canonicalName = canonicalName;
        this.simpleName = simpleName;
        this.packageName = packageName;
        this.tableAnnotation = tableAnnotation;
    }
    
    public List<Field> getDeclaredFields() {
        return fields;
    }
    
    public String getCanonicalName() {
        return canonicalName;
    }
    
    public String getSimpleName() {
        return simpleName;
    }
    
    public Table getTableAnnotation() {
        return tableAnnotation;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    static class Field {
        private final Column columnAnnotation;
        private final String name,typeName;
        private final Set<Modifier> modifiers;
        
        
        public Field(Column columnAnnotation, String name, String typeName, Set<Modifier> modifiers) {
            this.columnAnnotation = columnAnnotation;
            this.name = name;
            this.modifiers = modifiers;
            this.typeName = typeName;
        }
        
        public Column getColumnAnnotation() {
            return columnAnnotation;
        }
        
        public String getName() {
            return name;
        }
        
        public Set<Modifier> getModifiers() {
            return modifiers;
        }
    }
}
