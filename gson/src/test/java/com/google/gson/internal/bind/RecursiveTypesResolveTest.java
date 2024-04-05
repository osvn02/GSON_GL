/*
 * Copyright (C) 2017 Gson Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal.bind;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.GsonTypeChecker;
import org.junit.Test;

/**
 * Test fixes for infinite recursion on {@link GsonTypeChecker#resolve(java.lang.reflect.Type, Class,
 * java.lang.reflect.Type)}, described at <a href="https://github.com/google/gson/issues/440">Issue
 * #440</a> and similar issues.
 *
 * <p>These tests originally caused {@link StackOverflowError} because of infinite recursion on
 * attempts to resolve generics on types, with intermediate types like {@code Foo2<? extends ? super
 * ? extends ... ? extends A>}
 */
public class RecursiveTypesResolveTest {

  @SuppressWarnings("unused")
  private static class Foo1<A> {
    public Foo2<? extends A> foo2;
  }

  @SuppressWarnings("unused")
  private static class Foo2<B> {
    public Foo1<? super B> foo1;
  }

  /** Test simplest case of recursion. */
  @Test
  public void testRecursiveResolveSimple() {
    @SuppressWarnings("rawtypes")
    TypeAdapter<Foo1> adapter = new Gson().getAdapter(Foo1.class);
    assertThat(adapter).isNotNull();
  }

  /** Tests below check the behavior of the methods changed for the fix. */
  @Test
  public void testDoubleSupertype() {
    assertThat(GsonTypeChecker.supertypeOf(GsonTypeChecker.supertypeOf(Number.class)))
        .isEqualTo(GsonTypeChecker.supertypeOf(Number.class));
  }

  @Test
  public void testDoubleSubtype() {
    assertThat(GsonTypeChecker.subtypeOf(GsonTypeChecker.subtypeOf(Number.class)))
        .isEqualTo(GsonTypeChecker.subtypeOf(Number.class));
  }

  @Test
  public void testSuperSubtype() {
    assertThat(GsonTypeChecker.supertypeOf(GsonTypeChecker.subtypeOf(Number.class)))
        .isEqualTo(GsonTypeChecker.subtypeOf(Object.class));
  }

  @Test
  public void testSubSupertype() {
    assertThat(GsonTypeChecker.subtypeOf(GsonTypeChecker.supertypeOf(Number.class)))
        .isEqualTo(GsonTypeChecker.subtypeOf(Object.class));
  }

  /** Tests for recursion while resolving type variables. */
  @SuppressWarnings("unused")
  private static class TestType<X> {
    TestType<? super X> superType;
  }

  @SuppressWarnings("unused")
  private static class TestType2<X, Y> {
    TestType2<? super Y, ? super X> superReversedType;
  }

  @Test
  public void testRecursiveTypeVariablesResolve1() {
    @SuppressWarnings("rawtypes")
    TypeAdapter<TestType> adapter = new Gson().getAdapter(TestType.class);
    assertThat(adapter).isNotNull();
  }

  @Test
  public void testRecursiveTypeVariablesResolve12() {
    @SuppressWarnings("rawtypes")
    TypeAdapter<TestType2> adapter = new Gson().getAdapter(TestType2.class);
    assertThat(adapter).isNotNull();
  }
}
