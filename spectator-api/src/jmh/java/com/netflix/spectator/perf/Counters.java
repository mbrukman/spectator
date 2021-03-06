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
package com.netflix.spectator.perf;

import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.Spectator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
public class Counters {

  private final Counter cached = Spectator.registry().counter("cachedIncrement");

  @Threads(1)
  @Benchmark
  public void cachedIncrement_T1() {
    cached.increment();
  }

  @Threads(1)
  @Benchmark
  public void lookupIncrement_T1() {
    Spectator.registry().counter("lookupIncrement").increment();
  }

  @Threads(2)
  @Benchmark
  public void cachedIncrement_T2() {
    cached.increment();
  }

  @Threads(2)
  @Benchmark
  public void lookupIncrement_T2() {
    Spectator.registry().counter("lookupIncrement").increment();
  }

  @Threads(4)
  @Benchmark
  public void cachedIncrement_T4() {
    cached.increment();
  }

  @Threads(4)
  @Benchmark
  public void lookupIncrement_T4() {
    Spectator.registry().counter("lookupIncrement").increment();
  }

  @Threads(8)
  @Benchmark
  public void cachedIncrement_T8() {
    cached.increment();
  }

  @Threads(8)
  @Benchmark
  public void lookupIncrement_T8() {
    Spectator.registry().counter("lookupIncrement").increment();
  }

  @TearDown
  public void check() {
    final long cv = cached.count();
    final long lv = Spectator.registry().counter("lookupIncrement").count();
    assert cv > 0 || lv > 0 : "counters haven't been incremented";
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(".*")
        .forks(1)
        .build();
    new Runner(opt).run();
  }
}
