package br.angarion.dev.processor;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import br.angarion.dev.engine.communication.DataLayout;

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
    private static final String FAMILY_CONFIG_ANNOTATION_PATH = "br.angarion.dev.api.communication.FamilyConfiguration";

    private static final ByteOrder ORDER = ByteOrder.BIG_ENDIAN;

    private static final ClassName MEMORY_LAYOUT = ClassName.get("java.lang.foreign", "MemoryLayout");
    private static final ClassName MEMORY_SEGMENT = ClassName.get("java.lang.foreign", "MemorySegment");
    private static final ClassName STRUCT_LAYOUT = ClassName.get("java.lang.foreign", "StructLayout");
    private static final ClassName VALUE_LAYOUT = ClassName.get("java.lang.foreign", "ValueLayout");
    private static final ClassName VAR_HANDLE = ClassName.get("java.lang.invoke", "VarHandle");
    private static final ClassName PATH_ELEMENT = ClassName.get("java.lang.foreign", "MemoryLayout", "PathElement");
    private static final ClassName STANDARD_CHARSETS_UTF_8 = ClassName.get("java.nio.charset", "StandardCharsets", "UTF_8");

    private static final String DEFAULT_LAYOUT_NAME = "LAYOUT";
    private static final String DEFAULT_SEGMENT_NAME = "segment";

    private static final int STRING_MAX_SIZE = 64;
    private static final String STRING_DEFAULT_OFFSET_NAME = "Offset"; // componentName + offset

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(TYPE_ANNOTATION_PATH, FAMILY_CONFIG_ANNOTATION_PATH);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Elements elementUtils = processingEnv.getElementUtils();
        Types typeUtils = processingEnv.getTypeUtils();
        Messager messager = processingEnv.getMessager();

        Set<? extends Element> types = roundEnv.getElementsAnnotatedWith(Type.class);

        for (Element element : types) {
            createDataLayout(element, elementUtils, typeUtils, messager);
        }

        return true;
    }

    private Optional<ClassName> createDataLayout(
        Element element,
        Elements elementUtils,
        Types typeUtils,
        Messager messager)
    {
        AnnotationMirror mirror = getAnnotationMirror(element, TYPE_ANNOTATION_PATH);
        var valuesInRound = new LinkedHashMap<String, TypeMirror>();
        TypeElement payloadElement = null;
        boolean isNotification = false;
        String familyName = null;

        if (mirror == null) {
            return Optional.empty();
        }

        Map<? extends ExecutableElement, ? extends AnnotationValue> values =
            elementUtils.getElementValuesWithDefaults(mirror);

        for (var entry : values.entrySet()) {
            String keyName = entry.getKey().getSimpleName().toString();

            switch (keyName) {
                case "payload" -> {
                    TypeMirror payloadType = (TypeMirror) entry.getValue().getValue();
                    payloadElement = (TypeElement) typeUtils.asElement(payloadType);

                    for (RecordComponentElement component : payloadElement.getRecordComponents()) {
                        String name = component.getSimpleName().toString();
                        TypeMirror type = component.asType();

                        valuesInRound.put(name, type);
                    }

                    System.out.println(payloadType);
                }

                case "isNotification" -> {
                    Boolean isNotificationValue = (Boolean) entry.getValue().getValue();
                    isNotification = isNotificationValue;
                }

                case "family" -> {
                    TypeMirror familyType = (TypeMirror) entry.getValue().getValue();
                    Element familyElement = typeUtils.asElement(familyType);

                    AnnotationMirror familyAnnotation = getAnnotationMirror(familyElement, FAMILY_CONFIG_ANNOTATION_PATH);

                    if (familyAnnotation == null) {
                        messager.printMessage(
                            Diagnostic.Kind.ERROR, "Your Family implementation must be annotated with @FamilyConfiguration.");
                        return Optional.empty(); // Skip this type
                    }

                    var familyAnnotationValues = elementUtils.getElementValuesWithDefaults(familyAnnotation);

                    for (var familyAnnotationValue : familyAnnotationValues.entrySet()) {
                        String key = familyAnnotationValue.getKey().getSimpleName().toString();
                        Object value = familyAnnotationValue.getValue().getValue();

                        if (key.equals("value")) {
                            familyName = value.toString();
                        }
                    }
                }
            }
        }

        if (payloadElement == null) {
            return Optional.empty();
        }

        String defaultLayoutName = "LAYOUT";

        List<MemoryLayout> orderedLayouts = new ArrayList<>(valuesInRound.size());
        for (var entry : valuesInRound.entrySet()) {
            TypeMirror type = entry.getValue();
            MemoryLayout fieldLayout = switch (type.getKind()) {
                case INT -> ValueLayout.JAVA_INT.withOrder(ORDER);
                case BYTE -> ValueLayout.JAVA_BYTE.withOrder(ORDER);
                case SHORT -> ValueLayout.JAVA_SHORT.withOrder(ORDER);
                case LONG -> ValueLayout.JAVA_LONG.withOrder(ORDER);
                case FLOAT -> ValueLayout.JAVA_FLOAT.withOrder(ORDER);
                case DOUBLE -> ValueLayout.JAVA_DOUBLE.withOrder(ORDER);
                case BOOLEAN -> ValueLayout.JAVA_BOOLEAN.withOrder(ORDER);
                default -> {
                    if (type.toString().equals("java.lang.String")) {
                        yield MemoryLayout.sequenceLayout(STRING_MAX_SIZE, ValueLayout.JAVA_BYTE);
                    }
                    throw new IllegalArgumentException("Type not supported: " + type);
                }
            };
            orderedLayouts.add(fieldLayout.withName(entry.getKey()));
        }

        StructLayout baseLayout = MemoryLayout.structLayout(
            orderedLayouts.toArray(MemoryLayout[]::new)
        );
        long maxAlignment = baseLayout.byteAlignment();
        long currentSize = baseLayout.byteSize();
        long tailPadding = (maxAlignment - (currentSize % maxAlignment)) % maxAlignment;

        CodeBlock.Builder layoutInitializer = CodeBlock.builder()
            .add("$T.structLayout(", MEMORY_LAYOUT);

        boolean firstLayout = true;
        for (var entry : valuesInRound.entrySet()) {
            if (!firstLayout) {
                layoutInitializer.add(", ");
            }

            String name = entry.getKey();
            TypeMirror type = entry.getValue();
            CodeBlock fieldLayout = switch (type.getKind()) {
                case INT -> CodeBlock.of("$T.JAVA_INT.withOrder($T.BIG_ENDIAN).withName($S)", VALUE_LAYOUT, ByteOrder.class, name);
                case BYTE -> CodeBlock.of("$T.JAVA_BYTE.withOrder($T.BIG_ENDIAN).withName($S)", VALUE_LAYOUT, ByteOrder.class, name);
                case SHORT -> CodeBlock.of("$T.JAVA_SHORT.withOrder($T.BIG_ENDIAN).withName($S)", VALUE_LAYOUT, ByteOrder.class, name);
                case LONG -> CodeBlock.of("$T.JAVA_LONG.withOrder($T.BIG_ENDIAN).withName($S)", VALUE_LAYOUT, ByteOrder.class, name);
                case FLOAT -> CodeBlock.of("$T.JAVA_FLOAT.withOrder($T.BIG_ENDIAN).withName($S)", VALUE_LAYOUT, ByteOrder.class, name);
                case DOUBLE -> CodeBlock.of("$T.JAVA_DOUBLE.withOrder($T.BIG_ENDIAN).withName($S)", VALUE_LAYOUT, ByteOrder.class, name);
                case BOOLEAN -> CodeBlock.of("$T.JAVA_BOOLEAN.withOrder($T.BIG_ENDIAN).withName($S)", VALUE_LAYOUT, ByteOrder.class, name);
                default -> {
                    if (type.toString().equals("java.lang.String")) {
                        yield CodeBlock.of(
                            "$T.sequenceLayout($L, $T.JAVA_BYTE).withName($S)",
                            MEMORY_LAYOUT,
                            STRING_MAX_SIZE,
                            VALUE_LAYOUT,
                            name);
                    }
                    throw new IllegalArgumentException("Type not supported: " + type);
                }
            };
            layoutInitializer.add(fieldLayout);
            firstLayout = false;
        }

        if (tailPadding > 0) {
            if (!firstLayout) {
                layoutInitializer.add(", ");
            }
            layoutInitializer.add("$T.paddingLayout($L)", MEMORY_LAYOUT, tailPadding);
        }

        layoutInitializer.add(")");

        FieldSpec layoutField = FieldSpec.builder(STRUCT_LAYOUT, defaultLayoutName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .initializer(layoutInitializer.build())
                .build();

        String generatedClassName = payloadElement.getSimpleName() + "Layout";
        TypeSpec.Builder generatedClass = TypeSpec.classBuilder(generatedClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(layoutField);

        var writeComponents = new ArrayList<WriteComponent>();

        for (var entry : valuesInRound.entrySet()) {
            String componentName = entry.getKey();
            var typeName = TypeName.get(entry.getValue());

            if (typeName.equals(ClassName.get(String.class))) {
                String stringOffsetDefaultName = componentName + STRING_DEFAULT_OFFSET_NAME;

                FieldSpec stringOffsetField = FieldSpec.builder(int.class, stringOffsetDefaultName)
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .initializer("(int) $L.byteOffset($T.groupElement($S))", DEFAULT_LAYOUT_NAME, PATH_ELEMENT, componentName)
                    .build();

                MethodSpec stringGetterMethod = MethodSpec.methodBuilder(componentName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .returns(String.class)
                    .addParameter(MEMORY_SEGMENT, DEFAULT_SEGMENT_NAME)
                    .addStatement("return $L.getString($L, $T)", DEFAULT_SEGMENT_NAME, stringOffsetDefaultName, STANDARD_CHARSETS_UTF_8)
                    .build();

                generatedClass.addField(stringOffsetField).addMethod(stringGetterMethod);
            } else {
                FieldSpec varHandleField = FieldSpec.builder(VAR_HANDLE, componentName)
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .initializer("$L.varHandle($T.groupElement($S))", DEFAULT_LAYOUT_NAME, PATH_ELEMENT, componentName)
                    .build();

                MethodSpec getterMethod = MethodSpec.methodBuilder(componentName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .returns(typeName)
                    .addParameter(MEMORY_SEGMENT, DEFAULT_SEGMENT_NAME)
                    .addStatement("return ($T) $L.get($L, 0L)", typeName, componentName, DEFAULT_SEGMENT_NAME)
                    .build();

                generatedClass.addField(varHandleField).addMethod(getterMethod);
            }

            var writeComponent = new WriteComponent(componentName, typeName, componentName);
            writeComponents.add(writeComponent);
        }

        String payloadTypeDefaultName = "payloadType";
        TypeName payloadType = TypeName.get(payloadElement.asType());

        FieldSpec payloadTypeField = FieldSpec.builder(payloadType, payloadTypeDefaultName)
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();

        MethodSpec classConstructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(payloadType, payloadTypeDefaultName)
            .addStatement("this.$L = $L", payloadTypeDefaultName, payloadTypeDefaultName)
            .build();

        MethodSpec isNotificationMethod = MethodSpec.methodBuilder("isNotification")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .returns(boolean.class)
            .addStatement("return $L", isNotification)
            .build();

        MethodSpec sizeMethod = MethodSpec.methodBuilder("size")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .returns(int.class)
            .addStatement("return (int) $L.byteSize()", DEFAULT_LAYOUT_NAME)
            .build();

        MethodSpec familyMethod = MethodSpec.methodBuilder("family")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .returns(String.class)
            .addStatement("return $S", familyName)
            .build();

        MethodSpec.Builder writeMethod = MethodSpec.methodBuilder("write")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addParameter(MEMORY_SEGMENT, DEFAULT_SEGMENT_NAME)
            .addParameter(int.class, "offset")
            .returns(void.class);

        for (WriteComponent component : writeComponents) {
            String componentName = component.parameterName();

            if (component.type().equals(ClassName.get(String.class))) {
                writeMethod.addStatement(
                    "$L.setString($L, $L.$L(), $T)",
                    DEFAULT_SEGMENT_NAME,
                    componentName + STRING_DEFAULT_OFFSET_NAME,
                    payloadTypeDefaultName,
                    componentName,
                    STANDARD_CHARSETS_UTF_8);
                continue;
            }

            writeMethod.addStatement("$L.set($L, offset, payloadType.$L())", component.varHandleName(), DEFAULT_SEGMENT_NAME, componentName);
        }

        generatedClass
            .addField(payloadTypeField)
            .addMethod(classConstructor)
            .addMethod(isNotificationMethod)
            .addMethod(sizeMethod)
            .addMethod(familyMethod)
            .addMethod(writeMethod.build());

        generatedClass.addSuperinterface(DataLayout.class);

        String packageName = elementUtils.getPackageOf(payloadElement)
                .getQualifiedName()
                .toString();

        JavaFile javaFile = JavaFile.builder(packageName, generatedClass.build())
                .skipJavaLangImports(true)
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
            return Optional.of(ClassName.get(packageName, generatedClassName));
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.toString());
            return Optional.empty();
        }

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

    private record WriteComponent(
        String varHandleName,
        TypeName type,
        String parameterName
    ) {}
}
