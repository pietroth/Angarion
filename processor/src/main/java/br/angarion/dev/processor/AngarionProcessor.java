package br.angarion.dev.processor;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.auto.service.AutoService;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.lang.model.element.Modifier;
import java.io.IOException;

import br.angarion.dev.api.communication.Payload;
import br.angarion.dev.api.communication.Type;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_26)
public class AngarionProcessor extends AbstractProcessor {
    private static final String TYPE_ANNOTATION_PATH = "br.angarion.dev.api.communication.Type";
    private static final ByteOrder ORDER = ByteOrder.BIG_ENDIAN;

    private static final ClassName MEMORY_LAYOUT = ClassName.get("java.lang.foreign", "MemoryLayout");
    private static final ClassName MEMORY_SEGMENT = ClassName.get("java.lang.foreign", "MemorySegment");
    private static final ClassName STRUCT_LAYOUT = ClassName.get("java.lang.foreign", "StructLayout");
    private static final ClassName VALUE_LAYOUT = ClassName.get("java.lang.foreign", "ValueLayout");
    private static final ClassName VAR_HANDLE = ClassName.get("java.lang.invoke", "VarHandle");
    private static final ClassName PATH_ELEMENT = ClassName.get("java.lang.foreign", "MemoryLayout", "PathElement");

    private static final String DEFAULT_LAYOUT_NAME = "LAYOUT";
    private static final String DEFAULT_SEGMENT_NAME = "segment";

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(TYPE_ANNOTATION_PATH);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Elements elementUtils = processingEnv.getElementUtils();
        Types typeUtils = processingEnv.getTypeUtils();
        Messager messager = processingEnv.getMessager();

        Set<? extends Element> types = roundEnv.getElementsAnnotatedWith(Type.class);

        for (Element element : types) {
            AnnotationMirror mirror = getAnnotationMirror(element, TYPE_ANNOTATION_PATH);
            var valuesInRound = new LinkedHashMap<String, TypeMirror>();
            TypeElement payloadElement = null;

            if (mirror != null) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> values =
                    elementUtils.getElementValuesWithDefaults(mirror);

                for (var entry : values.entrySet()) {
                    if (entry.getKey().getSimpleName().contentEquals("value")) {
                        TypeMirror payloadType = (TypeMirror) entry.getValue().getValue();
                        payloadElement = (TypeElement) typeUtils.asElement(payloadType);

                        for (RecordComponentElement component : payloadElement.getRecordComponents()) {
                            String name = component.getSimpleName().toString();
                            TypeMirror type = component.asType();

                            valuesInRound.put(name, type);
                        }

                        System.out.println(payloadType);
                    }
                }

                if (payloadElement == null) {
                    continue;
                }

                StructLayout layout = buildStruct(valuesInRound);

                String defaultLayoutName = "LAYOUT";

                FieldSpec layoutField = FieldSpec.builder(STRUCT_LAYOUT, defaultLayoutName)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer(buildStructInitializer(valuesInRound))
                        .build();

                String generatedClassName = payloadElement.getSimpleName() + "Layout";
                TypeSpec.Builder generatedClass = TypeSpec.classBuilder(generatedClassName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addField(layoutField);

                for (var entry : valuesInRound.entrySet()) {
                    buildComponentHandlers(generatedClass, DEFAULT_SEGMENT_NAME, DEFAULT_LAYOUT_NAME, entry.getKey(), TypeName.get(entry.getValue()));
                }

                String packageName = elementUtils.getPackageOf(payloadElement)
                        .getQualifiedName()
                        .toString();

                JavaFile javaFile = JavaFile.builder(packageName, generatedClass.build())
                        .skipJavaLangImports(true)
                        .build();

                try {
                    javaFile.writeTo(processingEnv.getFiler());
                } catch (IOException e) {
                    messager.printMessage(Diagnostic.Kind.ERROR, e.toString());
                }

            }
        }

        return true;
    }

    private AnnotationMirror getAnnotationMirror(Element element, String annotationName) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            TypeElement annotationElement = (TypeElement) mirror.getAnnotationType().asElement();
            if (annotationElement.getQualifiedName().contentEquals(annotationName)) {
                return mirror;
            }
        }
        return null;
    }

    private MemoryLayout mapType(TypeMirror type) {
        return switch (type.getKind()) {
            case INT -> ValueLayout.JAVA_INT.withOrder(ORDER);
            case BYTE -> ValueLayout.JAVA_BYTE.withOrder(ORDER);
            case SHORT -> ValueLayout.JAVA_SHORT.withOrder(ORDER);
            case LONG -> ValueLayout.JAVA_LONG.withOrder(ORDER);
            case FLOAT -> ValueLayout.JAVA_FLOAT.withOrder(ORDER);
            case DOUBLE -> ValueLayout.JAVA_DOUBLE.withOrder(ORDER);
            case BOOLEAN -> ValueLayout.JAVA_BOOLEAN.withOrder(ORDER);
            default -> {
                String name = type.toString();
                if (name.equals("java.lang.String")) {
                    yield ValueLayout.ADDRESS.withOrder(ORDER);
                }
                throw new IllegalArgumentException("Type not supported: " + name);
            }
        };
    }

    private List<MemoryLayout> buildOrderedLayouts(Map<String, TypeMirror> fields) {
        List<MemoryLayout> ordered = new ArrayList<>(fields.size());

        for (var entry : fields.entrySet()) {
            MemoryLayout namedLayout = mapType(entry.getValue()).withName(entry.getKey());
            ordered.add(namedLayout);
        }

        return ordered;
    }

    private StructLayout buildStruct(Map<String, TypeMirror> fields) {
        List<MemoryLayout> ordered = buildOrderedLayouts(fields);
        StructLayout base = MemoryLayout.structLayout(
            ordered.toArray(MemoryLayout[]::new)
        );

        long maxAlignment = base.byteAlignment();
        long currentSize = base.byteSize();
        long tailPadding = (maxAlignment - (currentSize % maxAlignment)) % maxAlignment;

        if (tailPadding > 0) {
            ordered.add(MemoryLayout.paddingLayout(tailPadding));
            base = MemoryLayout.structLayout(ordered.toArray(MemoryLayout[]::new));
        }

        return base;
    }

    private CodeBlock buildStructInitializer(Map<String, TypeMirror> fields) {
        List<MemoryLayout> ordered = buildOrderedLayouts(fields);
        StructLayout base = MemoryLayout.structLayout(
            ordered.toArray(MemoryLayout[]::new)
        );

        long maxAlignment = base.byteAlignment();
        long currentSize = base.byteSize();
        long tailPadding = (maxAlignment - (currentSize % maxAlignment)) % maxAlignment;

        CodeBlock.Builder builder = CodeBlock.builder()
            .add("$T.structLayout(", MEMORY_LAYOUT);

        boolean first = true;
        for (var entry : fields.entrySet()) {
            if (!first) {
                builder.add(", ");
            }
            builder.add(layoutExpression(entry.getKey(), entry.getValue()));
            first = false;
        }

        if (tailPadding > 0) {
            if (!first) {
                builder.add(", ");
            }
            builder.add("$T.paddingLayout($L)", MEMORY_LAYOUT, tailPadding);
        }

        builder.add(")");
        return builder.build();
    }

    private CodeBlock layoutExpression(String name, TypeMirror type) {
        return switch (type.getKind()) {
            case INT -> CodeBlock.of("$T.JAVA_INT.withOrder($T.BIG_ENDIAN).withName($S)", VALUE_LAYOUT, ByteOrder.class, name);
            case BYTE -> CodeBlock.of("$T.JAVA_BYTE.withOrder($T.BIG_ENDIAN).withName($S)", VALUE_LAYOUT, ByteOrder.class, name);
            case SHORT -> CodeBlock.of("$T.JAVA_SHORT.withOrder($T.BIG_ENDIAN).withName($S)", VALUE_LAYOUT, ByteOrder.class, name);
            case LONG -> CodeBlock.of("$T.JAVA_LONG.withOrder($T.BIG_ENDIAN).withName($S)", VALUE_LAYOUT, ByteOrder.class, name);
            case FLOAT -> CodeBlock.of("$T.JAVA_FLOAT.withOrder($T.BIG_ENDIAN).withName($S)", VALUE_LAYOUT, ByteOrder.class, name);
            case DOUBLE -> CodeBlock.of("$T.JAVA_DOUBLE.withOrder($T.BIG_ENDIAN).withName($S)", VALUE_LAYOUT, ByteOrder.class, name);
            case BOOLEAN -> CodeBlock.of("$T.JAVA_BOOLEAN.withOrder($T.BIG_ENDIAN).withName($S)", VALUE_LAYOUT, ByteOrder.class, name);
            default -> {
                String typeName = type.toString();
                if (typeName.equals("java.lang.String")) {
                    yield CodeBlock.of("$T.ADDRESS.withOrder($T.BIG_ENDIAN).withName($S)", VALUE_LAYOUT, ByteOrder.class, name);
                }
                throw new IllegalArgumentException("Type not supported: " + typeName);
            }
        };
    }

    // Create component VarHandle, getter and setter fuctions
    private void buildComponentHandlers(TypeSpec.Builder generatedClass, String segmentName, String layoutName, String componentName, TypeName componentType) {
        FieldSpec varHandle = FieldSpec.builder(VAR_HANDLE, componentName)
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("$L.varHandle($T.groupElement($S))", layoutName, PATH_ELEMENT, componentName)
            .build();

        MethodSpec getterMethod = MethodSpec.methodBuilder(componentName)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .returns(componentType)
            .addParameter(MEMORY_SEGMENT, segmentName)
            .addStatement("return ($T) $L.get($L, 0L)", componentType, componentName, segmentName)
            .build();

        MethodSpec setterMethod = MethodSpec.methodBuilder("set_" + componentName)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .returns(void.class)
            .addParameter(componentType, componentName + "_value")
            .addParameter(MEMORY_SEGMENT, segmentName)
            .addStatement("$L.set($L, 0L, $L)", componentName, segmentName, componentName + "_value")
            .build();

        generatedClass.addField(varHandle);
        generatedClass.addMethod(getterMethod);
        generatedClass.addMethod(setterMethod);
    }
}
