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

import static org.apache.jena.riot.rowset.rw.JSONResultsKW.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.jena.atlas.data.BagFactory;
import org.apache.jena.atlas.data.DataBag;
import org.apache.jena.atlas.data.ThresholdPolicy;
import org.apache.jena.atlas.data.ThresholdPolicyFactory;
import org.apache.jena.atlas.iterator.IteratorSlotted;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetBuffered;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.system.SerializationFactoryFinder;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.vocabulary.RDF;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;


/**
 * Streaming RowSet implementation for application/sparql-results+json
 * The {@link #getResultVars()} will return null as long as the header has not
 * been consumed from the underlying stream.
 *
 * Use {@link BufferedRowSet} to modify the behavior such that {@link #getResultVars()}
 * immediately consumes the underlying stream until the header is read,
 * thereby buffering any encountered bindings for replay.
 *
 * Use {@link #createBuffered(InputStream, Context)} to create a buffered row set
 * with appropriate configuration w.r.t. ARQ.inputGraphBNodeLabels and ThresholdPolicyFactory.
 *
 */
public class RowSetJSONStreaming
    extends IteratorSlotted<Binding>
    implements RowSet
{
    public static RowSetBuffered<RowSetJSONStreaming> createBuffered(InputStream in, Context context) {
//        try {
//            byte[] buf = IOUtils.toByteArray(in);
//            System.out.println(new String(buf));
//            in = new ByteArrayInputStream(buf);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        Context cxt = context == null ? ARQ.getContext() : context;

        boolean inputGraphBNodeLabels = cxt.isTrue(ARQ.inputGraphBNodeLabels);
        LabelToNode labelMap = inputGraphBNodeLabels
            ? SyntaxLabels.createLabelToNodeAsGiven()
            : SyntaxLabels.createLabelToNode();

        Supplier<DataBag<Binding>> bufferFactory = () -> {
            ThresholdPolicy<Binding> policy = ThresholdPolicyFactory.policyFromContext(cxt);
            DataBag<Binding> r = BagFactory.newDefaultBag(policy, SerializationFactoryFinder.bindingSerializationFactory());
            return r;
        };

        return createBuffered(in, labelMap, bufferFactory, true);
    }


    public static RowSetBuffered<RowSetJSONStreaming> createBuffered(
            InputStream in, LabelToNode labelMap,
            Supplier<DataBag<Binding>> bufferFactory, boolean enableValidation) {
        return new RowSetBuffered<>(createUnbuffered(in, labelMap, enableValidation), bufferFactory);
    }

    public static RowSetJSONStreaming createUnbuffered(InputStream in, LabelToNode labelMap, boolean enableValidation) {
        Gson gson = new Gson();
        JsonReader reader = gson.newJsonReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        RowSetJSONStreaming result = new RowSetJSONStreaming(gson, reader, LabelToNode.createUseLabelAsGiven(), enableValidation);
        return result;
    }

    /** Parsing state; i.e. where we are in the json document */
    public enum State {
        INIT,
        ROOT,
        RESULTS,
        BINDINGS,
        DONE
    }

    protected Gson gson;
    protected JsonReader reader;

    protected List<Var> resultVars = null;
    protected long rowNumber;

    protected Boolean askResult = null;


    protected LabelToNode labelMap;

    // Hold the context for reference?
    // protected Context context;

    protected Function<JsonObject, Node> onUnknownRdfTermType = null;
    protected ErrorHandler errorHandler;

    protected boolean enableValidation;

    protected int kHeadCount = 0;
    protected int kResultsCount = 0;
    protected int kBooleanCount = 0;

    protected State state;


    public RowSetJSONStreaming(Gson gson, JsonReader reader, LabelToNode labelMap, boolean enableValidation) {
        this(gson, reader, labelMap, null, 0, enableValidation);
    }

    public RowSetJSONStreaming(Gson gson, JsonReader reader, LabelToNode labelMap, List<Var> resultVars, long rowNumber, boolean enableValidation) {
        super();
        this.gson = gson;
        this.reader = reader;
        this.labelMap = labelMap;

        this.resultVars = resultVars;
        this.rowNumber = rowNumber;

        this.enableValidation = enableValidation;

        this.state = State.INIT;
    }

    @Override
    public List<Var> getResultVars() {
        return resultVars;
    }

    @Override
    protected Binding moveToNext() {
        try {
            return computeNextActual();
        } catch (Exception | IOException e) {
            throw new ResultSetException(e.getMessage(), e);
        }
    }

    protected void onUnexpectedJsonElement() throws IOException {
        if (errorHandler != null) {
            errorHandler.warning("Encountered unexpected json element at path " + reader.getPath(), -1, -1);
        }
        reader.skipValue();
    }


    protected Binding computeNextActual() throws IOException {
        Binding result;
        outer: while (true) {
            switch (state) {
            case INIT:
                reader.beginObject();
                state = State.ROOT;
                continue outer;

            case ROOT:
                while (reader.hasNext()) {
                    String topLevelName = reader.nextName();
                    switch (topLevelName) {
                    case kHead:
                        ++kHeadCount;
                        resultVars = parseHead();
                        break;
                    case kResults:
                        ++kResultsCount;
                        reader.beginObject();
                        state = State.RESULTS;
                        continue outer;
                    case kBoolean:
                        ++kBooleanCount;
                        askResult = reader.nextBoolean();
                        continue outer;
                    default:
                        onUnexpectedJsonElement();
                        break;
                    }
                }
                reader.endObject();
                state = State.DONE;
                continue outer;

            case RESULTS:
                while (reader.hasNext()) {
                    String elt = reader.nextName();
                    switch (elt) {
                    case kBindings:
                        reader.beginArray();
                        state = State.BINDINGS;
                        continue outer;
                    default:
                        onUnexpectedJsonElement();
                        break;
                    }
                }
                reader.endObject();
                state = State.ROOT;
                break;

            case BINDINGS:
                while (reader.hasNext()) {
                    result = parseBinding(gson, reader, labelMap, onUnknownRdfTermType);
                    ++rowNumber;
                    break outer;
                }
                reader.endArray();
                state = State.RESULTS;
                break;

            case DONE:
                result = null; // endOfData();
                if (enableValidation) {
                    validateCompleted(this);
                }

                break outer;
            }
        }

        if (enableValidation) {
            validate(this);
        }

        return result;
    }

    protected List<Var> parseHead() throws IOException {
        List<Var> result = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String n = reader.nextName();
            switch (n) {
            case kVars:
                List<String> varNames = gson.fromJson(reader, new TypeToken<List<String>>() {}.getType());
                result = Var.varList(varNames);
                break;
            default:
                onUnexpectedJsonElement();
                break;
            }
        }
        reader.endObject();
        return result;
    }

    public Boolean getAskResult() {
        return askResult;
    }

    @Override
    public long getRowNumber() {
        return rowNumber;
    }

    public int getKHeadCount() {
        return kHeadCount;
    }

    public int getKBooleanCount() {
        return kBooleanCount;
    }

    public int getKResultsCount() {
        return kResultsCount;
    }

    /** Check the current state of a streaming json row set for inconsistencies */
    public static void validate(RowSetJSONStreaming rs) {
        if (rs.getKBooleanCount() > 0 && rs.getKResultsCount() > 0) {
            throw new ResultSetException("Encountered bindings as well as boolean result");
        }
    }

    /** Check a completed streaming json row set for inconsistencies.
     *  Specifically checks for whether there was a header attribute. */
    public static void validateCompleted(RowSetJSONStreaming rs) {
        if (rs.getKHeadCount() == 0) {
            throw new ResultSetException(String.format("Mandory key '%s' not seen", kHead));
        }

        if (rs.getKResultsCount() == 0 && rs.getKBooleanCount() == 0) {
            throw new ResultSetException(String.format("Either '%s' or '%s' is mandatory; neither seen", kResults, kBoolean));
        }
    }

    @Override
    public void closeIterator() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new ResultSetException("IOException on closing the underlying stream", e);
        }
    }

    @Override
    protected boolean hasMore() {
        return true;
    }


    public static Node parseOneTerm(JsonElement jsonElt, LabelToNode labelMap, Function<JsonObject, Node> onUnknownRdfTermType) {

        if (jsonElt == null) {
            throw new ResultSetException("Expected a json object for an RDF term but got null");
        }

        JsonObject term = jsonElt.getAsJsonObject();

        Node result;
        String type = expectNonNull(term, kType).getAsString();
        JsonElement valueJson = expectNonNull(term, kValue);
        String valueStr;
        switch (type) {
        case kUri:
            valueStr = valueJson.getAsString();
            result = NodeFactory.createURI(valueStr);
            break;
        case kTypedLiteral: /* Legacy */
        case kLiteral:
            valueStr = valueJson.getAsString();
            JsonElement langJson = term.get(kXmlLang);
            JsonElement dtJson = term.get(kDatatype);

            String lang = langJson == null ? null : langJson.getAsString();
            String dtStr = dtJson == null ? null : dtJson.getAsString();

            if ( lang != null ) {
                // Strictly, xml:lang=... and datatype=rdf:langString is wrong
                // (the datatype should be absent)
                // The RDF specs recommend omitting the datatype. They did
                // however come after the SPARQL 1.1 docs
                // it's more of a "SHOULD" than a "MUST".
                // datatype=xsd:string is also unnecessary.
                if ( dtStr != null && !dtStr.equals(RDF.dtLangString.getURI()) ) {
                    // Must agree.
                    throw new ResultSetException("Both language and datatype defined, datatype is not rdf:langString:\n" + term);
                }
            }

            result = NodeFactoryExtra.createLiteralNode(valueStr, lang, dtStr);
            break;
        case kBnode:
            valueStr = valueJson.getAsString();
            result = labelMap.get(null, valueStr);
            break;
        case kStatement:
        case kTriple:
            JsonObject tripleJson = valueJson.getAsJsonObject();

            JsonElement js = expectOneKey(tripleJson, kSubject, kSubjectAlt);
            JsonElement jp = expectOneKey(tripleJson, kPredicate, kProperty, kPredicateAlt);
            JsonElement jo = expectOneKey(tripleJson, kObject, kObjectAlt);

            Node s = parseOneTerm(js, labelMap, onUnknownRdfTermType);
            Node p = parseOneTerm(jp, labelMap, onUnknownRdfTermType);
            Node o = parseOneTerm(jo, labelMap, onUnknownRdfTermType);

            result = NodeFactory.createTripleNode(s, p, o);
            break;
        default:
            if (onUnknownRdfTermType != null) {
                result = onUnknownRdfTermType.apply(term);
                if (result == null) {
                    throw new ResultSetException("Custom handler returned null for unknown rdf term type '" + type + "'");
                }
            } else {
                throw new ResultSetException("Object key not recognized as valid for an RDF term: " + term);
            }
            break;
        }

        return result;
    }

    public static JsonElement expectNonNull(JsonObject json, String key) {
        JsonElement v = json.get(key);
        if ( v == null )
            throw new ResultSetException("Unexpected null value for key: " + key);

        return v;
    }


    public static JsonElement expectOneKey(JsonObject json, String ...keys) {
        JsonElement result = null;

        for (String key : keys) {
            JsonElement tmp = json.get(key);

            if (tmp != null) {
                if (result != null) {
                    throw new ResultSetException("More than one key out of " + Arrays.asList(keys));
                }

                result = tmp;
            }
        }

        if (result == null) {
            throw new ResultSetException("One or more of the required keys " + Arrays.asList(keys) + " was not found");
        }

        return result;
    }

    public static Binding parseBinding(
            Gson gson, JsonReader reader, LabelToNode labelMap,
            Function<JsonObject, Node> onUnknownRdfTermType) throws IOException {
        JsonObject obj = gson.fromJson(reader, JsonObject.class);

        BindingBuilder bb = BindingFactory.builder();

        for (Entry<String, JsonElement> e : obj.entrySet()) {
            Var v = Var.alloc(e.getKey());
            JsonElement nodeElt = e.getValue();

            Node node = parseOneTerm(nodeElt, labelMap, onUnknownRdfTermType);
            bb.add(v, node);
        }

        return bb.build();
    }

}
