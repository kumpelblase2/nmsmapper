package de.eternalwings.nmsmapper.gen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import de.eternalwings.nmsmapper.model.ElementTypePair;
import de.eternalwings.nmsmapper.model.MethodMapping;
import de.eternalwings.nmsmapper.processor.AnnotationHelper;
import de.eternalwings.nmsmapper.processor.ElementHelper;
import de.eternalwings.nmsmapper.processor.MappingEnvironment;
import de.eternalwings.nmsmapper.wrapping.NMSWrapper;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.List;

public class MethodMappingGenerator implements MappingGenerator {
    private final MethodMapping mappingInfo;
    private final ElementTypePair wrappedMirror;
    private final MappingEnvironment environment;

    public MethodMappingGenerator(MethodMapping mappingInfo, ElementTypePair wrappedMirror, MappingEnvironment environment) {
        this.mappingInfo = mappingInfo;
        this.wrappedMirror = wrappedMirror;
        this.environment = environment;
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
        List<? extends VariableElement> parameters = this.getParameters();
        int current = 0;
        for (VariableElement parameter : parameters) {
            current += 1;
            String paramName = "p" + current;
            methodCall += current > 1 ? ", " : "";
            if(this.shouldWrap(parameter)) {
                methodCall += this.wrapCall(paramName, parameter.asType().toString());
            } else {
                methodCall += paramName;
            }
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
            if (this.shouldWrap(variableElement)) {
                String targetType = this.environment.getMappedTypeOf(ElementHelper.getFullyQualifiedClassName(variableElement));
                builder = builder.addParameter(ClassName.bestGuess(targetType), "p" + parameterCount, Modifier.FINAL);
            } else {
                builder = builder.addParameter(ElementHelper.getTypeName(variableElement), "p" + parameterCount, Modifier.FINAL);
            }
        }

        return builder;
    }

    private boolean shouldWrap(VariableElement element) {
        AnnotationMirror wrappedAnnotation = AnnotationHelper.getAnnotation(this.environment.getTypeUtils(), element, this.wrappedMirror.type);
        return wrappedAnnotation != null;
    }

    private String wrapCall(String parameterName, String resultType) {
        return NMSWrapper.class.getCanonicalName() + ".wrapOf(" + parameterName + ", " + resultType + ".class)";
    }
}
