package de.eternalwings.nmsmapper.model;

import javax.lang.model.element.AnnotationMirror;

public class NMSMappingInfo {
    public final AnnotationMirror nmsAnnotation;
    public final ElementTypePair sourceAbstractType;

    public NMSMappingInfo(AnnotationMirror nmsAnnotation, ElementTypePair sourceAbstractType) {
        this.nmsAnnotation = nmsAnnotation;
        this.sourceAbstractType = sourceAbstractType;
    }
}
