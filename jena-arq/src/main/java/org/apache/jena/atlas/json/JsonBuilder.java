/**
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

package org.apache.jena.atlas.json ;

import java.math.BigDecimal ;
import java.util.ArrayDeque ;
import java.util.Deque ;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.jena.atlas.logging.Log ;

/**
 * Builder pattern for JSON.
 * The JsonValue built can be an array or object at the outmost level, but not an atomic value.
 */
public class JsonBuilder {

    // If not an array or object.
    private JsonValue         builtValue    = null ;
    private static final String NoMarker    = "" ;
    private Deque<String>     markers       = new ArrayDeque<>() ;
    private Deque<JsonArray>  arrays        = new ArrayDeque<>() ;
    private Deque<JsonObject> objects       = new ArrayDeque<>() ;

    private static enum State {
        ARRAY, OBJECT
    }
    private Deque<State>  stack = new ArrayDeque<>() ;

    // The depth of this stack is the object depth. key: { key: ... }
    private Deque<String> keys  = new ArrayDeque<>() ;

    public static JsonBuilder create() { return new JsonBuilder() ; }

    /** Create a builder from a {@link JsonValue}.
     *  <p>If the argument is an object or array, use it to initialize the builder.
     *  <p>If the argument is a JSON primitive (string, number, boolean or null),
     *  <p>Otherwise thrown {@link IllegalArgumentException}.
     */
    public static JsonBuilder createFrom(JsonValue arg) {
        if ( arg.isObject() ) {
            JsonObject obj = arg.getAsObject() ;
            JsonBuilder builder = JsonBuilder.create() ;
            builder.startObject() ;
            obj.forEach((k,v) -> builder.key(k).value(copy(v))) ;
            builder.finishObject() ;
            return builder ;
        }
        if ( arg.isArray() ) {
            JsonArray array = arg.getAsArray() ;
            JsonBuilder builder = JsonBuilder.create() ;
            builder.startArray() ;
            array.forEach((a)->builder.value(copy(a))) ;
            builder.finishArray() ;
            return builder ;
        }
        throw new IllegalArgumentException("Not a JSON object or JSON array; "+arg);
    }


    /** Create a safe copy of a {@link JsonValue}.
     *  <p>
     *  If the JsonValue is a structure (object or array), copy the structure recursively.
     *  <p>
     *  If the JsonValue is a primitive (string, number, boolean or null),
     *  it is immutable so return the same object.
     */
    public static JsonValue copy(JsonValue arg) {
        if ( ! arg.isArray() && ! arg.isObject() )
            return arg;
        return createFrom(arg).build();
    }

    // An unlikely-to-be-used label to help check object alignment
    private static String LABEL = "%|%object%|%" ;

    /** Build a JsonObject.  The outer object is created and then the {@code setup} function called to fill in the contents.
     * <pre>
     * buildObject(builder-&gt;{
     *     builder.pair("key", 1234);
     * });
     * </pre>
     *
     * @param setup
     * @return JsonObject
     */
    public static JsonObject buildObject(Consumer<JsonBuilder> setup) {
        JsonBuilder b = JsonBuilder.create().startObject(LABEL) ;
        setup.accept(b);
        return b.finishObject(LABEL).build().getAsObject() ;
    }

    public JsonBuilder() {
    }

    public JsonValue build() {
        if ( builtValue == null ) {
            if ( objects.isEmpty() && arrays.isEmpty() )
                throw new JsonException("Alignment error: no object or array started") ;
            throw new JsonException("Alignment error: unfinished outer object or array") ;
        }
        return builtValue ;
    }

    public void reset() {
        builtValue = null ;
        stack.clear() ;
        objects.clear() ;
        keys.clear();
        arrays.clear();
    }

    public JsonBuilder startObject() { return startObject(NoMarker) ; }

    public JsonBuilder startObject(String startMarker) {
        markers.push(startMarker);
        objects.push(new JsonObject()) ;
        stack.push(State.OBJECT) ;
        return this ;
    }

    public JsonBuilder finishObject() { return finishObject(NoMarker) ; }

    public JsonBuilder finishObject(String finishMarker) {
        if ( stack.isEmpty() )
            throw new JsonException("Alignment error : already built outer-most object or array") ;
        State state = stack.pop() ;
        if ( state != State.OBJECT )
            throw new JsonException("JSON build error : not in an object") ;
        JsonValue value = objects.pop() ;
        maybeObjectOrArray(value) ;
        if ( stack.isEmpty() )
            builtValue = value ;
        String startMarker = markers.pop();
        if ( ! Objects.equals(startMarker, finishMarker) )
            throw new JsonException("JSON build error : start/finish alignment error: start="+startMarker+"  finish="+finishMarker) ;
        return this ;
    }

    public JsonBuilder startArray() {
        arrays.push(new JsonArray()) ;
        stack.push(State.ARRAY) ;
        return this ;
    }

    public JsonBuilder finishArray() {
        if ( stack.isEmpty() )
            throw new JsonException("Alignment error : already built outer-most object or array") ;

        State state = stack.pop() ;
        if ( state != State.ARRAY )
            throw new JsonException("JSON build error : not in an array") ;

        JsonValue value = arrays.pop() ;
        maybeObjectOrArray(value) ;
        if ( stack.isEmpty() )
            builtValue = value ;
        return this ;
    }

    public JsonBuilder pair(String key, JsonValue value) {
        key(key);
        value(value);
        return this ;
    }

    public JsonBuilder pair(String key, boolean value) {
        key(key);
        value(value);
        return this ;
    }

    public JsonBuilder pair(String key, BigDecimal value) {
        key(key);
        value(value);
        return this ;
    }

    public JsonBuilder pair(String key, double value) {
        key(key);
        value(value);
        return this ;
    }

    public JsonBuilder pair(String key, long value) {
        key(key);
        value(value);
        return this ;
    }

    public JsonBuilder pair(String key, String value) {
        key(key);
        value(value);
        return this ;
    }

    public JsonBuilder key(String key) {
        State state = stack.peek() ;
        if ( state != State.OBJECT )
            throw new JsonException("JSON build error : not in an object") ;
        keys.push(key) ;
        return this ;
    }

    public JsonBuilder remove(String key) {
        State state = stack.peek() ;
        if ( state != State.OBJECT )
            throw new JsonException("JSON build error : not in an object") ;
        objects.peek().remove(key);
        return this ;
    }

    private void maybeObjectOrArray(JsonValue value) {
        if ( stack.size() == 0 )
            // Error.
            return ;

        switch (stack.peek()) {
            case OBJECT : {
                String k = keys.pop() ;
                JsonObject obj = objects.peek() ;
                if ( obj.hasKey(k) )
                    Log.warn(this, "Duplicate key '" + k + "' for object") ;
                obj.put(k, value) ;
                return ;
            }
            case ARRAY : {
                arrays.peek().add(value) ;
                return ;
            }
        }
    }

    public JsonBuilder value(JsonValue v) {
        maybeObjectOrArray(v) ;
        return this ;
    }

    public JsonBuilder value(boolean b) {
        JsonValue value = new JsonBoolean(b) ;
        maybeObjectOrArray(value) ;
        return this ;
    }

    public JsonBuilder value(BigDecimal decimal) {
        JsonValue value = JsonNumber.value(decimal) ;
        maybeObjectOrArray(value) ;
        return this ;
    }

    public JsonBuilder value(double d) {
        JsonValue value = JsonNumber.value(d) ;
        maybeObjectOrArray(value) ;
        return this ;
    }

    public JsonBuilder value(long val) {
        JsonValue value = JsonNumber.value(val) ;
        maybeObjectOrArray(value) ;
        return this ;
    }

    public JsonBuilder valueNull() {
        JsonValue value = JsonNull.instance ;
        maybeObjectOrArray(value) ;
        return this ;
    }

    public JsonBuilder value(String string) {
        JsonValue value = new JsonString(string) ;
        maybeObjectOrArray(value) ;
        return this ;
    }
}
