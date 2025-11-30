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

package org.apache.jena.sparql.expr.urifunctions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Dispatch to SPARQL function by URI.
 */
public class SPARQLDispatch {

    static final String NS0 = SPARQLFuncOp.NS.substring(0, SPARQLFuncOp.NS.length()-1);

    public static NodeValue exec(String uri, List<NodeValue>args) {
        Objects.requireNonNull(uri);
        Objects.requireNonNull(args);

        NodeValue[] a = args.toArray(NodeValue[]::new);
        return exec(uri,a);
    }

    public static NodeValue exec(String uri, NodeValue...args) {
        if ( uri.startsWith("#") ) {
            // Short name.
            uri = SPARQLDispatch.NS0+uri;
        }

        Call call = getDispatchMap().get(uri);
        if ( call == null )
            throw new SPARQLEvalException("No such function: "+uri);
        return call.exec(args);
    }

    private static RuntimeException exception(String format, Object...args) {
        String msg = String.format(format, args);
        return new SPARQLEvalException(msg);
    }

    static void register(Map<String, Call> map, String uri, Function0 function) {
        Call call = args->{
            if ( args.length != 0 ) throw exception("%s: Expected zero arguments. Got %d", uri, args.length);
            return function.exec();
        };
        registerCall(map, uri, call);
    }

    static void register(Map<String, Call> map, String uri, Function1 function) {
        Call call = args->{
            if ( args.length != 1 ) throw exception("%s: Expected one argument. Got %d", uri, args.length);
            return function.exec(args[0]);
        };
        registerCall(map, uri, call);
    }

    static void register(Map<String, Call> map, String uri, Function2 function) {
        Call call = args->{
            if ( args.length != 2 ) throw exception("%s: Expected two arguments. Got %d", uri, args.length);
            return function.exec(args[0], args[1]);
        };
        registerCall(map, uri, call);
    }

    static void register(Map<String, Call> map, String uri, Function3 function) {
        Call call = args->{
            if ( args.length != 3 ) throw exception("%s: Expected three arguments. Got %d", uri, args.length);
            return function.exec(args[0], args[1], args[2]);
        };
        registerCall(map, uri, call);
    }

    static void register(Map<String, Call> map, String uri, Function4 function) {
        Call call = args->{
            if ( args.length != 3 ) throw exception("%s: Expected  arguments. Got %d", uri, args.length);
            return function.exec(args[0], args[1], args[2], args[3]);
        };
        registerCall(map, uri, call);
    }

    // Switch on arity
    static void register(Map<String, Call> map, String uri, Function1 function1, Function2 function2) {
        Call call = args->{
            if ( args.length == 1 )
                return function1.exec(args[0]);
            if ( args.length == 2 )
                return function2.exec(args[0], args[1]);
            throw exception("%s: Expected one or two arguments. Got %d", uri, args.length);
        };
        registerCall(map, uri, call);
    }

    // Switch on arity
    static void register(Map<String, Call> map, String uri, Function2 function2, Function3 function3) {
        Call call = args->{
            if ( args.length == 2 )
                return function2.exec(args[0], args[1]);
            if ( args.length == 3 )
                return function3.exec(args[0], args[1], args[2]);
            throw exception("%s: Expected two or three arguments. Got %d", uri, args.length);
        };
        registerCall(map, uri, call);
    }

    // Switch on arity
    static void register(Map<String, Call> map, String uri, Function3 function3, Function4 function4) {
        Call call = args->{
            if ( args.length == 3 )
                return function3.exec(args[0], args[1], args[2]);
            if ( args.length == 4 )
                return function4.exec(args[0], args[1], args[2], args[3]);
            throw exception("%s: Expected three or four arguments. Got %d", uri, args.length);
        };
        registerCall(map, uri, call);
    }

    // Arity N
    static void register(Map<String, Call> map, String uri, FunctionN function) {
        Call call = args->{
            return function.exec(args);
        };
        registerCall(map, uri, call);
    }

    static void registerCall(Map<String, Call> map, String localName, Call call) {
        String uri = SPARQLFuncOp.NS+localName;
        Call oldCall = map.put(uri, call);
        if ( oldCall != null )
            throw new InternalErrorException("Multiple registration of "+uri);
    }

    private static class LazyDispatchMap {
        private static final Map<String, Call> INITIALIZED_MAP = buildDispatchMap();
        private static Map<String, Call> dispatchMap() {
            return INITIALIZED_MAP;
        }
    }

    static Map<String, Call> getDispatchMap() {
        return LazyDispatchMap.dispatchMap();
    }

    static Map<String, Call> buildDispatchMap() {
        Map<String, Call> map = new HashMap<>();

        // Move out/rename to "FunctionDispatch"
        // Add ARQ specials.

        register(map, "plus", SPARQLFuncOp::sparql_add );
        register(map, "add", SPARQLFuncOp::sparql_add );            // Alt name.
        register(map, "subtract", SPARQLFuncOp::sparql_subtract );
        register(map, "minus", SPARQLFuncOp::sparql_subtract );     // Alt name.
        register(map, "multiply", SPARQLFuncOp::sparql_multiply );
        register(map, "divide", SPARQLFuncOp::sparql_divide );

        register(map, "equals", SPARQLFuncOp::sparql_equals );
        register(map, "not-equals", SPARQLFuncOp::sparql_not_equals );
        register(map, "greaterThan", SPARQLFuncOp::sparql_greaterThan );
        register(map, "greaterThanOrEqual", SPARQLFuncOp::sparql_greaterThanOrEqual );
        register(map, "lessThan", SPARQLFuncOp::sparql_lessThan );
        register(map, "lessThanOrEqual", SPARQLFuncOp::sparql_lessThanOrEqual );

        register(map, "and", SPARQLFuncOp::sparql_function_and );
        register(map, "or", SPARQLFuncOp::sparql_function_or );
        register(map, "not", SPARQLFuncOp::sparql_function_not );

        register(map, "unary-minus", SPARQLFuncOp::sparql_unary_minus );
        register(map, "unary-plus", SPARQLFuncOp::sparql_unary_plus );

        register(map, "abs", SPARQLFuncOp::sparql_abs);
        register(map, "bnode", SPARQLFuncOp::sparql_bnode);
        register(map, "ceil", SPARQLFuncOp::sparql_ceil);

        // List arity
        register(map, "concat", (NodeValue[] args)->SPARQLFuncOp.sparql_concat(args));
        register(map, "contains", SPARQLFuncOp::sparql_contains);
        register(map, "datatype", SPARQLFuncOp::sparql_datatype);

        register(map, "encode", SPARQLFuncOp::sparql_encode);
        register(map, "floor", SPARQLFuncOp::sparql_floor);
        register(map, "haslang", SPARQLFuncOp::sparql_haslang);
        register(map, "haslangdir", SPARQLFuncOp::sparql_haslangdir);

        // Arity 1/2
        //register(map, "iri", x->SPARQLFuncOp.sparql_iri(x), (x,b)->SPARQLFuncOp.arq_iri(x,b));
        register(map, "iri", SPARQLFuncOp::sparql_iri, SPARQLFuncOp::arq_iri);
        //register(map, "uri", x->SPARQLFuncOp.sparql_uri(x), (x,b)->SPARQLFuncOp.arq_uri(x,b));
        register(map, "uri", SPARQLFuncOp::sparql_uri, SPARQLFuncOp::arq_uri);

        register(map, "isBlank", SPARQLFuncOp::sparql_isBlank);
        register(map, "isLiteral", SPARQLFuncOp::sparql_isLiteral);
        register(map, "isNumeric", SPARQLFuncOp::sparql_isNumeric);
        register(map, "isIRI", SPARQLFuncOp::sparql_isIRI);
        register(map, "isURI", SPARQLFuncOp::sparql_isURI);
        register(map, "lang", SPARQLFuncOp::sparql_lang);
        register(map, "langMatches", SPARQLFuncOp::sparql_langMatches);
        register(map, "langdir", SPARQLFuncOp::sparql_langdir);
        register(map, "lcase", SPARQLFuncOp::sparql_lcase);
        register(map, "ucase", SPARQLFuncOp::sparql_ucase);
        register(map, "now", SPARQLFuncOp::sparql_now);
        register(map, "rand", SPARQLFuncOp::sparql_rand);
        // Arity 2/3
        register(map, "regex", SPARQLFuncOp::sparql_regex, SPARQLFuncOp::sparql_regex);

        // Arity 3/4
        register(map, "replace", SPARQLFuncOp::sparql_replace, SPARQLFuncOp::sparql_replace);

        register(map, "round", SPARQLFuncOp::sparql_round);
        register(map, "sameTerm", SPARQLFuncOp::sparql_sameTerm);
        register(map, "sameValue", SPARQLFuncOp::sparql_sameValue);
        register(map, "uuid", SPARQLFuncOp::sparql_uuid);

        register(map, "year", SPARQLFuncOp::sparql_year);
        register(map, "month", SPARQLFuncOp::sparql_month);
        register(map, "day", SPARQLFuncOp::sparql_day);
        register(map, "hours", SPARQLFuncOp::sparql_hours);
        register(map, "minutes", SPARQLFuncOp::sparql_minutes);
        register(map, "seconds", SPARQLFuncOp::sparql_seconds);
        register(map, "tz", SPARQLFuncOp::sparql_tz);
        register(map, "timezone", SPARQLFuncOp::sparql_timezone);

        register(map, "subject", SPARQLFuncOp::sparql_subject);
        register(map, "object", SPARQLFuncOp::sparql_object);
        register(map, "predicate", SPARQLFuncOp::sparql_predicate);
        register(map, "isTriple", SPARQLFuncOp::sparql_isTriple);
        register(map, "triple", SPARQLFuncOp::sparql_triple);

        register(map, "md5", SPARQLFuncOp::sparql_md5);
        register(map, "sha1", SPARQLFuncOp::sparql_sha1);
        register(map, "sha224", SPARQLFuncOp::sparql_sha224);
        register(map, "sha256", SPARQLFuncOp::sparql_sha256);
        register(map, "sha384", SPARQLFuncOp::sparql_sha384);
        register(map, "sha512", SPARQLFuncOp::sparql_sha512);

        register(map, "str", SPARQLFuncOp::sparql_str);
        register(map, "strafter", SPARQLFuncOp::sparql_strafter);
        register(map, "strbefore", SPARQLFuncOp::sparql_strbefore);
        register(map, "strdt", SPARQLFuncOp::sparql_strdt);
        register(map, "strends", SPARQLFuncOp::sparql_strends);
        register(map, "strlang", SPARQLFuncOp::sparql_strlang);
        register(map, "strlangdir", SPARQLFuncOp::sparql_strlangdir);
        register(map, "strlen", SPARQLFuncOp::sparql_strlen);
        register(map, "strstarts", SPARQLFuncOp::sparql_strstarts);
        // Arity 2/3
        register(map, "substr", SPARQLFuncOp::sparql_substr, SPARQLFuncOp::sparql_substr);
        register(map, "struuid", SPARQLFuncOp::sparql_struuid);

        return Map.copyOf(map);
     }

    interface Call { NodeValue exec(NodeValue... nv); }
    interface Function0 { NodeValue exec(); }
    interface Function1 { NodeValue exec(NodeValue nv); }
    interface Function2 { NodeValue exec(NodeValue nv1, NodeValue nv2); }
    interface Function3 { NodeValue exec(NodeValue nv1, NodeValue nv2, NodeValue nv3); }
    interface Function4 { NodeValue exec(NodeValue nv1, NodeValue nv2, NodeValue nv3, NodeValue nv4); }
    interface FunctionN { NodeValue exec(NodeValue... nv); }
}
