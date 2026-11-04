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

package org.apache.jena.atlas.json;

import java.io.* ;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.PeekReader;
import org.apache.jena.atlas.json.io.JsonGson;
import org.apache.jena.atlas.json.io.JsonWriter ;

/** A class that is the front door to the JSON subsystem */
public class JSON
{
    /** Parse a complete JSON object */
    public static JsonObject parse(InputStream input) {
        return _parse(input) ;
    }

    /** Parse a complete JSON object */
    public static JsonObject parse(String string) {
        return _parse(new StringReader(string)) ;
    }

    /** Parse any JSON value, not just an object, from an input stream */
    public static JsonValue parseAny(InputStream input) {
        return _parseAny(input) ;
    }

    /** Parse any JSON value, not just an object, from a file */
    public static JsonValue parseAny(String string) {
        return _parseAny(new StringReader(string)) ;
    }

    /** Read a JSON object from a file */
    public static JsonObject read(String filename) {
        try (InputStream in = IO.openFileEx(filename)) {
            return JSON.parse(in) ;
        }
        catch (FileNotFoundException ex) {
            IO.exception("File not found: " + filename, ex) ;
            return null ;
        }
        catch (IOException ex) {
            IO.exception("IOException: " + filename, ex) ;
            return null ;
        }
    }

    /** Read any JSON value, not just an object, from a file */
    public static JsonValue readAny(String filename) {
        try {
            try (InputStream in = IO.openFileEx(filename)) {
                return JSON.parseAny(in) ;
            }
        }
        catch (FileNotFoundException ex) {
            throw new RuntimeException("File not found: " + filename, ex) ;
        }
        catch (IOException ex) {
            IO.exception("IOException: " + filename, ex) ;
            return null ;
        }
    }

    private static JsonObject _parse(InputStream input) {
        // PeekReader.makeUTF8: UTF-8 and skip a BOM if present.
        return _parse(PeekReader.makeUTF8(input));
    }

    private static JsonValue _parseAny(InputStream input) {
        return _parseAny(PeekReader.makeUTF8(input));
    }

    private static JsonObject _parse(Reader r) {
        // With an object required, the result is a JsonObject.
        return (JsonObject)_parse(r, true);
    }

    private static JsonValue _parseAny(Reader r) {
        return _parse(r, false);
    }

    /**
     * Parse with gson, which maps the token stream directly onto the Jena JSON
     * object model ({@link JsonGson}). {@code objectExpected} for "a complete JSON object".
     */
    private static JsonValue _parse(Reader r, boolean objectExpected) {
        // The JsonReader is not closed - the caller owns the stream or reader.
        JsonReader jsonReader = new JsonReader(r);
        // Jena JSON has always been more liberal than strict JSON:
        // keys do not need quotes, strings can use '' and comments are allowed.
        jsonReader.setStrictness(Strictness.LENIENT);
        try {
            if ( jsonReader.peek() == JsonToken.END_DOCUMENT )
                throw parseException("Empty input", jsonReader.toString());
            JsonValue jValue = JsonGson.read(jsonReader);
            if ( objectExpected && ! jValue.isObject() )
                throw parseException("JSON object expected", jsonReader.toString());
            if ( jsonReader.peek() != JsonToken.END_DOCUMENT )
                throw parseException("Content after the JSON value", jsonReader.toString());
            return jValue;
        }
        catch (EOFException ex) {
            throw parseException("Empty input", jsonReader.toString());
        }
        catch (MalformedJsonException ex) {
            throw parseException(null, ex.getMessage());
        }
        catch (com.google.gson.JsonParseException ex) {
            // Includes JsonSyntaxException and JsonIOException.
            Throwable reported = ( ex.getCause() != null ) ? ex.getCause() : ex;
            throw parseException(null, reported.getMessage());
        }
        catch (IOException ex) {
            IO.exception(ex);
            return null;
        }
    }

    // gson reports the location of an error in text, both in exception messages and
    // in JsonReader.toString(), in the form "... at line L column C path $...".
    private static final Pattern patternGsonLocation = Pattern.compile(" at line (\\d{1,9}) column (\\d{1,9})");

    /**
     * Create a {@link JsonParseException}, recovering the line and column from the
     * location gson reports in {@code gsonText}. A null {@code message} means "use the
     * text of {@code gsonText}, up to the location".
     */
    private static JsonParseException parseException(String message, String gsonText) {
        int line = -1;
        int column = -1;
        if ( gsonText != null ) {
            Matcher matcher = patternGsonLocation.matcher(gsonText);
            if ( matcher.find() ) {
                line = Integer.parseInt(matcher.group(1));
                column = Integer.parseInt(matcher.group(2));
                if ( message == null )
                    message = gsonText.substring(0, matcher.start());
            }
        }
        if ( message == null )
            message = gsonText;
        return new org.apache.jena.atlas.json.JsonParseException(message, line, column);
    }

    /** JsonValue to a formatted, multiline string */
    public static String toString(JsonValue jValue) {
        try ( IndentedLineBuffer b = new IndentedLineBuffer() ) {
            JSON.write(b, jValue);
            return b.asString() ;
        }
    }

    /** JsonValue to a string with no newlines */
    public static String toStringFlat(JsonValue jValue) {
        try ( IndentedLineBuffer b = new IndentedLineBuffer() ) {
            b.setFlatMode(true);
            JSON.write(b, jValue);
            return b.asString() ;
        }
    }

    /** Write out a JSON value - pass a JSON Object to get legal exchangeable JSON */
    public static void write(OutputStream output, JsonValue jValue) {
        IndentedWriter iOut = new IndentedWriter(output) ;
        write(iOut, jValue) ;
        iOut.flush() ;
    }

    /** Write out a JSON value - pass a JSON Object to get legal exchangeable JSON */
    public static void write(IndentedWriter output, JsonValue jValue) {
        int rowStart = output.getRow();
        JsonWriter w = new JsonWriter(output) ;
        w.startOutput() ;
        jValue.visit(w) ;
        w.finishOutput() ;
        // If multiline, make sure we end on a new line.
        if ( ! output.inFlatMode() && output.getRow() > rowStart )
            output.ensureStartOfLine();
    }

    /** Write out a JSON value - pass a JSON Object to get legal exchangeable JSON */
    public static void write(JsonValue jValue) {
        write(IndentedWriter.stdout, jValue) ;
    }

    // General functions for working with JSON

    /** Create a safe copy of a {@link JsonValue}.
     *  <p>
     *  If the JsonValue is a structure (object or array), copy the structure recursively.
     *  <p>
     *  If the JsonValue is a primitive (string, number, boolean or null),
     *  it is immutable so return the same object.
     */
    public static JsonValue copy(JsonValue arg) {
        return JsonBuilder.copy(arg);
    }

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
        return JsonBuilder.buildObject(setup);
    }
}
