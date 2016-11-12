package de.eternalwings.nmsmapper.model;

import javax.lang.model.element.ExecutableElement;

public class MethodMapping {
    public final ExecutableElement targetMethod;
    public final NMSMethodMapping methodMapping;

    public MethodMapping(ExecutableElement targetMethod, NMSMethodMapping methodMapping) {
        this.targetMethod = targetMethod;
        this.methodMapping = methodMapping;
    }
}
