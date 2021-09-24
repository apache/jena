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

package org.apache.jena.riot.protobuf;

import java.io.*;

import com.google.protobuf.GeneratedMessageV3;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.protobuf.wire.PB_RDF.RDF_Quad;
import org.apache.jena.riot.protobuf.wire.PB_RDF.RDF_StreamRow;
import org.apache.jena.riot.protobuf.wire.PB_RDF.RDF_Term;
import org.apache.jena.riot.protobuf.wire.PB_RDF.RDF_Triple;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.core.Quad;

/** Package support functions */
class PBufRDF {

    private static PrefixMap PMAP0 = PrefixMapFactory.emptyPrefixMap();

    public static void writeDelimitedTo(GeneratedMessageV3 gmv3, OutputStream output) {
        try {
            gmv3.writeDelimitedTo(output);
        } catch (IOException e) { IO.exception(e); }
    }

    public static RDF_Triple rdfTriple(Triple triple, RDF_Triple.Builder tripleBuilder, RDF_Term.Builder termBuilder) {
        tripleBuilder.clear();
        tripleBuilder.setS(rdfTerm(triple.getSubject(), termBuilder));
        tripleBuilder.setP(rdfTerm(triple.getPredicate(), termBuilder));
        tripleBuilder.setO(rdfTerm(triple.getObject(), termBuilder));
        return tripleBuilder.build();
    }

    public static RDF_Quad rdfQuad(Quad quad, RDF_Quad.Builder quadBuilder, RDF_Term.Builder termBuilder) {
        quadBuilder.clear();
        if ( quad.getGraph() != null )
            quadBuilder.setG(rdfTerm(quad.getGraph(), termBuilder));
        quadBuilder.setS(rdfTerm(quad.getSubject(), termBuilder));
        quadBuilder.setP(rdfTerm(quad.getPredicate(), termBuilder));
        quadBuilder.setO(rdfTerm(quad.getObject(), termBuilder));
        return quadBuilder.build();
    }

    public static RDF_Term rdfTerm(Node node, RDF_Term.Builder termBuilder) {
        return rdfTerm(node, termBuilder, false);
    }

    static RDF_Term rdfTerm(Node node, RDF_Term.Builder termBuilder, boolean encodeValues) {
        termBuilder.clear();
        RDF_Term term = ProtobufConvert.toProtobuf(node, PMAP0, termBuilder, encodeValues);
        return term;
    }


    /** Visitor dispatch for {@link RDF_StreamRow} */
    static boolean visit(RDF_StreamRow x, VisitorStreamRowProtoRDF visitor) {
        if ( x == null )
            return false;
        switch(x.getRowCase()) {
            case BASE :
                visitor.visit(x.getBase());
                return true;
            case PREFIXDECL :
                visitor.visit(x.getPrefixDecl());
                return true;
            case QUAD :
                visitor.visit(x.getQuad());
                return true;
            case TRIPLE :
                visitor.visit(x.getTriple());
                return true;
            case ROW_NOT_SET :
                throw new InternalErrorException();
        }
        return false;
    }
}
