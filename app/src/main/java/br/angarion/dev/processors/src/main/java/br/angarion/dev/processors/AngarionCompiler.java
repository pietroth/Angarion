package br.angarion.dev.processors;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.google.auto.service.AutoService;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;

import javax.tools.Diagnostic;

import br.angarion.dev.api.communication.DataField;
import br.angarion.dev.api.communication.FamilyConfiguration;
import br.angarion.dev.api.communication.TypeConfiguration;
import br.angarion.dev.engine.communication.IdentifierGlossary;
import br.angarion.dev.engine.communication.codec.DataLayout;
import br.angarion.dev.engine.communication.validator.Validator;
import br.angarion.dev.engine.usecase.UseCase;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_25)
@SupportedAnnotationTypes(
    {
        "br.angarion.dev.api.communication.FamilyConfiguration",
        "br.angarion.dev.api.communication.TypeConfiguration",
        "br.angarion.dev.api.communication.DataField",
        "br.angarion.dev.api.communication.GenericValidation",
        "br.angarion.dev.api.communication.ValidationPerFamily"
    })
public class AngarionCompiler extends AbstractProcessor {
    private IdentifierGlossary glossary;
    
    private Types typeUtils;
    private Elements elementUtils;

    public AngarionCompiler() {}

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.glossary = new IdentifierGlossary(); // Temporary solution

        this.typeUtils = processingEnv.getTypeUtils();
        this.elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){

        // Family processing

        for (Element el : roundEnv.getElementsAnnotatedWith(FamilyConfiguration.class)) {
            FamilyConfiguration configuration = el.getAnnotation(FamilyConfiguration.class);

            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Processing Family: " + el.getSimpleName()
            );

            for (Element enclosed : el.getEnclosedElements()) {
                if (enclosed.getKind().isField() && enclosed.getAnnotation(DataField.class) != null) {
                    processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE, 
                        "Found annotated field: " + enclosed.getSimpleName()
                    );
                }
            }

            glossary.registerFamily(configuration.value()); // register family
        }

        // Type processing

        TypeElement typeElement = elementUtils.getTypeElement("br.angarion.dev.api.communication.Type");
        if (typeElement == null) {
            processingEnv.getMessager().printError
                ("br.angarion.dev.api.communication.Type was not found in the classpath");
        }
        TypeMirror targetInterfaceType = typeElement.asType();

        for (Element el : roundEnv.getElementsAnnotatedWith(TypeConfiguration.class)) {
            TypeElement familyElement = getFamilyElement(el);
            FamilyConfiguration familyConfiguration = familyElement.getAnnotation(FamilyConfiguration.class);
            String familyName = familyConfiguration != null ? familyConfiguration.value() : "<missing-family-configuration>";

            TypeConfiguration configuration = el.getAnnotation(TypeConfiguration.class);

            TypeMirror elementType = el.asType();

            if (!typeUtils.isSubtype(elementType, targetInterfaceType)) {
                processingEnv.getMessager().printError
                    ("Type class must implement br.angarion.dev.api.communication.Type interface besides br.angarion.dev.api.communication.TypeConfiguration annotation");
            }

            /*
                begin DataLayout Builder
            */
            TypeSpec.Builder dataLayoutBuilder = TypeSpec.classBuilder(configuration.name() + "$DL")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(DataLayout.class);
            
            // end Codec Builder

            /*
                begin UseCase Builder
            */
            TypeSpec.Builder useCaseBuilder = TypeSpec.classBuilder(configuration.name() + "$UC")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(UseCase.class);

            // end UseCase Builder

            /*
                begin Validator Builder
            */
            TypeSpec.Builder validatorBuilder = TypeSpec.classBuilder(configuration.name() + "$V")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(Validator.class);

            // end Validator Builder

            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Processing Type: " + el.getSimpleName() + " from family: " + familyName
            );

            for (Element enclosed : el.getEnclosedElements()) {
                if (enclosed.getKind().isField() && enclosed.getAnnotation(DataField.class) != null) {
                    processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE, 
                        "Found annotated field: " + enclosed.getSimpleName()
                    );

                    /*
                        For each field marked as @DataField, create and add a field in codecBuilder
                    */
                    dataLayoutBuilder.addField(FieldSpec.builder(TypeName.get(enclosed.asType()), enclosed.getSimpleName().toString())
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .build()
                    );
                }
            }

            glossary.registerType(familyName, configuration.name()); // register type
        }

        glossary.snapshot().forEach((key, value) -> {
            processingEnv.getMessager().printMessage
                (Diagnostic.Kind.NOTE, "Family name: " + key + "; FamilyId: " + value.id() + ". " + value.types());
        });

        return true;
    }

    private TypeElement getFamilyElement(Element typeElement) {
        // Walk through the raw annotation model instead of resolving Class<?> directly.
        for (AnnotationMirror annotationMirror : typeElement.getAnnotationMirrors()) {
            Element annotationElement = annotationMirror.getAnnotationType().asElement();
            // Filter only the @TypeConfiguration annotation instance.
            if (!(annotationElement instanceof TypeElement annotationType)
                || !annotationType.getQualifiedName().contentEquals(TypeConfiguration.class.getCanonicalName())) {
                continue;
            }

            // Inspect declared annotation arguments until the family attribute is found.
            for (var entry : annotationMirror.getElementValues().entrySet()) {
                if (!entry.getKey().getSimpleName().contentEquals("family")) {
                    continue;
                }

                Object value = entry.getValue().getValue();
                // The family attribute is exposed as a declared source-level type.
                if (value instanceof DeclaredType declaredType) {
                    Element familyElement = declaredType.asElement();
                    // Return the concrete family type so its own annotations can be inspected.
                    if (familyElement instanceof TypeElement familyType) {
                        return familyType;
                    }
                }
            }
        }

        throw new IllegalStateException("Unable to resolve family for type: " + typeElement.getSimpleName());
    }
    
}
