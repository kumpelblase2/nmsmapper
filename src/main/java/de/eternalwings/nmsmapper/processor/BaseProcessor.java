package de.eternalwings.nmsmapper.processor;

import de.eternalwings.nmsmapper.NMS;
import de.eternalwings.nmsmapper.model.ElementTypePair;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class BaseProcessor extends AbstractProcessor {

    protected ElementTypePair getType(String className) {
        TypeElement element = this.getElementUtils().getTypeElement(className);
        if(element == null) {
            return null;
        }

        DeclaredType type = this.getTypeUtils().getDeclaredType(element);
        return new ElementTypePair(element, type);
    }

    protected AnnotationMirror getAnnotation(Element element, DeclaredType annotationType) {
        for(AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if(this.getTypeUtils().isSameType(mirror.getAnnotationType(), annotationType)) {
                return mirror;
            }
        }

        return null;
    }

    protected ExecutableElement getMethod(Element element, String methodName) {
        for(ExecutableElement executableElement : ElementFilter.methodsIn(element.getEnclosedElements())) {
            if(executableElement.getSimpleName().toString().equals(methodName)) {
                return executableElement;
            }
        }

        throw new IllegalArgumentException("Unknown method " + methodName + " on " + element.getSimpleName());
    }

    protected VariableElement getField(Element element, String fieldName) {
        for(VariableElement variableElement : ElementFilter.fieldsIn(element.getEnclosedElements())) {
            if(variableElement.getSimpleName().toString().equals(fieldName)) {
                return variableElement;
            }
        }

        throw new IllegalArgumentException("Unknown field " + fieldName + " on " + element.getSimpleName());
    }

    protected AnnotationValue getAnnotationProperty(AnnotationMirror mirror, String methodName) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
        return elementValues.get(this.getMethod((TypeElement) mirror.getAnnotationType().asElement(), methodName));
    }

    protected TypeMirror getPropertyType(Element element) {
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

    protected String getPackage(TypeElement element) {
        String qualifiedName = element.getQualifiedName().toString();
        return qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
    }

    protected boolean isAnnotationValueNull(AnnotationValue annotationValue) {
        return annotationValue == null || annotationValue.getValue() == null;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(Collections.singletonList(NMS.class.getCanonicalName()));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public Messager getMessager() {
        return processingEnv.getMessager();
    }

    public Filer getFiler() {
        return processingEnv.getFiler();
    }

    public Elements getElementUtils() {
        return processingEnv.getElementUtils();
    }

    public Types getTypeUtils() {
        return processingEnv.getTypeUtils();
    }
}
