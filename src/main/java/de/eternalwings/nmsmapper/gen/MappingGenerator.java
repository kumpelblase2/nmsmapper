package de.eternalwings.nmsmapper.gen;

import com.squareup.javapoet.MethodSpec;

import java.util.Collection;

public interface MappingGenerator {
    Collection<MethodSpec> generateInterfaceMapping(String targetEntityField);
    Collection<MethodSpec> generateClassMapping();
}
