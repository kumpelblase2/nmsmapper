package de.eternalwings.nmsmapper.processor;

import de.eternalwings.nmsmapper.model.ElementTypePair;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class TypeHelper {
    public static TypeMirror getSuperclass(Types typeUtils, TypeMirror type) {
        for (TypeMirror typeMirror : typeUtils.directSupertypes(type)) {
            DeclaredType declaredType = ((DeclaredType) typeMirror);
            if(declaredType.asElement().getKind() == ElementKind.CLASS) {
                return typeMirror;
            }
        }

        return null;
    }

    public static ElementTypePair getType(Types typeUtil, Elements elementUtil, String className) {
        TypeElement element = elementUtil.getTypeElement(className);
        if(element == null) {
            return null;
        }

        DeclaredType type = typeUtil.getDeclaredType(element);
        return new ElementTypePair(element, type);
    }
}
