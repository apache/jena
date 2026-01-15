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
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.rdf.api.RdfConsumerException;
import com.apicatalog.rdf.api.RdfQuadConsumer;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;

public class TitaniumToJena {

    /** Translates a Titanium document to a {@link StreamRDF}  **/
    public static void convert(Document document, JsonLdOptions opts, StreamRDF output, ParserProfile profile) throws JsonLdError {
        RdfQuadConsumer consumer = new JsonLDToStreamRDF(output, profile);
        JsonLd.toRdf(document).options(opts).provide(consumer);
    }

    static class JsonLDToStreamRDF implements RdfQuadConsumer {
        private static final long line = -1L;
        private static final long col = -1L;

        private final StreamRDF output;
        private final ParserProfile profile;

        JsonLDToStreamRDF(StreamRDF output, ParserProfile profile) {
            this.output = output;
            this.profile = profile;
        }

        @Override
        public RdfQuadConsumer quad(String subject, String predicate, String object,
                                    String datatype, String language, String direction,
                                    String graph) throws RdfConsumerException {
            Node g = (graph == null) ? null : convertToNode(graph);
            Node s = convertToNode(subject);
            Node p = convertToNode(predicate);
            Node o;

            if ( RdfQuadConsumer.isLiteral(datatype, language, direction) )
                o = convertToLiteral(object, datatype, language, direction);
            else
                o = convertToNode(object);

            if ( g == null )
                output.triple(Triple.create(s, p, o));
            else
                output.quad(Quad.create(g, s, p, o));
            return this;
        }
        private Node convertToNode(String str) {
            if ( RdfQuadConsumer.isBlank(str) ) {
                str = str.substring(2); // Remove "_:"
                return profile.getFactorRDF().createBlankNode(str);
            }
            str = profile.resolveIRI(str, line, col);
            return profile.createURI(str, line, col);
        }

        private Node convertToLiteral(String lexical, String datatypeURI, String language, String direction) {
            if ( RdfQuadConsumer.isLangString(datatypeURI, language, direction) )
                return profile.createLangLiteral(lexical, language, line, col);
            if ( RdfQuadConsumer.isDirLangString(datatypeURI, language, direction) )
                return profile.createLangDirLiteral(lexical, language, direction, line, col);
            RDFDatatype dType = TypeMapper.getInstance().getSafeTypeByName(datatypeURI) ;
            return profile.createTypedLiteral(lexical, dType, line, col);
        }
    }
}
