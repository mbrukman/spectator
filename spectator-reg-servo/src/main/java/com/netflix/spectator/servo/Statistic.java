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
package com.netflix.spectator.servo;

import com.netflix.spectator.api.Tag;

/**
 * The valid set of statistics that can be reported by timers and distribution summaries.
 */
enum Statistic implements Tag {
  /** Rate per second for calls to record. */
  count,

  /** The maximum amount recorded. */
  max,

  /** The sum of the amounts recorded. */
  totalAmount,

  /** The sum of the squares of the amounts recorded. */
  totalOfSquares,

  /** The sum of the times recorded. */
  totalTime;

  @Override public String key() {
    return "statistic";
  }

  @Override public String value() {
    return name();
  }
}
