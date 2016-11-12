package de.eternalwings.nmsmapper.model;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

public class NMSMethodMapping {
    public final ExecutableElement method;
    public final AnnotationMirror mappingAnnotation;

    public NMSMethodMapping(ExecutableElement method, AnnotationMirror mappingAnnotation) {
        this.method = method;
        this.mappingAnnotation = mappingAnnotation;
    }
}
