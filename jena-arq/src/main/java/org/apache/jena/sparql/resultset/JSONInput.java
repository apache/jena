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

package org.apache.jena.sparql.resultset;

import java.io.InputStream;

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
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.rw.ResultsReader;

/**
 * @deprecated Use {@code ResultSetMgr.read} 
 */
@Deprecated
public class JSONInput extends SPARQLResult {
    public static ResultSet fromJSON(InputStream input) {
        return 
            ResultsReader.create()
                .lang(ResultSetLang.SPARQLResultSetJSON)
                .read(input);
    }

    public static boolean booleanFromJSON(InputStream input) {
        return make(input).getBooleanResult();
    }

    public static SPARQLResult make(InputStream input) {
        return
            ResultsReader.create()
                .lang(ResultSetLang.SPARQLResultSetJSON)
                .build()
                .readAny(input);
    }

    public static SPARQLResult make(InputStream input, Model model) {
        return new JSONInput().process(input, model);
    }

    private JSONInput() {}

    private Boolean       booleanResult = null; // Valid if rows is null.
    private List<Binding> rows          = null;
    private List<Var>     vars          = null;

    // TODO Streaming version of JSON Result set processing

    private SPARQLResult process(InputStream in, Model model) {
        parse(in);
        if ( model == null )
            model = GraphFactory.makeJenaDefaultModel();
        if ( rows != null ) {
            QueryIterator qIter = new QueryIterPlainWrapper(rows.iterator());
            ResultSet rs = new ResultSetStream(Var.varNames(vars), model, qIter);
            super.set(rs);
        } else
            super.set(booleanResult);
        return this;
    }

    private void parse(InputStream in) {
        JsonObject obj = JSON.parse(in);

        if ( obj.hasKey(kBoolean) ) {
            checkContains(obj, true, true, kHead, kBoolean);
            booleanResult = obj.get(kBoolean).getAsBoolean().value();
            rows = null;
            return;
        }

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
                Node n = parseOneTerm(vt.getAsObject());
                b.add(Var.alloc(vn), n);
            }
            rows.add(b);
        }
    }

    private List<Var> parseVars(JsonObject obj) {
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

    private LabelToNode labelMap = ARQ.getContext().isTrue(ARQ.inputGraphBNodeLabels) ?
        SyntaxLabels.createLabelToNodeAsGiven() :
        SyntaxLabels.createLabelToNode();

    private Node parseOneTerm(JsonObject term) {
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
            if ( lang != null && dtStr != null )
                throw new ResultSetException("Both language and datatype defined: " + term);
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
