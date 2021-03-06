/**
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.spectator.api;

import java.util.NoSuchElementException;

/**
 * Base class for {@code ConfigMap} implementations.
 */
public abstract class AbstractConfigMap implements ConfigMap {

  @Override public String get(String key, String dflt) {
    final String v = get(key);
    return (v == null) ? dflt : v;
  }

  private String getOrThrow(String key) {
    final String v = get(key);
    if (v == null) {
      throw new NoSuchElementException(key);
    }
    return v;
  }

  @Override public int getInt(String key) {
    return Integer.parseInt(getOrThrow(key));
  }

  @Override public int getInt(String key, int dflt) {
    final String v = get(key);
    return (v == null) ? dflt : Integer.parseInt(v);
  }

  @Override public long getLong(String key) {
    return Long.parseLong(getOrThrow(key));
  }

  @Override public long getLong(String key, long dflt) {
    final String v = get(key);
    return (v == null) ? dflt : Long.parseLong(v);
  }

  @Override public double getDouble(String key) {
    return Double.parseDouble(getOrThrow(key));
  }

  @Override public double getDouble(String key, double dflt) {
    final String v = get(key);
    return (v == null) ? dflt : Double.parseDouble(v);
  }

  @Override public boolean getBoolean(String key) {
    return Boolean.parseBoolean(getOrThrow(key));
  }

  @Override public boolean getBoolean(String key, boolean dflt) {
    final String v = get(key);
    return (v == null) ? dflt : Boolean.parseBoolean(v);
  }
}
