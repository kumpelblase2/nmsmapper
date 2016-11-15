package de.eternalwings.nmsmapper.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

class WrappingException extends RuntimeException {
    private final String message;
    private final Element element;
    private final AnnotationMirror annotationMirror;
    private final AnnotationValue annotationValue;

    public WrappingException(String message) {
        this(message, null);
    }

    public WrappingException(String message, Element element) {
        this(message, element, null);
    }

    public WrappingException(String message, Element element, AnnotationMirror annotationMirror) {
        this(message, element, annotationMirror, null);
    }

    public WrappingException(String message, Element element, AnnotationMirror annotationMirror, AnnotationValue annotationValue) {
        super(message);
        this.message = message;
        this.element = element;
        this.annotationMirror = annotationMirror;
        this.annotationValue = annotationValue;
    }

    public void printError(Messager messager) {
        messager.printMessage(Diagnostic.Kind.ERROR, this.message, this.element, this.annotationMirror, this.annotationValue);
    }
}
