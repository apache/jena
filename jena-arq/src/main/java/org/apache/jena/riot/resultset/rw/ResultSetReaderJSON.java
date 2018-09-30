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

package org.apache.jena.riot.resultset.rw;

import static org.apache.jena.riot.resultset.rw.JSONResultsKW.*;

import java.io.InputStream;
import java.util.*;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetReader;
import org.apache.jena.riot.resultset.ResultSetReaderFactory;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.ResultSetStream;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.resultset.SPARQLResult;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.vocabulary.RDF;

/** Read JSON format SPARQL Results.
 * <p>
 * <a href="https://www.w3.org/TR/sparql11-results-json/">SPARQL 1.1 Query Results JSON Format</a>
 */
public class ResultSetReaderJSON implements ResultSetReader {
    
    public static final ResultSetReaderFactory factory = lang -> {
        if (!Objects.equals(lang, ResultSetLang.SPARQLResultSetJSON ) )
            throw new ResultSetException("ResultSet for JSON asked for a "+lang); 
        return new ResultSetReaderJSON(); 
    };
    
    private ResultSetReaderJSON() {}
    
    // TODO Streaming version of JSON Result set processing

    @Override
    public SPARQLResult readAny(InputStream in, Context context) {
        return process(in, null, context);
    }

    static private SPARQLResult process(InputStream in, Model model, Context context) {
        if ( context == null )
            context = ARQ.getContext();
        RS_JSON exec = new RS_JSON(context);
        exec.parse(in);
        if ( model == null )
            model = GraphFactory.makeJenaDefaultModel();
        if ( exec.rows != null ) {
            QueryIterator qIter = new QueryIterPlainWrapper(exec.rows.iterator());
            ResultSet rs = new ResultSetStream(Var.varNames(exec.vars), model, qIter);
            return new SPARQLResult(rs); 
        } else
            return new SPARQLResult(exec.booleanResult);
    }

    /** 
     * Parse a result set - whether rows (SELECT) or a boolean results (ASK).
     * This object is one-time use and exists to carry the results as they are built-up.
     */
    static class RS_JSON {
        final Context   context;
        Boolean         booleanResult = null; // Valid if rows is null.
        List<Binding>   rows          = null;
        List<Var>       vars          = null;
        final LabelToNode labelMap;

        RS_JSON(Context context) {
            this.context = context;
            boolean inputGraphBNodeLabels = (context != null) && context.isTrue(ARQ.inputGraphBNodeLabels);
            this.labelMap = inputGraphBNodeLabels
                ? SyntaxLabels.createLabelToNodeAsGiven()
                : SyntaxLabels.createLabelToNode();
            this.rows = null ;
        }

        private void parse(InputStream in) {
            JsonObject obj = JSON.parse(in);

            // Boolean?
            if ( obj.hasKey(kBoolean) ) {
                checkContains(obj, true, true, kHead, kBoolean);
                booleanResult = obj.get(kBoolean).getAsBoolean().value();
                rows = null;
                return;
            }

            // ResultSet.
            rows = new ArrayList<>(1000);

            checkContains(obj, true, true, kHead, kResults);

            // process head
            if ( !obj.get(kHead).isObject() )
                throw new ResultSetException("Key 'head' must have a JSON object as value: found: " + obj.get(kHead));
            JsonObject head = obj.get(kHead).getAsObject();

            // ---- Head
            // -- Link - array.
            if ( head.hasKey(kLink) ) {
                List<String> links = new ArrayList<>();

                if ( head.get(kLink).isString() ) {
                    Log.warn(this, "Link field is a string, should be an array of strings");
                    links.add(head.get(kLink).getAsString().value());
                } else {
                    if ( !head.get(kLink).isArray() )
                        throw new ResultSetException("Key 'link' must have be an array: found: " + obj.get(kLink));

                    for ( JsonValue v : head.get(kLink).getAsArray() ) {
                        if ( !v.isString() )
                            throw new ResultSetException("Key 'link' must have be an array of strings: found: " + v);
                        links.add(v.getAsString().value());
                    }
                }
            }
            // -- Vars
            vars = parseVars(head);

            // ---- Results
            JsonObject results = obj.get(kResults).getAsObject();
            if ( !results.get(kBindings).isArray() )
                throw new ResultSetException("'bindings' must be an array");
            JsonArray array = results.get(kBindings).getAsArray();
            Iterator<JsonValue> iter = array.iterator();

            for ( ; iter.hasNext() ; ) {
                BindingMap b = BindingFactory.create();
                JsonValue v = iter.next();
                if ( !v.isObject() )
                    throw new ResultSetException("Entry in 'bindings' array must be an object {}");
                JsonObject x = v.getAsObject();
                Set<String> varNames = x.keys();
                for ( String vn : varNames ) {
                    // if ( ! vars.contains(vn) ) {}
                    JsonValue vt = x.get(vn);
                    if ( !vt.isObject() )
                        throw new ResultSetException("Binding for variable '" + vn + "' is not a JSON object: " + vt);
                    Node n = parseOneTerm(vt.getAsObject(), labelMap);
                    b.add(Var.alloc(vn), n);
                }
                rows.add(b);
            }
        }

        private static List<Var> parseVars(JsonObject obj) {
            if ( !obj.get(kVars).isArray() )
                throw new ResultSetException("Key 'vars' must be a JSON array");
            JsonArray a = obj.get(kVars).getAsArray();
            Iterator<JsonValue> iter = a.iterator();
            List<Var> vars = new ArrayList<>();
            for ( ; iter.hasNext() ; ) {
                JsonValue v = iter.next();
                if ( !v.isString() )
                    throw new ResultSetException("Entries in vars array must be strings");
                Var var = Var.alloc(v.getAsString().value());
                vars.add(var);
            }
            return vars;
        }

        private static Node parseOneTerm(JsonObject term, LabelToNode labelMap) {
            checkContains(term, false, false, kType, kValue, kXmlLang, kDatatype);

            String type = stringOrNull(term, kType);
            String v = stringOrNull(term, kValue);

            if ( kUri.equals(type) ) {
                checkContains(term, false, true, kType, kValue);
                String uri = v;
                Node n = NodeFactory.createURI(v);
                return n;
            }

            if ( kLiteral.equals(type) || kTypedLiteral.equals(type) ) {
                String lang = stringOrNull(term, kXmlLang);
                String dtStr = stringOrNull(term, kDatatype);
                if ( lang != null ) {
                    // Strictly, xml:lang=... and datatype=rdf:langString is wrong (the datatype should be absent)
                    // The RDF specs recommend omitting the datatype. They did however come after the SPARQL 1.1 docs
                    // it's more of a "SHOULD" than a "MUST".
                    // datatype=xsd:string is also unnecessary.
                    if ( dtStr != null && ! dtStr.equals(RDF.dtLangString.getURI() ) ) {
                        // Must agree.
                        throw new ResultSetException("Both language and datatype defined, datatype is not rdf:langString:\n" + term);
                    }
                }
                RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(dtStr);
                return NodeFactory.createLiteral(v, lang, dt);
            }

            if ( kBnode.equals(type) )
                return labelMap.get(null, v);

            throw new ResultSetException("Object key not recognized as valid for an RDF term: " + term);
        }

        private static String stringOrNull(JsonObject obj, String key) {
            JsonValue v = obj.get(key);
            if ( v == null )
                return null;
            if ( !v.isString() )
                throw new ResultSetException("Not a string: key: " + key);
            return v.getAsString().value();
        }

        private static void checkContains(JsonObject term, boolean allowUndefinedKeys, boolean requireAllExpectedKeys, String... keys) {
            List<String> expectedKeys = Arrays.asList(keys);
            Set<String> declared = new HashSet<>();
            for ( String k : term.keys() ) {
                if ( !expectedKeys.contains(k) && !allowUndefinedKeys )
                    throw new ResultSetException("Expected only object keys " + Arrays.asList(keys) + " but encountered '" + k + "'");
                if ( expectedKeys.contains(k) )
                    declared.add(k);
            }

            if ( requireAllExpectedKeys && declared.size() < expectedKeys.size() )
                throw new ResultSetException("One or more of the required keys " + expectedKeys + " was not found");
        }
    }
}
