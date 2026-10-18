/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.atlas.json.io;

import java.io.IOException;
import java.math.BigDecimal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.apache.jena.atlas.json.*;
import org.apache.jena.atlas.logging.Log;

/**
 * gson object mapping for the Jena JSON object model.
 * <p>
 * The {@link TypeAdapter} builds {@link JsonValue} objects directly from the gson
 * token stream; there is no intermediate {@code com.google.gson.JsonElement} tree.
 * <p>
 * Reading only - JSON output is {@link JSON#write}.
 */
public class JsonGson {

    private JsonGson() {}

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(JsonValue.class, new JsonValueTypeAdapter())
            .create();

    /**
     * Read one JSON value from the token stream, creating Jena JSON objects.
     * The strictness of the {@link JsonReader} is left as set by the caller.
     * Returns null for an empty token stream.
     */
    public static JsonValue read(JsonReader jsonReader) {
        return gson.fromJson(jsonReader, JsonValue.class);
    }

    /** Map the gson token stream onto the Jena JSON object model. */
    private static class JsonValueTypeAdapter extends TypeAdapter<JsonValue> {

        @Override
        public JsonValue read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            switch (token) {
                case BEGIN_OBJECT : {
                    JsonObject jsonObject = new JsonObject();
                    in.beginObject();
                    while (in.hasNext()) {
                        String key = in.nextName();
                        if ( jsonObject.hasKey(key) )
                            Log.warn("JSON", "Duplicate key '"+key+"' for object at "+in.getPath());
                        jsonObject.put(key, read(in));
                    }
                    in.endObject();
                    return jsonObject;
                }
                case BEGIN_ARRAY : {
                    JsonArray jsonArray = new JsonArray();
                    in.beginArray();
                    while (in.hasNext())
                        jsonArray.add(read(in));
                    in.endArray();
                    return jsonArray;
                }
                case STRING :
                    return new JsonString(in.nextString());
                case NUMBER :
                    // JsonNumber is a BigDecimal: take the number as written.
                    return JsonNumber.value(new BigDecimal(in.nextString()));
                case BOOLEAN :
                    return new JsonBoolean(in.nextBoolean());
                case NULL :
                    in.nextNull();
                    return JsonNull.instance;
                default :
                    throw new JsonException("Not the start of a JSON value: "+token);
            }
        }

        @Override
        public void write(JsonWriter out, JsonValue jValue) {
            throw new UnsupportedOperationException("JsonGson is read-only: use JSON.write");
        }
    }
}
