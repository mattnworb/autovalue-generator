package com.mattnworb.generator;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

class JsonFieldInfo {

  static JsonFieldInfo of(VariableElement element, JsonProperty annotation) {
    return new JsonFieldInfo(
        element.asType(), element.getSimpleName().toString(), annotation.value());
  }

  private final TypeMirror fieldType;
  private final String fieldName;
  private final String jsonFieldName;

  private JsonFieldInfo(
      final TypeMirror fieldType,
      final String fieldName,
      final String jsonFieldName) {
    this.fieldType = fieldType;
    this.fieldName = fieldName;
    this.jsonFieldName = jsonFieldName;
  }

  TypeMirror getFieldType() {
    return fieldType;
  }

  String getFieldName() {
    return fieldName;
  }

  String getJsonFieldName() {
    return jsonFieldName;
  }
}
