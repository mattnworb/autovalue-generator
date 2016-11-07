package com.mattnworb.generator;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(javax.annotation.processing.Processor.class)
@SupportedAnnotationTypes("com.fasterxml.jackson.annotation.JsonAutoDetect")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class Processor extends AbstractProcessor {

  private Messager messager;
  private Types typeUtils;
  private Elements elementUtils;
  private TestClassGenerator testClassGenerator;

  @Override
  public synchronized void init(final ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    elementUtils = processingEnv.getElementUtils();
    messager = processingEnv.getMessager();
    typeUtils = processingEnv.getTypeUtils();
    testClassGenerator = new TestClassGenerator(typeUtils, elementUtils);
  }

  private void note(String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
  }

  private void warning(String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args));
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations,
                         final RoundEnvironment roundEnv) {

    final Set<? extends Element> elements =
        roundEnv.getElementsAnnotatedWith(JsonAutoDetect.class);

    for (Element element : elements) {

      if (!(element instanceof TypeElement)) {
        warning("Don't know how to handle element %s", element);
        continue;
      }

      final TypeElement typeElement = (TypeElement) element;

      note("Processing %s", typeElement.getQualifiedName());

      final List<JsonFieldInfo> fields = findJsonFields(typeElement);

      if (fields.isEmpty()) {
        continue;
      }

      final JavaFile javaFile = testClassGenerator.generate(
          elementUtils.getPackageOf(typeElement).getQualifiedName(), typeElement, fields);
      note("Would generate test class:\n%s", javaFile);

      try {
        javaFile.writeTo(processingEnv.getFiler());
      } catch (IOException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
      }
    }
    note("Generation finished!");
    return false;
  }

  private List<JsonFieldInfo> findJsonFields(final TypeElement typeElement) {
    final List<JsonFieldInfo> annotatedFields = new ArrayList<>();
    for (Element enclosed : typeElement.getEnclosedElements()) {
      if (enclosed.getKind() != ElementKind.FIELD) {
        continue;
      }
      final VariableElement variableElement = (VariableElement) enclosed;
      final JsonProperty annotation = variableElement.getAnnotation(JsonProperty.class);
      if (annotation != null) {
        final String jsonValue = annotation.value();

        note("Found @JsonProperty annotated field `%s %s` with json value '%s'",
            variableElement.asType(), variableElement.getSimpleName(), jsonValue);

        annotatedFields.add(JsonFieldInfo.of(variableElement, annotation));
      }
    }
    return annotatedFields;
  }
}
