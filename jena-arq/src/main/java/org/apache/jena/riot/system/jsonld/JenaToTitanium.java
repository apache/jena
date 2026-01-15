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

package org.apache.jena.riot.system.jsonld;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.serialization.QuadsToJsonld;
import com.apicatalog.rdf.api.RdfConsumerException;
import jakarta.json.JsonArray;
import org.apache.jena.graph.Node;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

public class JenaToTitanium {

    static class JenaTitaniumException extends JenaException {
        JenaTitaniumException(String msg) { super(msg); }
        JenaTitaniumException(String msg, Throwable cause)   { super(msg, cause) ; }
    }

    /** Translates a Jena {@link DatasetGraph} to a JSON-LD document **/
    public static JsonArray convert(DatasetGraph dataset, JsonLdOptions opts) throws JsonLdError {
        QuadsToJsonld consumer = JsonLd.fromRdf()
                .options(opts)
                .mode(JsonLdVersion.V1_1);
        dataset.stream().forEach( quad->{
            String s = resource(quad.getSubject());
            String p = resource(quad.getPredicate());
            String g = resourceGraphName(quad.getGraph());
            Node obj = quad.getObject();

            if ( obj.isURI() || obj.isBlank() ) {
                String o = resource(obj);
                try {
                    consumer.quad(s, p, o, null, null, null, g);
                } catch (RdfConsumerException ex) {
                    throw new JenaTitaniumException("Exception while translating to JSON-LD", ex);
                }
            } else if ( obj.isLiteral() ) {
                String lex = obj.getLiteralLexicalForm();
                String datatype = obj.getLiteralDatatypeURI();
                String lang = obj.getLiteralLanguage();
                if ( lang.isEmpty() )
                    lang = null;
                String dir = null;
                if ( obj.getLiteralBaseDirection() != null )
                    dir = obj.getLiteralBaseDirection().toString();
                try {
                    consumer.quad(s, p, lex, datatype, lang, dir, g);
                } catch (RdfConsumerException ex) {
                    throw new JenaTitaniumException("Exception while translating to JSON-LD", ex);
                }
            } else if  ( obj.isTripleTerm() ) {
                throw new JenaTitaniumException("Triple terms not supported for JSON-LD");
            } else {
                throw new JenaTitaniumException("Encountered unexpected term: "+obj);
            }
        });

        return consumer.toJsonLd();
    }

    private static String resourceGraphName(Node gn) {
        if ( gn == null || Quad.isDefaultGraph(gn) )
            return null;
        return resource(gn);
    }

    private static String resource(Node term) {
        if ( term.isBlank() )
            return "_:"+term.getBlankNodeLabel();
        if ( term.isURI() )
            return term.getURI();
        throw new JenaTitaniumException("Not a URI or a blank node");
    }

}
