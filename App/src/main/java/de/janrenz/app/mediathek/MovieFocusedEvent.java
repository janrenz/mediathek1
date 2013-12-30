/*
 * Copyright (C) 2013 Jan Renz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.janrenz.app.mediathek;

public class MovieFocusedEvent {
  public final int pos;
  public final int dayTimestamp;

  public MovieFocusedEvent(int pos, int dayTimestamp ) {
    this.pos = pos;
    this.dayTimestamp = dayTimestamp;
  }

  @Override public String toString() {
    return new StringBuilder("(") //
        .append(", ") //
        .append(pos) //
        .append(")") //
        .append("td:"+dayTimestamp)
        .toString();
  }
}