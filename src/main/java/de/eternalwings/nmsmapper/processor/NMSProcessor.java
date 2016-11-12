package de.eternalwings.nmsmapper.processor;

import com.squareup.javapoet.*;
import de.eternalwings.nmsmapper.gen.FieldMappingGenerator;
import de.eternalwings.nmsmapper.gen.MappingGenerator;
import de.eternalwings.nmsmapper.gen.MethodMappingGenerator;
import de.eternalwings.nmsmapper.model.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

public class NMSProcessor extends BaseProcessor {
    private static final String NMS_TYPE_SUFFIX = "$NMS";
    private static final String WRAPPED_ENTITY_FIELD_NAME = "nmsEntity";

    private ElementTypePair nmsAnnotation;
    private ElementTypePair mappedMethodAnnotation;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.nmsAnnotation = this.getType("de.eternalwings.nmsmapper.NMS");
        this.mappedMethodAnnotation = this.getType("de.eternalwings.nmsmapper.NMSMethod");
    }

    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Collection<NMSMappingInfo> mappings = this.getFoundMappingInterfaces(roundEnvironment);
        for(NMSMappingInfo info : mappings) {
            AnnotationValue targetValue = this.getAnnotationProperty(info.nmsAnnotation, "value");
            if(targetValue == null || targetValue.getValue() == null) { //TODO Property file based mapping
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Missing target class", info.sourceInterface.element, info.nmsAnnotation, targetValue);
            } else {
                String targetName = ((String) targetValue.getValue());
                ElementTypePair targetType = this.getType(targetName);
                if(targetType == null) {
                    this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot find target class " + targetName, info.sourceInterface.element, info.nmsAnnotation, targetValue);
                    continue;
                }

                if(targetType.element.getModifiers().contains(Modifier.ABSTRACT)) {
                    this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Target type may not be abstract.", info.sourceInterface.element, info.nmsAnnotation, targetValue);
                    continue;
                }

                List<NMSMethodMapping> methodMappings = this.getMethodMappings(info.sourceInterface);
                List<MappingGenerator> generators = new ArrayList<>();
                for(NMSMethodMapping mapping : methodMappings) {
                    AnnotationValue methodNameValue = this.getAnnotationProperty(mapping.mappingAnnotation, "value");
                    if(methodNameValue == null || methodNameValue.getValue() == null) {
                        this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Missing method name in @NMSMethod", info.sourceInterface.element, mapping.mappingAnnotation, methodNameValue);
                        return false;
                    }

                    AnnotationValue isFieldValue = this.getAnnotationProperty(mapping.mappingAnnotation, "isField");
                    Boolean isField = false;
                    if(isFieldValue != null && isFieldValue.getValue() != null) {
                        isField = (Boolean) isFieldValue.getValue();
                    }

                    String methodName = ((String) methodNameValue.getValue());
                    if(isField) {
                        VariableElement targetField;
                        try {
                            targetField = this.getField(targetType.element, methodName);
                        } catch (IllegalArgumentException e) {
                            this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Mapped field " + methodName + " does not exist.", mapping.method, mapping.mappingAnnotation, methodNameValue);
                            return false;
                        }

                        if(mapping.method.getParameters().size() > 0) {
                            this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Field mappings may not have any method parameters.", mapping.method.getParameters().get(0));
                            return false;
                        }

                        TypeMirror methodReturnType = this.getPropertyType(mapping.method);
                        TypeMirror targetFieldType = this.getPropertyType(targetField);

                        if(!this.getTypeUtils().isSameType(methodReturnType, targetFieldType)) {
                            this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Interface method and target field are of different types.", mapping.method);
                            return false;
                        }

                        generators.add(new FieldMappingGenerator(new FieldMapping(targetField, mapping)));
                    } else {
                        ExecutableElement targetMethod;
                        try {
                            targetMethod = this.getMethod(targetType.element, methodName);
                        } catch (IllegalArgumentException e) {
                            this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Mapped method " + methodName + " does not exist.", mapping.method, mapping.mappingAnnotation, methodNameValue);
                            return false;
                        }

                        if (!this.ensureSameSignature(mapping.method, targetMethod)) {
                            return false;
                        }

                        generators.add(new MethodMappingGenerator(new MethodMapping(targetMethod, mapping)));
                    }
                }

                String newTypeName = info.sourceInterface.element.getSimpleName() + NMS_TYPE_SUFFIX;
                TypeSpec resultType = this.buildType(newTypeName, info.sourceInterface, targetType, generators);
                this.writeType(resultType, this.getPackage(info.sourceInterface.element));
            }
        }

        return true;
    }

    private void writeType(TypeSpec resultType, String packageName) {
        JavaFile output = JavaFile.builder(packageName, resultType).build();
        try {
            output.writeTo(this.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
            this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to write source.");
        }
    }

    private TypeSpec buildType(String typeName, ElementTypePair interfaceType, ElementTypePair targetType, List<MappingGenerator> generators) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(typeName);

        builder = builder.addSuperinterface(TypeName.get(interfaceType.type));
        String wrappedEntityFieldName = WRAPPED_ENTITY_FIELD_NAME;
        TypeName targetTypeName = TypeName.get(targetType.type);
        FieldSpec wrappedEntityField = FieldSpec.builder(targetTypeName, wrappedEntityFieldName, Modifier.FINAL).build();
        MethodSpec constructor = MethodSpec.constructorBuilder().addParameter(targetTypeName, wrappedEntityFieldName)
                .addStatement("this.$N = $N", wrappedEntityFieldName, wrappedEntityFieldName)
                .build();

        builder = builder.addField(wrappedEntityField).addMethod(constructor);

        for (MappingGenerator generator : generators) {
            builder = builder.addMethod(generator.generate(wrappedEntityFieldName));
        }

        return builder.build();
    }

    private boolean ensureSameSignature(ExecutableElement method, ExecutableElement targetMethod) {

        TypeMirror interfaceReturn = this.getPropertyType(method);
        TypeMirror targetReturn = this.getPropertyType(targetMethod);
        if(!this.getTypeUtils().isSameType(interfaceReturn, targetReturn)) {
            this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Interface return and target return type does not match.", method);
            return false;
        }

        List<? extends VariableElement> interfaceMethodParameters = method.getParameters();
        List<? extends VariableElement> targetMethodParameters = targetMethod.getParameters();

        if(interfaceMethodParameters.size() != targetMethodParameters.size()) {
            this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Interface method and target method do not have the same amount of parameters.", method);
            return false;
        }

        for(int i = 0; i < interfaceMethodParameters.size(); i++) {
            VariableElement interfaceParameter = interfaceMethodParameters.get(i);
            VariableElement targetParameter = targetMethodParameters.get(i);

            TypeMirror interfaceType = this.getPropertyType(interfaceParameter);
            TypeMirror targetType = this.getPropertyType(targetParameter);
            if(!this.getTypeUtils().isSameType(interfaceType, targetType)) {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Parameter type does not match target method parmeter type.", interfaceParameter);
                return false;
            }
        }

        return true;
    }

    private List<NMSMethodMapping> getMethodMappings(ElementTypePair sourceInterface) {
        List<NMSMethodMapping> mappings = new ArrayList<>();
        List<? extends Element> containedElements = sourceInterface.element.getEnclosedElements();
        for(ExecutableElement method : ElementFilter.methodsIn(containedElements)) {
            AnnotationMirror annotation = this.getAnnotation(method, this.mappedMethodAnnotation.type);
            if(annotation == null) {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Method in interface is missing @NMSMethod annotation.", method);
                return Collections.emptyList();
            }

            NMSMethodMapping mapping = new NMSMethodMapping(method, annotation);
            mappings.add(mapping);
        }

        return mappings;
    }

    private Collection<NMSMappingInfo> getFoundMappingInterfaces(RoundEnvironment roundEnvironment) {
        Collection<NMSMappingInfo> mappings = new ArrayList<>();
        Set<? extends Element> nmsInterfaces = roundEnvironment.getElementsAnnotatedWith(this.nmsAnnotation.element);
        for(TypeElement e : ElementFilter.typesIn(nmsInterfaces)) {
            AnnotationMirror nmsAnnotationElement = this.getAnnotation(e, this.nmsAnnotation.type);
            if(e.getKind() == ElementKind.INTERFACE) {
                ElementTypePair pair = new ElementTypePair(e, this.getTypeUtils().getDeclaredType(e));
                NMSMappingInfo info = new NMSMappingInfo(nmsAnnotationElement, pair);
                mappings.add(info);
            } else {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Only interfaces may be annotated with @NMS", e, nmsAnnotationElement);
            }
        }

        return mappings;
    }
}
