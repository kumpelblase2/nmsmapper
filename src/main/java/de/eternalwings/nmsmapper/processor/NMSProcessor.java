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
    private ElementTypePair wrapParameterAnnotation;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.nmsAnnotation = this.getType("de.eternalwings.nmsmapper.NMS");
        this.mappedMethodAnnotation = this.getType("de.eternalwings.nmsmapper.NMSMethod");
        this.wrapParameterAnnotation = this.getType("de.eternalwings.nmsmapper.NMSWrap");
    }

    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Collection<NMSMappingInfo> mappings = this.getFoundMappingTypes(roundEnvironment);
        Map<String, String> wrapperMapping = this.getWrapperMapping(mappings);
        MappingEnvironment environment = new MappingEnvironment(wrapperMapping, mappings, this);
        for(NMSMappingInfo info : mappings) {
            try {
                this.handleMapping(info, environment);
            } catch(MappingException ignored) {
            }
        }

        return false;
    }

    private Map<String, String> getWrapperMapping(Collection<NMSMappingInfo> mappings) {
        Map<String, String> mapping = new HashMap<>();
        for (NMSMappingInfo mappingInfo : mappings) {
            String targetClass = this.findTargetType(mappingInfo, AnnotationHelper.getAnnotationProperty(mappingInfo.nmsAnnotation, "value")).element.getQualifiedName().toString();
            mapping.put(mappingInfo.sourceAbstractType.element.getQualifiedName().toString(), targetClass);
        }

        return mapping;
    }

    private void handleMapping(NMSMappingInfo info, MappingEnvironment env) {
        AnnotationValue targetValue = this.getAnnotationProperty(info.nmsAnnotation, "value");
        ElementTypePair targetType = this.findTargetType(info, targetValue);

        if (targetType == null) {
            this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot find target class", info.sourceAbstractType.element, info.nmsAnnotation, targetValue);
            return;
        }

        List<NMSMethodMapping> methodMappings = this.getMethodMappings(info.sourceAbstractType);
        List<MappingGenerator> generators = new ArrayList<>();
        for(NMSMethodMapping mapping : methodMappings) {
            generators.add(this.getGeneratorForMethod(mapping, targetType, env));
        }

        String newTypeName = buildNMSWrapperName(info.sourceAbstractType.element.getSimpleName().toString());
        TypeSpec resultType;
        if(info.sourceAbstractType.element.getKind() == ElementKind.INTERFACE) {
            resultType = this.buildInterfaceImplType(newTypeName, info.sourceAbstractType, targetType, generators);
        } else {
            resultType = this.buildClassImplType(newTypeName, info.sourceAbstractType, generators);
        }

        this.writeType(resultType, this.getPackage(info.sourceAbstractType.element));
    }

    private MappingGenerator getGeneratorForMethod(NMSMethodMapping mapping, ElementTypePair targetType, MappingEnvironment env) {
        AnnotationValue methodNameValue = this.getAnnotationProperty(mapping.mappingAnnotation, "value");
        if(this.isAnnotationValueNull(methodNameValue)) {
            this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Missing method name in @NMSMethod", mapping.method, mapping.mappingAnnotation, methodNameValue);
            throw new MappingException();
        }

        AnnotationValue isFieldValue = this.getAnnotationProperty(mapping.mappingAnnotation, "isField");
        Boolean isField = this.isAnnotationValueNull(isFieldValue) ? false : (Boolean) isFieldValue.getValue();

        String methodName = ((String) methodNameValue.getValue());
        if(isField) {
            VariableElement targetField;
            try {
                targetField = this.getField(targetType.element, methodName);
            } catch (IllegalArgumentException e) {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Mapped field " + methodName + " does not exist.", mapping.method, mapping.mappingAnnotation, methodNameValue);
                throw new MappingException();
            }

            if(mapping.method.getParameters().size() > 1) {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Field mappings may not have any extra method parameters.", mapping.method.getParameters().get(1));
                throw new MappingException();
            }

            boolean isGetter = mapping.method.getParameters().size() == 0;
            TypeMirror returnType;
            TypeMirror targetFieldType;
            if(isGetter) {
                returnType = this.getPropertyType(mapping.method);
                targetFieldType = this.getPropertyType(targetField);
            } else {
                returnType = this.getPropertyType(targetField);
                targetFieldType = this.getPropertyType(mapping.method.getParameters().get(0));
            }

            if (!this.getTypeUtils().isSameType(returnType, targetFieldType)) {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Interface method and target field are of different types.", mapping.method);
                throw new MappingException();
            }

            return new FieldMappingGenerator(new FieldMapping(targetField, mapping), isGetter);
        } else {
            ExecutableElement targetMethod;
            try {
                targetMethod = this.findSuitableMethod(methodName, mapping.method, targetType.element, env);
            } catch (IllegalArgumentException e) {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Mapped method " + methodName + " does not exist.", mapping.method, mapping.mappingAnnotation, methodNameValue);
                throw new MappingException();
            }

            return new MethodMappingGenerator(new MethodMapping(targetMethod, mapping), this.wrapParameterAnnotation, env);
        }
    }

    private ElementTypePair findTargetType(NMSMappingInfo info, AnnotationValue targetValue) {
        if(this.isAnnotationValueNull(targetValue)) {
            if(info.sourceAbstractType.element.getKind() == ElementKind.INTERFACE) {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Missing target class", info.sourceAbstractType.element, info.nmsAnnotation, targetValue);
                throw new MappingException();
            }

            String targetClass = info.sourceAbstractType.element.getSuperclass().toString();
            return this.getType(targetClass);
        } else {
            if(info.sourceAbstractType.element.getKind() != ElementKind.INTERFACE) {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot specify target with class", info.sourceAbstractType.element, info.nmsAnnotation, targetValue);
                throw new MappingException();
            }

            String targetName = ((String) targetValue.getValue());
            return this.getType(targetName);
        }
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

    private TypeSpec buildInterfaceImplType(String typeName, ElementTypePair interfaceType, ElementTypePair targetType, List<MappingGenerator> generators) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(typeName).addModifiers(Modifier.PUBLIC);

        builder = builder.addSuperinterface(TypeName.get(interfaceType.type));
        String wrappedEntityFieldName = WRAPPED_ENTITY_FIELD_NAME;
        TypeName targetTypeName = TypeName.get(targetType.type);
        FieldSpec wrappedEntityField = FieldSpec.builder(targetTypeName, wrappedEntityFieldName, Modifier.FINAL).build();
        MethodSpec constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
                .addParameter(targetTypeName, wrappedEntityFieldName)
                .addStatement("this.$N = $N", wrappedEntityFieldName, wrappedEntityFieldName)
                .build();

        builder = builder.addField(wrappedEntityField).addMethod(constructor);

        for (MappingGenerator generator : generators) {
            builder = builder.addMethod(generator.generateInterfaceMapping(wrappedEntityFieldName));
        }

        return builder.build();
    }

    private TypeSpec buildClassImplType(String name, ElementTypePair baseClass, List<MappingGenerator> generators) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);

        builder = builder.superclass(TypeName.get(baseClass.type));
        List<MethodSpec> constructors = this.buildConstructorsForClass(baseClass);
        for (MethodSpec constructor : constructors) {
            builder = builder.addMethod(constructor);
        }

        for (MappingGenerator generator : generators) {
            builder = builder.addMethod(generator.generateClassMapping());
        }

        return builder.build();
    }

    private List<MethodSpec> buildConstructorsForClass(ElementTypePair targetType) {
        List<MethodSpec> specs = new ArrayList<>();

        for (ExecutableElement constructor : ElementFilter.constructorsIn(targetType.element.getEnclosedElements())) {
            MethodSpec.Builder builder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
            int parameterCount = 0;
            for (VariableElement parameter : constructor.getParameters()) {
                parameterCount += 1;
                builder = builder.addParameter(TypeName.get(parameter.asType()), "p" + parameterCount, Modifier.FINAL);
            }

            StringBuilder superCallBuilder = new StringBuilder("super(");
            for(int i = 1; i <= parameterCount; i++) {
                if(i > 1) {
                    superCallBuilder.append(", ");
                }
                superCallBuilder.append("p").append(i);
            }

            superCallBuilder.append(")");
            builder.addStatement(superCallBuilder.toString());
            specs.add(builder.build());
        }

        return specs;
    }

    private ExecutableElement findSuitableMethod(String name, ExecutableElement matchingElement, TypeElement target, MappingEnvironment env) {
        List<? extends VariableElement> interfaceParameters = matchingElement.getParameters();

        methodLoop:
        for(ExecutableElement method : ElementFilter.methodsIn(target.getEnclosedElements())) {
            if(method.getSimpleName().toString().equals(name)) {
                List<? extends VariableElement> targetParameters = method.getParameters();
                if(targetParameters.size() != interfaceParameters.size()) {
                    continue;
                }

                for(int i = 0; i < interfaceParameters.size(); i++) {
                    VariableElement interfaceParameter = interfaceParameters.get(i);
                    VariableElement targetParameter = targetParameters.get(i);

                    TypeMirror interfaceType = this.getPropertyType(interfaceParameter);
                    TypeMirror targetType = this.getPropertyType(targetParameter);
                    if(!this.getTypeUtils().isSameType(interfaceType, targetType)) {
                        AnnotationMirror wrappingAnnotation = this.getAnnotation(interfaceParameter, this.wrapParameterAnnotation.type);
                        if(wrappingAnnotation != null) {
                            if(!env.hasMapperFor(interfaceType.toString())) {
                                continue methodLoop;
                            }
                        } else {
                            continue methodLoop;
                        }
                    }
                }

                TypeMirror interfaceReturn = this.getPropertyType(matchingElement);
                TypeMirror targetReturn = this.getPropertyType(method);
                if(!this.getTypeUtils().isSameType(interfaceReturn, targetReturn)) {
                    continue;
                }

                return method;
            }
        }

        throw new IllegalArgumentException("No suitable method available.");
    }

    private List<NMSMethodMapping> getMethodMappings(ElementTypePair sourceInterface) {
        boolean isInterface = sourceInterface.element.getKind() == ElementKind.INTERFACE;
        List<NMSMethodMapping> mappings = new ArrayList<>();
        List<? extends Element> containedElements = sourceInterface.element.getEnclosedElements();
        for(ExecutableElement method : ElementFilter.methodsIn(containedElements)) {
            AnnotationMirror annotation = this.getAnnotation(method, this.mappedMethodAnnotation.type);
            if(annotation == null) {
                if(isInterface) {
                    this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Method in interface is missing @NMSMethod annotation.", method);
                    return Collections.emptyList();
                } else {
                    continue;
                }
            }

            NMSMethodMapping mapping = new NMSMethodMapping(method, annotation);
            mappings.add(mapping);
        }

        return mappings;
    }

    private Collection<NMSMappingInfo> getFoundMappingTypes(RoundEnvironment roundEnvironment) {
        Collection<NMSMappingInfo> mappings = new ArrayList<>();
        Set<? extends Element> nmsInterfaces = roundEnvironment.getElementsAnnotatedWith(this.nmsAnnotation.element);
        for(TypeElement e : ElementFilter.typesIn(nmsInterfaces)) {
            AnnotationMirror nmsAnnotationElement = this.getAnnotation(e, this.nmsAnnotation.type);
            if(e.getKind() == ElementKind.CLASS || e.getKind() == ElementKind.INTERFACE) {
                ElementTypePair pair = new ElementTypePair(e, this.getTypeUtils().getDeclaredType(e));
                NMSMappingInfo info = new NMSMappingInfo(nmsAnnotationElement, pair);
                mappings.add(info);
            } else {
                this.getMessager().printMessage(Diagnostic.Kind.ERROR, "Only interfaces or classes may be annotated with @NMS", e, nmsAnnotationElement);
            }
        }

        return mappings;
    }

    public static String buildNMSWrapperName(String currentTypeName) {
        return currentTypeName + NMS_TYPE_SUFFIX;
    }
}
