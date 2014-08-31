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

package org.apache.jena.riot.thrift;

import java.io.OutputStream ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.thrift.wire.* ;
import org.apache.thrift.TException ;
import org.apache.thrift.protocol.TProtocol ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Encode StreamRDF in Thrift.
 *  Usually used via {@linkplain BinRDF} functions. 
 * 
 * @see Thrift2StreamRDF (for each RDF_StreamRow) for the reverse process.
 */ 
public class StreamRDF2Thrift implements StreamRDF, AutoCloseable 
{
    // No REPEAT support.
    private final OutputStream out ;
    private final TProtocol protocol ;
    private PrefixMap pmap = PrefixMapFactory.create() ;
    private final boolean encodeValues ;

//    public StreamRDF2Thrift(OutputStream out) {
//        this(out, false) ;
//    }
    
    public StreamRDF2Thrift(OutputStream out, boolean encodeValues) {
        this.out = out ;
        this.protocol = TRDF.protocol(out) ;
        this.encodeValues = encodeValues ;
    }

//    public StreamRDF2Thrift(TProtocol out) {
//        this(out, false) ;
//    }
    
    public StreamRDF2Thrift(TProtocol out, boolean encodeValues) { 
        this.out = null ;
        this.protocol = out ;
        this.pmap = PrefixMapFactory.create() ;
        this.encodeValues = encodeValues ;
    }

    @Override
    public void start() { }

    private final RDF_StreamRow  tStreamRow   = new RDF_StreamRow() ;
    
    private final RDF_Triple ttriple    = new RDF_Triple() ;
    private final RDF_Quad   tquad      = new RDF_Quad() ;
    
    private final RDF_Term   tsubject   = new RDF_Term() ;
    private final RDF_Term   tpredicate = new RDF_Term() ;
    private final RDF_Term   tobject    = new RDF_Term() ;
    private final RDF_Term   tgraph     = new RDF_Term() ;
    
    @Override
    public void triple(Triple triple) {
        doTriple(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }

    private void doTriple(Node subject, Node predicate, Node object) {
        ThriftConvert.toThrift(subject, pmap, tsubject, encodeValues) ;
        ThriftConvert.toThrift(predicate, pmap, tpredicate, encodeValues) ;
        ThriftConvert.toThrift(object, pmap, tobject, encodeValues) ;
        ttriple.setS(tsubject) ;
        ttriple.setP(tpredicate) ;
        ttriple.setO(tobject) ;

        tStreamRow.setTriple(ttriple) ;
        try { tStreamRow.write(protocol) ; }
        catch (TException e) { TRDF.exception(e) ; }
        tStreamRow.clear();
        ttriple.clear();
        tsubject.clear();
        tpredicate.clear() ;
        tobject.clear() ;
    }
    
    @Override
    public void quad(Quad quad) {
        if ( quad.getGraph() == null || quad.isDefaultGraph() ) {
            doTriple(quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
            return ;
        }
        
        ThriftConvert.toThrift(quad.getGraph(), pmap, tgraph, encodeValues) ;
        ThriftConvert.toThrift(quad.getSubject(), pmap, tsubject, encodeValues) ;
        ThriftConvert.toThrift(quad.getPredicate(), pmap, tpredicate, encodeValues) ;
        ThriftConvert.toThrift(quad.getObject(), pmap, tobject, encodeValues) ;
        
        tquad.setG(tgraph) ;
        tquad.setS(tsubject) ;
        tquad.setP(tpredicate) ;
        tquad.setO(tobject) ;
        tStreamRow.setQuad(tquad) ;
        
        try { tStreamRow.write(protocol) ; } 
        catch (TException e) { TRDF.exception(e) ; }
        
        tStreamRow.clear() ;
        tquad.clear();
        tgraph.clear();
        tsubject.clear();
        tpredicate.clear() ;
        tobject.clear() ;
    }

    @Override
    public void base(String base) {
        // Ignore.
    }

    @Override
    public void prefix(String prefix, String iri) {
        try { pmap.add(prefix, iri) ; }
        catch ( RiotException ex) {
            Log.warn(this, "Prefix mapping error", ex) ;
        }
        RDF_PrefixDecl tprefix = new RDF_PrefixDecl(prefix, iri) ; 
        tStreamRow.setPrefixDecl(tprefix) ;
        try { tStreamRow.write(protocol) ; }
        catch (TException e) { TRDF.exception(e) ; }
        tStreamRow.clear(); 
    }

    @Override
    public void close() {
        finish() ;
    }
    
    @Override
    public void finish() {
        TRDF.flush(protocol) ;
    }
}
