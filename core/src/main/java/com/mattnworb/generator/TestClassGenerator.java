package com.mattnworb.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Date;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

class TestClassGenerator {

  private final Types types;
  private final Elements elements;
  private final AnnotationSpec testAnnotation;

  TestClassGenerator(final Types types, final Elements elements) {
    this.types = types;
    this.elements = elements;
    testAnnotation = AnnotationSpec.builder(ClassName.get("org.junit", "Test")).build();
  }

  JavaFile generate(final Name packageName, final TypeElement typeElement,
                    final List<JsonFieldInfo> annotatedFields) {

    final String testClassName = typeElement.getSimpleName() + "Test";

    final TypeSpec typeSpec = TypeSpec.classBuilder(testClassName)
        .addField(objectMapper())
        .addMethod(serializeMethod(typeElement, annotatedFields))
        .addMethod(deserializeMethod(typeElement, annotatedFields))
        .build();

    return JavaFile.builder(packageName.toString(), typeSpec)
        .addFileComment("Generated by auto-value-generator at $L", new Date())
        .addStaticImport(ClassName.get("org.junit", "Assert"), "assertEquals")
        .skipJavaLangImports(true)
        .build();
  }

  private FieldSpec objectMapper() {
    return FieldSpec.builder(TypeName.get(ObjectMapper.class), "objectMapper")
        .addModifiers(Modifier.PRIVATE)
        .addModifiers(Modifier.FINAL)
        .initializer("new ObjectMapper()")
        .build();
  }

  private MethodSpec serializeMethod(final TypeElement typeElement,
                                     final List<JsonFieldInfo> annotatedFields) {

    return testMethod("testSerialization")
        .addCode(instantiateObjectNode("expected", annotatedFields))
        .addCode(instantiatePojo("value", typeElement, annotatedFields))
        .addStatement("assertEquals(expected, objectMapper.writeValueAsString(value))")
        .build();
  }

  private MethodSpec deserializeMethod(final TypeElement typeElement,
                                       final List<JsonFieldInfo> annotatedFields) {
    return testMethod("testDeserialization")
        .addCode(instantiatePojo("expected", typeElement, annotatedFields))
        .addCode(instantiateObjectNode("value", annotatedFields))
        .addStatement("assertEquals(expected, objectMapper.readValue(value.toString(), $T.class))",
            typeElement)
        .build();
  }

  private MethodSpec.Builder testMethod(String name) {
    return MethodSpec.methodBuilder(name)
        .addAnnotation(testAnnotation)
        .addException(Exception.class);
  }

  private CodeBlock instantiatePojo(
      final String varName,
      final TypeElement typeElement,
      final List<JsonFieldInfo> annotatedFields) {
    final CodeBlock.Builder builder = CodeBlock.builder()
        .add("$[final $T $L = $T.builder()", typeElement, varName, typeElement);

    for (JsonFieldInfo field : annotatedFields) {
      final Object generatedValue = fieldValue(field);
      final String format = generatedValue instanceof String
                            ? "\n.$L($S)"
                            : "\n.$L($L)";
      builder.add(format, field.getFieldName(), generatedValue);
    }

    return builder.add("\n.build();\n$]\n")
        .build();
  }

  private CodeBlock instantiateObjectNode(
      final String varName,
      final List<JsonFieldInfo> annotatedFields) {
    final CodeBlock.Builder builder = CodeBlock.builder()
        .add("$[final $T $L = objectMapper.createObjectNode()",
            TypeName.get(ObjectNode.class), varName);

    for (JsonFieldInfo field : annotatedFields) {
      final Object generatedValue = fieldValue(field);
      final String format = generatedValue instanceof String
                            ? "\n.put($S, $S)"
                            : "\n.put($S, $L)";
      builder.add(format, field.getJsonFieldName(), generatedValue);
    }

    return builder.add(";\n$]\n")
        .build();
  }

  private Object fieldValue(final JsonFieldInfo field) {
    final TypeMirror type = field.getFieldType();
    switch (type.getKind()) {
      case BOOLEAN:
        return true;
      case LONG:
      case INT:
      case BYTE:
      case SHORT:
        return 1;
      case CHAR:
        return 'a';
      case FLOAT:
      case DOUBLE:
        return 1.1;
      case DECLARED:
        // check for string
        if (type.toString().equals("java.lang.String")) {
          return field.getFieldName();
        }
      default:
        throw new IllegalArgumentException("Unsupported type of JSON field: " + type);

    }
  }
}