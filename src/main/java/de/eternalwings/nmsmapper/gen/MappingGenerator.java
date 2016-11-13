package de.eternalwings.nmsmapper.gen;

import com.squareup.javapoet.MethodSpec;

public interface MappingGenerator {
    MethodSpec generateInterfaceMapping(String targetEntityField);
    MethodSpec generateClassMapping();
}
