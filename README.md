# autovalue generator

A set of very specific tools for use in simplfying very verbose POJO files.

Let's say you have a bunch of Java classes that model your JSON data that you
use with `ObjectMapper` for serialization and deserialization which are extra
verbose because they have getters, equals/hashCode/toString, and builders all
written out by hand:

```java
@JsonAutoDetect(...)
public class Flibbit {

  @JsonProperty("Foo")
  private String foo;

  @JsonProperty("Bar")
  private int bar;

  private Flibbit() {
  }

  private Flibbit(Builder b) {
    this.foo = b.foo;
    this.bar = b.bar;
  }

  public String foo() {
    return foo;
  }

  public int bar() {
    return bar
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String foo;
    private int bar;

    private Builder() {
    }

    public Builder foo(String foo) {
      this.foo = foo;
      return this;
    }

    public Builder bar(int bar) {
      this.bar = bar;
      return this;
    }

    public Flibbit build() {
      return new Flibbit(this);
    }
  }
```

This is really verbose! It is about 50 lines of code for a single immutable
value class with two fields, accessors, and a builder - and we didn't even
implement equals/hashCode/toString in this example.

Faced with a bunch of classes like this, you might be tempted to refactor them
to use [AutoValue][autovalue] and [AutoValue Builders][autovalue-builders].

Before doing any refactoring, it would be great if we could create tests to
make sure that our refactoring doesn't break the JSON
serialization/deserialization of these classes in any way. Writing all these
test classes by hand would be as tedious as having to maintain these classes in
the first place.

This tool will generate those test classes for you, and then also (soon)
generate the autovalue-using version of the class.

It does this by means of [annotation processing][], to be able to read the
input classes at the same time the compiler does and emit the test classes.
[JavaPoet][] makes generating Java source files easy.

[annotation processing]: https://docs.oracle.com/javase/7/docs/api/javax/annotation/processing/Processor.html
[autovalue]: https://github.com/google/auto/tree/master/value
[autovalue-builders]: https://github.com/google/auto/blob/master/value/userguide/builders.md
[JavaPoet]: https://github.com/square/javapoet
