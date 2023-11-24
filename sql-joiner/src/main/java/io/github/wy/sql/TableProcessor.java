package io.github.wy.sql;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.joining;

/**
 * @author rycat
 * @since 2023/11/22
 */
@SupportedAnnotationTypes({"io.github.wy.sql.Table", "io.github.wy.sql.Column"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class TableProcessor extends AbstractProcessor {
    private Messager messager;
    private Filer filer;
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        log(TableProcessor.class.getName() + " has created!");
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        log(TableProcessor.class.getName() + " starts to processing!");
        
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Table.class);
        log("本轮编译拿到的支持注解：" + annotations.stream().map(Objects::toString).collect(joining(",")));
        log("本轮编译拿到的具有@Table注解的语法元素有：" + elements.stream().map(Objects::toString).collect(joining(",")));
        
        for (Element element : elements) {
            Table table = element.getAnnotation(Table.class);
            //只处理java类
            if (ElementKind.CLASS != element.getKind()) continue;
            TypeElement typeElement = (TypeElement) element;
            // 获取被注解类的包名
            String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
            // 获取被注解类的类名
            String className = typeElement.getSimpleName().toString();
            EntityClass entityClass = new EntityClass(packageName, className, packageName + "." + className, table);
            
            // 获取被注解类的字段信息
            for (Element enclosedElement : typeElement.getEnclosedElements()) {
                //只处理java类中的字段，方法什么的不管
                if (enclosedElement.getKind() != ElementKind.FIELD) continue;
                VariableElement fieldElement = (VariableElement) enclosedElement;
                // 获取字段名和类型信息
                String fieldName = fieldElement.getSimpleName().toString();
                String fieldType = fieldElement.asType().toString();
                Column column = fieldElement.getAnnotation(Column.class);
                EntityClass.Field field = new EntityClass.Field(column, fieldName, fieldType, fieldElement.getModifiers());
                entityClass.fields.add(field);
            }
            
            try {
                TableClassGenerator.write(filer, entityClass);
            } catch (Exception e) {
                log("生成java源文件异常");
                e.printStackTrace();
            }
        }
        log("本轮结束\n====================");
        return true;
    }
    
    //private method area
    private void log(String message) {
        System.out.println("java:" + message);
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }
}
