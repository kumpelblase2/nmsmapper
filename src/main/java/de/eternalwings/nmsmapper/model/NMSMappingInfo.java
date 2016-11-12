package de.eternalwings.nmsmapper.model;

import javax.lang.model.element.AnnotationMirror;

public class NMSMappingInfo {
    public final AnnotationMirror nmsAnnotation;
    public final ElementTypePair sourceInterface;

    public NMSMappingInfo(AnnotationMirror nmsAnnotation, ElementTypePair sourceInterface) {
        this.nmsAnnotation = nmsAnnotation;
        this.sourceInterface = sourceInterface;
    }
}
