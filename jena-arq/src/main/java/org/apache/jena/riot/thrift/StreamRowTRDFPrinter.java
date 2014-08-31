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

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.riot.thrift.wire.RDF_PrefixDecl ;
import org.apache.jena.riot.thrift.wire.RDF_Quad ;
import org.apache.jena.riot.thrift.wire.RDF_Term ;
import org.apache.jena.riot.thrift.wire.RDF_Triple ;

/** Print (in debug format) an rdf-thrift stream */ 
public class StreamRowTRDFPrinter implements VisitorStreamRowTRDF
{
    private static final boolean ONELINE = false ;
    private final IndentedWriter out ;

    public StreamRowTRDFPrinter(IndentedWriter out) { this.out = out ; }
    
    @Override
    public void visit(RDF_Triple triple) {
        out.print("RDF_Triple") ;
        out.incIndent(); 
        gap() ;
        print(triple.getS()) ;
        gap() ;
        print(triple.getP()) ;
        gap() ;
        print(triple.getO()) ;
        lineEnd() ;
        out.decIndent();
    }

    @Override
    public void visit(RDF_Quad quad) {
        out.print("RDF_Quad") ;
        out.incIndent(); 
        gap() ;
        print(quad.getS()) ;
        gap() ;
        print(quad.getP()) ;
        gap() ;
        print(quad.getO()) ;
        if ( quad.isSetG() ) {
            gap() ;
            print(quad.getG()) ;
        }
        lineEnd() ;
        out.decIndent();
    }

    @Override
    public void visit(RDF_PrefixDecl prefix) {
        out.printf("RDF_PrefixDecl (%s: %s)\n", prefix.getPrefix(), prefix.getUri()) ;
    }
    
    private void gap() { 
        if ( ONELINE )
            out.print(" ") ;
        else
            out.println() ;
    }
    
    private void lineEnd() {
        if ( ! ONELINE )
            out.println() ;
    }
    
    private void startRow() { }
    
    private void finishRow() { }
    
    private void print(RDF_Term term) {
        out.print(term.toString()) ;
    }
}
