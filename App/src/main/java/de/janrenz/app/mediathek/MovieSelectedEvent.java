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
import java.util.ArrayList;

import de.janrenz.app.mediathek.Movie;;
public class MovieSelectedEvent {
  public final int pos;
  public final int dayTimestamp;
  public final String extId;
  public final ArrayList<Movie> mList;

  public MovieSelectedEvent(int pos, String extId, int dayTimestamp, ArrayList<Movie> mList ) {
    this.pos = pos;
    this.extId = extId;
    this.dayTimestamp = dayTimestamp;
    this.mList = mList;
  }

  @Override public String toString() {
    return new StringBuilder("(") //
        .append(", ") //
        .append(extId) //
        .append(")") //
        .toString();
  }
}