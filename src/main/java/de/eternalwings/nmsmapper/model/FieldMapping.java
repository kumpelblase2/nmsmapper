package de.eternalwings.nmsmapper.model;

import javax.lang.model.element.VariableElement;

public class FieldMapping {
    public final VariableElement targetField;
    public final NMSMethodMapping methodMapping;

    public FieldMapping(VariableElement targetField, NMSMethodMapping methodMapping) {
        this.targetField = targetField;
        this.methodMapping = methodMapping;
    }
}
