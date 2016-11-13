package de.eternalwings.nmsmapper.gen;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import de.eternalwings.nmsmapper.model.FieldMapping;

import javax.lang.model.element.Modifier;

public class FieldMappingGenerator implements MappingGenerator {
    private final FieldMapping mappingInfo;

    public FieldMappingGenerator(FieldMapping mappingInfo) {
        this.mappingInfo = mappingInfo;
    }

    @Override
    public MethodSpec generateInterfaceMapping(String targetEntityField) {
        String methodName = this.mappingInfo.methodMapping.method.getSimpleName().toString();
        TypeName targetTypeName = TypeName.get(this.mappingInfo.targetField.asType());
        String targetFieldName = this.mappingInfo.targetField.getSimpleName().toString();
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(targetTypeName)
                .addStatement("return this.$N.$N", targetEntityField, targetFieldName)
                .build();
    }

    @Override
    public MethodSpec generateClassMapping() {
        return null;
    }
}
