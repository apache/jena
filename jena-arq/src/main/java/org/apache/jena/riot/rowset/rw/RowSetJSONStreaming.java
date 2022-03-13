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

import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kBnode;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kBoolean;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kDatatype;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kHead;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kLiteral;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kObject;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kObjectAlt;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kPredicate;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kPredicateAlt;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kProperty;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kResults;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kStatement;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kSubject;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kSubjectAlt;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kTriple;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kType;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kTypedLiteral;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kUri;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kValue;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kVars;
import static org.apache.jena.riot.rowset.rw.JSONResultsKW.kXmlLang;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.jena.atlas.data.DataBag;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.atlas.iterator.IteratorSlotted;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.rowset.rw.IteratorRsJSON.RsJsonEltEncoder;
import org.apache.jena.riot.system.ErrorEvent;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlers;
import org.apache.jena.riot.system.Severity;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetBuffered;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.vocabulary.RDF;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * Streaming RowSet implementation for application/sparql-results+json
 * The {@link #getResultVars()} will return null as long as the header has not
 * been consumed from the underlying stream.
 *
 * Use {@link RowSetBuffered} to modify the behavior such that {@link #getResultVars()}
 * immediately consumes the underlying stream until the header is read,
 * thereby buffering any encountered bindings for replay.
 */
public class RowSetJSONStreaming<E>
    extends IteratorSlotted<Binding>
    implements RowSet
{
    /* Construction -------------------------------------------------------- */

    public static RowSetBuffered<RowSetJSONStreaming<?>> createBuffered(
            InputStream in, LabelToNode labelMap,
            Supplier<DataBag<Binding>> bufferFactory, ValidationSettings validationSettings,
            ErrorHandler errorHandler) {
        return new RowSetBuffered<>(createUnbuffered(in, labelMap, validationSettings, errorHandler), bufferFactory);
    }

    public static RowSetJSONStreaming<?> createUnbuffered(InputStream in, LabelToNode labelMap, ValidationSettings validationSettings,
            ErrorHandler errorHandler) {

        Gson gson = new Gson();
        JsonReader reader = gson.newJsonReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        // Set up handling of unexpected json elements
        UnexpectedJsonEltHandler unexpectedJsonHandler = (_gson, _reader) -> {
            ErrorHandlers.relay(errorHandler, validationSettings.unexpectedJsonElementSeverity, () ->
                    new ErrorEvent("Encountered unexpected json element at path " + reader.getPath()));
            reader.skipValue();
            return null;
        };

        // Experiments with an decoder/encoder that returned Bindings directly without
        // wrapping them as RsJsonEltDft did not show a significant performance difference
        // that would justify supporting alternative encoder/decoder pairs.
        RsJsonEltEncoder<RsJsonEltDft> eltEncoder = new RsJsonEltEncoderDft(labelMap, null, unexpectedJsonHandler);
        RsJsonEltDecoder<RsJsonEltDft> eltDecoder = RsJsonEltDecoderDft.INSTANCE;
        IteratorRsJSON<RsJsonEltDft> eltIt = new IteratorRsJSON<>(gson, reader, eltEncoder);

        // Wrap this up as a validating RowSet
        RowSetJSONStreaming<?> result = new RowSetJSONStreaming<>(eltIt, eltDecoder, 0l, validationSettings, errorHandler);
        return result;
    }

    /* Domain adapter / bridge for IteratorRsJson  ------------------------- */

    public enum RsJsonEltType {
        UNKNOWN,
        HEAD,
        BOOLEAN,
        RESULTS, // The 'results' event exists as a marker for validation
        BINDING
    }

    public static class RsJsonEltDft {
        protected RsJsonEltType type;
        protected List<Var> head;
        protected Boolean askResult;
        protected Binding binding;

        // Nullable json element instance of the underlying json API
        protected Object unknownJsonElement;

        public RsJsonEltDft(RsJsonEltType type) {
            this.type = type;
        }

        public RsJsonEltDft(List<Var> head) {
            this.type = RsJsonEltType.HEAD;
            this.head = head;
        }

        public RsJsonEltDft(Boolean askResult) {
            this.type = RsJsonEltType.BOOLEAN;
            this.askResult = askResult;
        }

        public RsJsonEltDft(Binding binding) {
            this.type = RsJsonEltType.BINDING;
            this.binding = binding;
        }

        public RsJsonEltDft(Object jsonElement) {
            this.type = RsJsonEltType.UNKNOWN;
            this.unknownJsonElement = jsonElement;
        }

        public RsJsonEltType getType() {
            return type;
        }

        public List<Var> getHead() {
            return head;
        }

        public Boolean getAskResult() {
            return askResult;
        }

        public Binding getBinding() {
            return binding;
        }

        public Object getUnknownJsonElement() {
            return unknownJsonElement;
        }

        @Override
        public String toString() {
            return "RsJsonEltDft [type=" + type + ", head=" + head + ", askResult=" + askResult + ", binding=" + binding
                    + ", unknownJsonElement=" + unknownJsonElement + "]";
        }
    }

    /* Core implementation ------------------------------------------------- */

    protected IteratorCloseable<E> eltIterator;
    protected RsJsonEltDecoder<? super E> eltDecoder;
    protected long rowNumber;

    protected ValidationSettings validationSettings;
    protected ErrorHandler errorHandler;

    protected TentativeValue<List<Var>> resultVars = null;
    protected TentativeValue<Boolean> askResult = null;

    protected int kHeadCount = 0;
    protected int kResultsCount = 0;
    protected int kBooleanCount = 0;
    protected int unknownJsonCount = 0;

    public RowSetJSONStreaming(
            IteratorCloseable<E> rsJsonIterator,
            RsJsonEltDecoder<? super E> eltDecoder,
            long rowNumber,
            ValidationSettings validationSettings, ErrorHandler errorHandler) {
        super();

        this.eltIterator = rsJsonIterator;
        this.eltDecoder = eltDecoder;
        this.rowNumber = rowNumber;

        this.validationSettings = validationSettings;
        this.errorHandler = errorHandler;

        this.resultVars = new TentativeValue<>();
        this.askResult = new TentativeValue<>();
    }

    @Override
    protected Binding moveToNext() {
        try {
            Binding result = computeNextActual();
            return result;
        } catch (Throwable e) {
            // Re-wrap any exception
            throw new ResultSetException(e.getMessage(), e);
        }
    }

    protected Binding computeNextActual() throws IOException {
        boolean updateAccepted;
        Binding result = null;

        E elt = null;
        RsJsonEltType type = null;

        outer: while (eltIterator.hasNext()) {
            elt = eltIterator.next();
            type = eltDecoder.getType(elt);

            switch (type) {
            case HEAD:
                ++kHeadCount;
                List<Var> rsv = eltDecoder.getAsHead(elt);
                updateAccepted = resultVars.updateValue(rsv);
                if (!updateAccepted) {
                    ErrorHandlers.relay(errorHandler, validationSettings.getInvalidatedHeadSeverity(),
                            new ErrorEvent(String.format(
                                ". Prior value for headVars was %s but got superseded with %s", resultVars.getValue(), rsv)));
                }
                validate(this, errorHandler, validationSettings);
                continue;

            case BOOLEAN:
                ++kBooleanCount;
                Boolean b = eltDecoder.getAsBoolean(elt);
                updateAccepted = askResult.updateValue(b);
                if (!updateAccepted) {
                    ErrorHandlers.relay(errorHandler, validationSettings.getInvalidatedHeadSeverity(),
                            new ErrorEvent(String.format(
                                ". Prior value for boolean result was %s but got supersesed %s", askResult.getValue(), b)));
                }
                validate(this, errorHandler, validationSettings);
                continue;

            case RESULTS:
                ++kResultsCount;
                validate(this, errorHandler, validationSettings);
                continue;

            case BINDING:
                ++rowNumber;
                result = eltDecoder.getAsBinding(elt);
                break outer;

            case UNKNOWN:
                ++unknownJsonCount;
                continue;
            }
        }

        // Once we return any tentative value becomes final
        resultVars.makeFinal();
        askResult.makeFinal();

        if (result == null && !eltIterator.hasNext()) {
            validateCompleted(this, errorHandler, validationSettings);
        }

        return result;
    }

    @Override
    protected boolean hasMore() {
        return true;
    }

    @Override
    protected void closeIterator() {
        eltIterator.close();
    }


    /* Statistics ---------------------------------------------------------- */

    @Override
    public long getRowNumber() {
        return rowNumber;
    }

    boolean hasResultVars() {
        return resultVars.hasValue();
    }

    @Override
    public List<Var> getResultVars() {
        return resultVars.getValue();
    }

    public boolean hasAskResult() {
        return askResult.hasValue();
    }

    public Boolean getAskResult() {
        return askResult.getValue();
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

    public int getUnknownJsonCount() {
        return unknownJsonCount;
    }

    /* Parsing (gson-based) ------------------------------------------------ */

    /** Parse the vars element from head - may return null */
    public static List<Var> parseHeadVars(Gson gson, JsonReader reader) throws IOException {
        List<Var> result = null;
        Type stringListType = new TypeToken<List<String>>() {}.getType();
        JsonObject headJson = gson.fromJson(reader, JsonObject.class);
        JsonElement varsJson = headJson.get(kVars);
        if (varsJson != null) {
            List<String> varNames = gson.fromJson(varsJson, stringListType);
            result = Var.varList(varNames);
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

    /* Validation ---------------------------------------------------------- */

    /** Runtime validation of the current state of a streaming json row set */
    public static void validate(RowSetJSONStreaming<?> rs, ErrorHandler errorHandler, ValidationSettings settings) {
        if (rs.hasAskResult() && rs.getKResultsCount() > 0) {
            ErrorHandlers.relay(errorHandler, settings.getMixedResultsSeverity(), () ->
                new ErrorEvent("Encountered bindings as well as boolean result"));
        }

        if (rs.getKResultsCount() > 1) {
            ErrorHandlers.relay(errorHandler, settings.getInvalidatedResultsSeverity(), () ->
                new ErrorEvent("Multiple 'results' keys encountered"));
        }
    }

    /** Check a completed streaming json row set for inconsistencies.
     *  Specifically checks for missing result value and missing head */
    public static void validateCompleted(RowSetJSONStreaming<?> rs, ErrorHandler errorHandler, ValidationSettings settings) {
        // Missing result (neither 'results' nor 'boolean' seen)
        if (rs.getKResultsCount() == 0 && rs.getKBooleanCount() == 0) {
            ErrorHandlers.relay(errorHandler, settings.getEmptyJsonSeverity(),
                new ErrorEvent(String.format("Either '%s' or '%s' is mandatory; neither seen", kResults, kBoolean)));
        }

        // Missing head
        if (rs.getKHeadCount() == 0) {
            ErrorHandlers.relay(errorHandler, settings.getMissingHeadSeverity(),
                new ErrorEvent(String.format("Mandory key '%s' not seen", kHead)));
        }
    }

    /** Validation settings class */
    public static class ValidationSettings implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * What to do if the JSON is effectively 'empty', i.e. if neither
         * the head nor the results key were present.
         * Unexpected elements are captured by onUnexpectedJsonElement.
         * e.g. returned older version of virtuoso open source
         * Mitigation is to assume an empty set of bindings.
         */
        protected Severity emptyJsonSeverity = Severity.ERROR;

        /** What to do if no head was encountered. We may have already
         * optimistically streamed all the bindings in anticipation of an
         * eventual head. */
        protected Severity missingHeadSeverity = Severity.ERROR;

        /** What to do if there is a repeated 'results' key
         * At this stage we have already optimisticaly streamed results which
         * under JSON semantics would have been superseded by this newly
         * encountered key. */
        protected Severity invalidatedResultsSeverity = Severity.ERROR;

        /** What to do if there is a repeated 'head' <b>whole value does not match the
         * prior value</b>. Repeated heads with the same value are valid.
         * Any possibly prior reported head would have been superseded by this newly
         * encountered key.
         * Should parsing continue then only the first encountered value will remain active.
         */
        protected Severity invalidatedHeadSeverity = Severity.FATAL;

        /**
         * What to do if the JSON contains both a boolean result and bindings
         * Mitigation is to assume bindings and ignore the boolean result
         */
        protected Severity mixedResultsSeverity = Severity.FATAL;

        /** What to do if we encounter an unexpected JSON key */
        protected Severity unexpectedJsonElementSeverity = Severity.IGNORE;

        public Severity getEmptyJsonSeverity() {
            return emptyJsonSeverity;
        }

        public void setEmptyJsonSeverity(Severity severity) {
            this.emptyJsonSeverity = severity;
        }

        public Severity getInvalidatedHeadSeverity() {
            return invalidatedHeadSeverity;
        }

        public void setInvalidatedHeadSeverity(Severity severity) {
            this.invalidatedHeadSeverity = severity;
        }

        public Severity getInvalidatedResultsSeverity() {
            return invalidatedResultsSeverity;
        }

        public void setInvalidatedResultsSeverity(Severity severity) {
            this.invalidatedResultsSeverity = severity;
        }

        public Severity getMissingHeadSeverity() {
            return missingHeadSeverity;
        }

        public void setMissingHeadSeverity(Severity severity) {
            this.missingHeadSeverity = severity;
        }

        public Severity getMixedResultsSeverity() {
            return mixedResultsSeverity;
        }

        public void setMixedResultsSeverity(Severity severity) {
            this.mixedResultsSeverity = severity;
        }

        public Severity getUnexpectedJsonElementSeverity() {
            return unexpectedJsonElementSeverity;
        }

        public void setUnexpectedJsonElementSeverity(Severity severity) {
            this.unexpectedJsonElementSeverity = severity;
        }
    }

    /* Internal / Utility -------------------------------------------------- */

    /** Interface for optionally emitting the json elements
     * Typically this only calls jsonReader.skipValue() as to not spend
     * efforts on parsing */
    @FunctionalInterface
    public interface UnexpectedJsonEltHandler {
        JsonElement apply(Gson gson, JsonReader reader) throws IOException;
    }

    /** A decoder can extract an instance of E's {@link RsJsonEltType}
     *  an provides methods to extract the corresponding value.
     *  It decouples {@link RowSetJSONStreaming} from gson.
     */
    public static interface RsJsonEltDecoder<E> {
        RsJsonEltType getType(E elt);
        Binding getAsBinding(E elt);
        List<Var> getAsHead(E elt);
        Boolean getAsBoolean(E elt);
    }

    /** Decoder for extraction of rs-json domain objects from {@link RsJsonEltDft} */
    public static class RsJsonEltDecoderDft
        implements RsJsonEltDecoder<RsJsonEltDft> {

        public static final RsJsonEltDecoderDft INSTANCE = new RsJsonEltDecoderDft();

        @Override public RsJsonEltType getType(RsJsonEltDft elt) { return elt.getType(); }
        @Override public Binding getAsBinding(RsJsonEltDft elt)  { return elt.getBinding(); }
        @Override public List<Var> getAsHead(RsJsonEltDft elt)   { return  elt.getHead(); }
        @Override public Boolean getAsBoolean(RsJsonEltDft elt)  { return elt.getAskResult(); }
    }

    /**
     * Json encoder that wraps each message uniformly as a {@link RsJsonEltDft}.
     */
    public static class RsJsonEltEncoderDft
        implements RsJsonEltEncoder<RsJsonEltDft> {

        protected LabelToNode labelMap;
        protected Function<JsonObject, Node> unknownRdfTermTypeHandler;
        protected UnexpectedJsonEltHandler unexpectedJsonHandler;

        public RsJsonEltEncoderDft(LabelToNode labelMap,
                Function<JsonObject, Node> unknownRdfTermTypeHandler,
                UnexpectedJsonEltHandler unexpectedJsonHandler) {
            super();
            this.labelMap = labelMap;
            this.unknownRdfTermTypeHandler = unknownRdfTermTypeHandler;
            this.unexpectedJsonHandler = unexpectedJsonHandler;
        }

        @Override
        public RsJsonEltDft newHeadElt(Gson gson, JsonReader reader) throws IOException {
            List<Var> vars = parseHeadVars(gson, reader);
            return new RsJsonEltDft(vars);
        }

        @Override
        public RsJsonEltDft newBooleanElt(Gson gson, JsonReader reader) throws IOException {
            Boolean b = reader.nextBoolean();
            return new RsJsonEltDft(b);
        }

        @Override
        public RsJsonEltDft newBindingElt(Gson gson, JsonReader reader) throws IOException {
            Binding binding = parseBinding(gson, reader, labelMap, unknownRdfTermTypeHandler);
            return new RsJsonEltDft(binding);
        }

        @Override
        public RsJsonEltDft newResultsElt(Gson gson, JsonReader reader) throws IOException {
            return new RsJsonEltDft(RsJsonEltType.RESULTS);
        }

        @Override
        public RsJsonEltDft newUnknownElt(Gson gson, JsonReader reader) throws IOException {
            JsonElement jsonElement = unexpectedJsonHandler == null
                    ? null
                    : unexpectedJsonHandler.apply(gson, reader);
            return new RsJsonEltDft(jsonElement);
        }
    }

    /**
     * Internal helper class used for valiaditon.
     * It can hold tentative values and make them final.
     * Once a value is final then further updates are rejected.
     * This is used to iterate over sequences of repeated json keys ('head', 'boolean')
     * and only have the last value take effect.
     *
     * We don't want to validate for oddities in the json document - we just want to catch
     * the cases when our optimistic reports turned out to be wrong.
     * So even if there are e.g. repeated boolean fields then as long as we are sure that
     * we report the last of those we are fine.
     */
    protected static class TentativeValue<T> {
        T value;
        boolean isValueFinal = false;
        boolean isTentative = false;

        // Return true if update is valid
        // As long as makeFinal is not called the value can be updated
        boolean updateValue(T arg) {
            boolean result;
            if (!isValueFinal) {
                value = arg;
                isTentative = true;
                result = true;
            } else {
                result = Objects.equals(value, arg);
            }
            return result;
        }

        // Finalize a tentative value - otherwise do nothing
        void makeFinal() {
            if (isTentative) {
                isValueFinal = true;
            }
        }

        T getValue() {
            return value;
        }

        /** Whether updateValue was called at all */
        boolean hasValue() {
            return isTentative;
        }

        /** Whether the value has been finalized */
        boolean isValueFinal() {
            return isValueFinal;
        }

        @Override
        public String toString() {
            return "Holder [value=" + value + ", isValueSet=" + isValueFinal + "]";
        }
    }
}
