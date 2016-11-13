package de.eternalwings.nmsmapper.gen;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import de.eternalwings.nmsmapper.model.MethodMapping;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.List;

public class MethodMappingGenerator implements MappingGenerator {
    private final MethodMapping mappingInfo;

    public MethodMappingGenerator(MethodMapping mappingInfo) {
        this.mappingInfo = mappingInfo;
    }

    @Override
    public MethodSpec generateInterfaceMapping(String targetEntityFieldName) {
        String methodName = this.mappingInfo.methodMapping.method.getSimpleName().toString();
        TypeName targetReturnType = TypeName.get(this.mappingInfo.targetMethod.getReturnType());
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(targetReturnType);

        builder = this.buildParameters(builder);
        builder = this.buildStatement(builder, targetReturnType, targetEntityFieldName, this.mappingInfo.targetMethod);

        return builder.build();
    }

    @Override
    public MethodSpec generateClassMapping() {
        String targetMethodName = this.mappingInfo.targetMethod.getSimpleName().toString();
        TypeName targetReturnType = TypeName.get(this.mappingInfo.targetMethod.getReturnType());
        MethodSpec.Builder builder = MethodSpec.methodBuilder(targetMethodName)
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(targetReturnType);

        builder = this.buildParameters(builder);
        builder = this.buildStatement(builder, targetReturnType, null, this.mappingInfo.methodMapping.method);

        return builder.build();
    }

    private MethodSpec.Builder buildStatement(MethodSpec.Builder builder, TypeName returnTypeName, String targetEntityFieldName, ExecutableElement targetMethod) {
        String methodCall = "";
        int parameterCount = this.getParameters().size();
        for(int i = 1; i <= parameterCount; i++) {
            methodCall += (parameterCount > 1 ? ", " : "") + "p" + i;
        }

        String targetMethodName = targetMethod.getSimpleName().toString();
        if(returnTypeName.equals(TypeName.VOID)) {
            if(targetEntityFieldName != null) {
                return builder.addStatement("this.$N.$N(" + methodCall + ")", targetEntityFieldName, targetMethodName);
            } else {
                return builder.addStatement("this.$N(" + methodCall + ")", targetMethodName);
            }
        } else {
            if(targetEntityFieldName != null) {
                return builder.addStatement("return this.$N.$N(" + methodCall + ")", targetEntityFieldName, targetMethodName);
            } else {
                return builder.addStatement("this.$N(" + methodCall + ")", targetMethod);
            }
        }
    }

    private List<? extends VariableElement> getParameters() {
        return this.mappingInfo.methodMapping.method.getParameters();
    }

    private MethodSpec.Builder buildParameters(MethodSpec.Builder builder) {
        int parameterCount = 0;
        for (VariableElement variableElement : this.getParameters()) {
            parameterCount += 1;
            builder = builder.addParameter(TypeName.get(variableElement.asType()), "p" + parameterCount, Modifier.FINAL);
        }

        return builder;
    }
}
