/**
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

import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.atlas.lib.CacheFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.protobuf.wire.PB_RDF.RDF_IRI;
import org.apache.jena.riot.protobuf.wire.PB_RDF.RDF_PrefixDecl;
import org.apache.jena.riot.protobuf.wire.PB_RDF.RDF_Quad;
import org.apache.jena.riot.protobuf.wire.PB_RDF.RDF_Triple;
import org.apache.jena.riot.system.FactoryRDFCaching;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;

/** Protobuf RDF (wire format items) to StreamRDF terms (Jena java objects)
 *
 * @see StreamRDF2Protobuf for the reverse process.
 */

public class Protobuf2StreamRDF implements VisitorStreamRowProtoRDF {

    private final StreamRDF dest;
    private final PrefixMap pmap;
    private final Cache<String, Node> uriCache =
            CacheFactory.createSimpleCache(FactoryRDFCaching.DftNodeCacheSize);

    public Protobuf2StreamRDF(PrefixMap pmap, StreamRDF stream) {
        this.pmap = pmap;
        this.dest = stream;
    }

    @Override
    public void visit(RDF_Triple rt) {
        Triple t = ProtobufConvert.convert(uriCache, rt, pmap);
        dest.triple(t);
    }

    @Override
    public void visit(RDF_Quad rq) {
        Quad q = ProtobufConvert.convert(uriCache, rq, pmap);
        dest.quad(q);
    }

    @Override
    public void visit(RDF_PrefixDecl prefixDecl) {
        String prefix = prefixDecl.getPrefix();
        String iriStr = prefixDecl.getUri();
        pmap.add(prefix, iriStr);
        dest.prefix(prefix, iriStr);
    }

    @Override
    public void visit(RDF_IRI baseDecl) {
        String iriStr = baseDecl.getIri();
        dest.base(iriStr);
    }
}
