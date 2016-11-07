package com.mattnworb.generator;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

public class ProcessorTest {

  @Test
  public void testNoAnnotations() {
    final Compilation compilation = javac()
        .withProcessors(new Processor())
        .compile(JavaFileObjects.forSourceLines("HelloWorld", "final class HelloWorld {}"));

    assertThat(compilation).succeeded();
  }

  @Test
  public void testGeneratesFile() {
    final Compilation compilation = javac()
        .withProcessors(new Processor())
        .compile(JavaFileObjects.forSourceLines("HelloWorld",
            "import com.fasterxml.jackson.annotation.JsonAutoDetect;",
            "import com.fasterxml.jackson.annotation.JsonProperty; ",
            "",
            "@JsonAutoDetect ",
            "final class HelloWorld { ",
            "   @JsonProperty(\"foo\") ",
            "   private String foo;",
            "   private HelloWorld() {}",
            "   private HelloWorld(Builder b) { this.foo = b.foo; }",
            "   public String foo() { return foo; }",
            "   public static Builder builder() { return new Builder(); }",
            "   public static class Builder {",
            "     private String foo;",
            "     private Builder() {}",
            "     public Builder foo(String s) { foo = s; return this; }",
            "     public HelloWorld build() { return new HelloWorld(this); }",
            "   }",
            "}"
        ));

    assertThat(compilation).succeeded();
  }
}
