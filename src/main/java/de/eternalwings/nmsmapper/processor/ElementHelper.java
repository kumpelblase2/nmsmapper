package de.eternalwings.nmsmapper.processor;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

public class ElementHelper {
    public static String getPackage(TypeElement element) {
        String qualifiedName = element.getQualifiedName().toString();
        if (!qualifiedName.contains(".")) {
            return "";
        } else {
            return qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
        }
    }

    public static TypeMirror getPropertyType(Element element) {
        switch (element.getKind()) {
            case FIELD:
                return ((VariableElement) element).asType();
            case METHOD:
                return ((ExecutableElement) element).getReturnType();
            case PARAMETER:
                return ((VariableElement) element).asType();
            default:
                return null;
        }
    }

    public static VariableElement getField(Element element, String fieldName) {
        for(VariableElement variableElement : ElementFilter.fieldsIn(element.getEnclosedElements())) {
            if(variableElement.getSimpleName().toString().equals(fieldName)) {
                return variableElement;
            }
        }

        throw new IllegalArgumentException("Unknown field " + fieldName + " on " + element.getSimpleName());
    }

    public static ExecutableElement getMethod(Element element, String methodName) {
        for(ExecutableElement executableElement : ElementFilter.methodsIn(element.getEnclosedElements())) {
            if(executableElement.getSimpleName().toString().equals(methodName)) {
                return executableElement;
            }
        }

        throw new IllegalArgumentException("Unknown method " + methodName + " on " + element.getSimpleName());
    }

    public static String getFullyQualifiedClassName(Element element) {
        return element.asType().toString();
    }

    public static TypeName getTypeName(Element element) {
        return TypeName.get(element.asType());
    }

    public static TypeElement getSuperclass(TypeElement element) {
        TypeMirror superclass = element.getSuperclass();
        if(superclass instanceof DeclaredType) {
            return (TypeElement) ((DeclaredType) superclass).asElement();
        } else {
            return null;
        }
    }
}
