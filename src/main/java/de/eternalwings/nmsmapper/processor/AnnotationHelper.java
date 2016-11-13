package de.eternalwings.nmsmapper.processor;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Types;
import java.util.Map;

public class AnnotationHelper {
    public static boolean isAnnotationValueNull(AnnotationValue value) {
        return value == null || value.getValue() == null;
    }

    public static AnnotationValue getAnnotationProperty(AnnotationMirror mirror, String methodName) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
        return elementValues.get(ElementHelper.getMethod(mirror.getAnnotationType().asElement(), methodName));
    }

    public static AnnotationMirror getAnnotation(Types typeUtils, Element element, DeclaredType annotationType) {
        for(AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if(typeUtils.isSameType(mirror.getAnnotationType(), annotationType)) {
                return mirror;
            }
        }

        return null;
    }
}
