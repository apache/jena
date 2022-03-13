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

package org.apache.jena.riot.rowset.rw;

import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kBindings;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kBoolean;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kHead;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kResults;

import java.io.IOException;

import org.apache.jena.atlas.iterator.IteratorSlotted;
import org.apache.jena.sparql.resultset.ResultSetException;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

/**
 * An iterator that reads sparql result set domain elements from a
 * underlying gson JsonReader.
 *
 * This iterator is 'dumb': it neither validates nor keeps statistics.
 *
 * For this purpose the iterator delegates to {@link RsJsonEltEncoder} which
 * is responsible for creating instances of type E from the state of the
 * json stream.
 *
 * @param <E> The type of elements to yield by this iterator
 */
public class IteratorRsJSON<E>
    extends IteratorSlotted<E>
{
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
    public interface RsJsonEltEncoder<E> {
        E newHeadElt   (Gson gson, JsonReader reader) throws IOException;
        E newBooleanElt(Gson gson, JsonReader reader) throws IOException;
        E newResultsElt(Gson gson, JsonReader reader) throws IOException;
        E newBindingElt(Gson gson, JsonReader reader) throws IOException;

        // May just invoke reader.skipValue() and return null
        E newUnknownElt(Gson gson, JsonReader reader)  throws IOException;
    }

    /** Parsing state; i.e. where we are in the json document */
    public enum ParserState {
        INIT,
        ROOT,
        RESULTS,
        BINDINGS,
        DONE
    }

    protected ParserState parserState;
    protected RsJsonEltEncoder<E> eltEncoder;

    protected Gson gson;
    protected JsonReader reader;

    public IteratorRsJSON(Gson gson, JsonReader jsonReader, RsJsonEltEncoder<E> eltEncoder) {
        this(gson, jsonReader, eltEncoder, ParserState.INIT);
    }

    /**
     * Constructor that allows setting the initial parser state such as
     * when starting to parse in a hadoop input split.
     */
    public IteratorRsJSON(Gson gson, JsonReader jsonReader, RsJsonEltEncoder<E> eltEncoder, ParserState parserState) {
        this.gson = gson;
        this.reader = jsonReader;
        this.eltEncoder = eltEncoder;
        this.parserState = parserState;
    }

    @Override
    protected E moveToNext() {
        E result;
        try {
            result = computeNextActual();
        } catch (Throwable e) {
            // Re-wrap any exception
            throw new ResultSetException(e.getMessage(), e);
        }
        return result;
    }

    protected E computeNextActual() throws IOException {
        E result;
        outer: while (true) {
            switch (parserState) {
            case INIT:
                reader.beginObject();
                parserState = ParserState.ROOT;
                continue outer;

            case ROOT:
                while (reader.hasNext()) {
                    String topLevelName = reader.nextName();
                    switch (topLevelName) {
                    case kHead:
                        result = eltEncoder.newHeadElt(gson, reader);
                        break outer;
                    case kResults:
                        reader.beginObject();
                        parserState = ParserState.RESULTS;
                        result = eltEncoder.newResultsElt(gson, reader);
                        break outer;
                    case kBoolean:
                        result = eltEncoder.newBooleanElt(gson, reader);
                        break outer;
                    default:
                        result = eltEncoder.newUnknownElt(gson, reader);
                        break outer;
                    }
                }
                reader.endObject();
                parserState = ParserState.DONE;
                continue outer;

            case RESULTS:
                while (reader.hasNext()) {
                    String elt = reader.nextName();
                    switch (elt) {
                    case kBindings:
                        reader.beginArray();
                        parserState = ParserState.BINDINGS;
                        continue outer;

                    // Legacy distinct / ordered keys could be caught here too
                    // in order to assess use of legacy features in validation

                    default:
                        result = eltEncoder.newUnknownElt(gson, reader);
                        break;
                    }
                }
                reader.endObject();
                parserState = ParserState.ROOT;
                break;

            case BINDINGS:
                while (reader.hasNext()) {
                    result = eltEncoder.newBindingElt(gson, reader);
                    break outer;
                }
                reader.endArray();
                parserState = ParserState.RESULTS;
                break;

            case DONE:
                result = null;
                break outer;
            }
        }

        return result;
    }

    @Override
    protected boolean hasMore() {
        return true;
    }

    @Override
    public void closeIterator() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new ResultSetException("IOException on closing the underlying stream", e);
        }
    }
}
