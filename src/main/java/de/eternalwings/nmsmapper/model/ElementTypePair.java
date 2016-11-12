package de.eternalwings.nmsmapper.model;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

public class ElementTypePair {
    public final TypeElement element;
    public final DeclaredType type;

    public ElementTypePair(TypeElement element, DeclaredType type) {
        this.element = element;
        this.type = type;
    }
}
