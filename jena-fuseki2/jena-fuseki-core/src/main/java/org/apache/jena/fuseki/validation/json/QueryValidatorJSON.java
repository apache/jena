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

package org.apache.jena.fuseki.validation.json;

import static org.apache.jena.fuseki.validation.json.ValidatorJsonLib.getArg;
import static org.apache.jena.fuseki.validation.json.ValidatorJsonLib.getArgOrNull;
import static org.apache.jena.fuseki.validation.json.ValidatorJsonLib.jErrors;
import static org.apache.jena.fuseki.validation.json.ValidatorJsonLib.jParseError;
import static org.apache.jena.fuseki.validation.json.ValidatorJsonLib.jParseErrorCol;
import static org.apache.jena.fuseki.validation.json.ValidatorJsonLib.jParseErrorLine;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.serializer.SerializationContext;

public class QueryValidatorJSON {

    public QueryValidatorJSON() {}

    static final String paramQuery       = "query";
    static final String paramSyntax      = "languageSyntax";

    static final String jInput           = "input";

    static final String jFormatted       = "formatted";
    static final String jAlgebra         = "algebra";
    static final String jAlgebraQuads    = "algebra-quads";
    static final String jAlgebraOpt      = "algebra-opt";
    static final String jAlgebraOptQuads = "algebra-opt-quads";

    public static JsonObject execute(ValidationAction action) {
        JsonBuilder obj = new JsonBuilder();
        obj.startObject();

        final String queryString = getArg(action, paramQuery);

        String querySyntax = getArgOrNull(action, paramSyntax);
        if ( querySyntax == null || querySyntax.equals("") )
            querySyntax = "SPARQL";

        Syntax language = Syntax.lookup(querySyntax);
        if ( language == null ) {
            ServletOps.errorBadRequest("Unknown syntax: " + querySyntax);
            return null;
        }

        boolean outputSPARQL = true;
        boolean outputAlgebra = true;
        boolean outputQuads = true;
        boolean outputOptimized = true;
        boolean outputOptimizedQuads = true;

        obj.key(jInput).value(queryString);

        // Attempt to parse it.
        Query query = null;
        try {
            query = QueryFactory.create(queryString, "http://example/base/", language);
        } catch (QueryParseException ex) {
            obj.key(jErrors);
            obj.startArray();      // Errors array
            obj.startObject();
            obj.key(jParseError).value(ex.getMessage());
            obj.key(jParseErrorLine).value(ex.getLine());
            obj.key(jParseErrorCol).value(ex.getColumn());
            obj.finishObject();
            obj.finishArray();

            obj.finishObject(); // Outer object
            return obj.build().getAsObject();
        }

        if ( query != null ) {

            if ( outputSPARQL )
                formatted(obj, query);

            if ( outputAlgebra )
                algebra(obj, query);

            if ( outputQuads )
                algebraQuads(obj, query);

            if ( outputOptimized )
                algebraOpt(obj, query);

            if ( outputOptimizedQuads )
                algebraOptQuads(obj, query);
        }

        obj.finishObject();
        return obj.build().getAsObject();
    }

    private static void formatted(JsonBuilder obj, Query query) {
        IndentedLineBuffer out = new IndentedLineBuffer();
        query.serialize(out);
        obj.key(jFormatted).value(out.asString());
    }

    private static void algebra(JsonBuilder obj, Query query) {
        Op op = Algebra.compile(query);
        obj.key(jAlgebra).value(string(query, op));
    }

    private static void algebraQuads(JsonBuilder obj, Query query) {
        Op op = Algebra.compile(query);
        op = Algebra.toQuadForm(op);
        obj.key(jAlgebraQuads).value(string(query, op));
    }

    private static void algebraOpt(JsonBuilder obj, Query query) {
        Op op = Algebra.compile(query);
        op = Algebra.optimize(op);
        obj.key(jAlgebraOpt).value(string(query, op));
    }

    private static void algebraOptQuads(JsonBuilder obj, Query query) {
        Op op = Algebra.compile(query);
        op = Algebra.toQuadForm(op);
        op = Algebra.optimize(op);
        obj.key(jAlgebraOptQuads).value(string(query, op));
    }

    private static String string(Query query, Op op) {
        final SerializationContext sCxt = new SerializationContext(query);
        IndentedLineBuffer out = new IndentedLineBuffer();
        op.output(out, sCxt);
        return out.asString();
    }
}
