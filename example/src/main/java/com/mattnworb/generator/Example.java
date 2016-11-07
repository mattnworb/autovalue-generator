package com.mattnworb.generator;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect
public class Example {

  @JsonProperty("someString")
  private String string1;

  @JsonProperty("anotherString")
  private String string2;

  @JsonProperty("num")
  private int num;

  private Example() {
  }

  private Example(Builder b) {
    this.string1 = b.string1;
    this.string2 = b.string2;
    this.num = b.num;
  }

  public String string1() {
    return string1;
  }

  public String string2() {
    return string2;
  }

  public int num() {
    return num;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String string1;
    private String string2;
    private int num;

    private Builder() {
    }

    public Builder string1(String s) {
      this.string1 = s;
      return this;
    }

    public Builder string2(String s) {
      this.string2 = s;
      return this;
    }

    public Builder num(int n) {
      this.num = n;
      return this;
    }

    Example build() {
      return new Example(this);
    }
  }
}
