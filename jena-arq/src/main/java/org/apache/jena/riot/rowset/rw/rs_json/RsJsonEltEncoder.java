/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.riot.rowset.rw.rs_json;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

/**
 * Bridge between gson json elements and domain element objects
 * (which are free to (not) abstract from gson)
 *
 * The most basic implementation could turn each event into a gson JsonElement
 * by parameterizing E with JsonElement and having each method
 * return gson.fromJson(reader).
 *
 * @param <E> The uniform element type to create from the raw events
 */
interface RsJsonEltEncoder<E> {
    E newHeadElt   (Gson gson, JsonReader reader) throws IOException;
    E newBooleanElt(Gson gson, JsonReader reader) throws IOException;
    E newResultsElt(Gson gson, JsonReader reader) throws IOException;
    E newBindingElt(Gson gson, JsonReader reader) throws IOException;

    // May just invoke reader.skipValue() and return null
    E newUnknownElt(Gson gson, JsonReader reader)  throws IOException;
}