package de.eternalwings.nmsmapper.gen;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import de.eternalwings.nmsmapper.model.FieldMapping;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.Collections;

public class FieldMappingGenerator implements MappingGenerator {
    private final FieldMapping mappingInfo;
    private boolean isGetter;

    public FieldMappingGenerator(FieldMapping mappingInfo) {
        this(mappingInfo, true);
    }

    public FieldMappingGenerator(FieldMapping mappingInfo, boolean isGetter) {
        this.mappingInfo = mappingInfo;
        this.isGetter = isGetter;
    }

    @Override
    public Collection<MethodSpec> generateInterfaceMapping(String targetEntityField) {
        String targetFieldName = this.mappingInfo.targetField.getSimpleName().toString();
        MethodSpec.Builder builder = this.createSignature();
        if(this.isGetter) {
            builder = builder.addStatement("return this.$N.$N", targetEntityField, targetFieldName);
        } else {
            builder = builder.addStatement("this.$N.$N = $N", targetEntityField, targetFieldName, "value");
        }

        return Collections.singletonList(builder.build());
    }

    private MethodSpec.Builder prepareBuilder(String methodName) {
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                .addAnnotation(Override.class);
    }

    @Override
    public Collection<MethodSpec> generateClassMapping() {
        String targetFieldName = this.mappingInfo.targetField.getSimpleName().toString();
        MethodSpec.Builder builder = this.createSignature();
        if(this.isGetter) {
            builder = builder.addStatement("return this.$N", targetFieldName);
        } else {
            builder = builder.addStatement("this.$N = $N", targetFieldName, "value");
        }

        return Collections.singletonList(builder.build());
    }

    private MethodSpec.Builder createSignature() {
        String methodName = this.mappingInfo.methodMapping.method.getSimpleName().toString();
        TypeName targetTypeName = TypeName.get(this.mappingInfo.targetField.asType());
        if(this.isGetter) {
            return prepareBuilder(methodName)
                    .returns(targetTypeName);
        } else {
            return prepareBuilder(methodName)
                    .returns(TypeName.VOID)
                    .addParameter(targetTypeName, "value");
        }
    }
}
