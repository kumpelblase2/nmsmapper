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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseProcessor extends AbstractProcessor {

    protected ElementTypePair getType(String className) {
        return TypeHelper.getType(this.getTypeUtils(), this.getElementUtils(), className);
    }

    protected AnnotationMirror getAnnotation(Element element, DeclaredType annotationType) {
        return AnnotationHelper.getAnnotation(this.getTypeUtils(), element, annotationType);
    }

    protected ExecutableElement getMethod(Element element, String methodName) {
        return ElementHelper.getMethod(element, methodName);
    }

    protected VariableElement getField(Element element, String fieldName) {
        return ElementHelper.getField(element, fieldName);
    }

    protected AnnotationValue getAnnotationProperty(AnnotationMirror mirror, String methodName) {
        return AnnotationHelper.getAnnotationProperty(mirror, methodName);
    }

    protected TypeMirror getPropertyType(Element element) {
        return ElementHelper.getPropertyType(element);
    }

    protected String getPackage(TypeElement element) {
        return ElementHelper.getPackage(element);
    }

    protected boolean isAnnotationValueNull(AnnotationValue annotationValue) {
        return AnnotationHelper.isAnnotationValueNull(annotationValue);
    }

    protected TypeMirror getSuperclass(TypeMirror type) {
        return TypeHelper.getSuperclass(this.getTypeUtils(), type);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Collections.singletonList(NMS.class.getCanonicalName()));
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
