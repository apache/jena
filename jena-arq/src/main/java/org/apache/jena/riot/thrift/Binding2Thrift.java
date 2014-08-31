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
import java.util.Collection ;
import java.util.Iterator ;

import org.apache.jena.riot.thrift.wire.RDF_DataTuple ;
import org.apache.jena.riot.thrift.wire.RDF_Term ;
import org.apache.jena.riot.thrift.wire.RDF_VAR ;
import org.apache.jena.riot.thrift.wire.RDF_VarTuple ;
import org.apache.thrift.TException ;
import org.apache.thrift.protocol.TProtocol ;
import org.apache.thrift.transport.TIOStreamTransport ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

/** Converted from Bindings to SPARQL result set encoded in Thrift */
public class Binding2Thrift implements AutoCloseable {
    private final RDF_DataTuple row = new RDF_DataTuple() ;
    private final Collection<Var> vars ;
    private final OutputStream out ;
    private final TProtocol protocol ;
    private final boolean encodeValues ;

    public Binding2Thrift(OutputStream out, Collection<Var> vars, boolean encodeValues) { 
        this.out = out ;
        this.vars = vars ; 
        TIOStreamTransport transport = new TIOStreamTransport(out) ;
        this.protocol = TRDF.protocol(transport) ;
        this.encodeValues = encodeValues ;
        varsRow() ;
    }

    private void varsRow() {
        RDF_VarTuple vrow = new RDF_VarTuple() ;
        // ** Java8
//        vars.iterator().forEachRemaining( v -> {
//            RDF_VAR rv = new RDF_VAR() ;
//            rv.setName(v.getName()) ;
//            vrow.addToVars(rv) ;
//        }) ;
        for ( Var v : vars ) {
            RDF_VAR rv = new RDF_VAR() ;
            rv.setName(v.getName()) ;
            vrow.addToVars(rv) ;
        }
        try { vrow.write(protocol) ; }
        catch (TException e) { TRDF.exception(e) ; }
    }

    public Binding2Thrift(TProtocol out, Collection<Var> vars, boolean encodeValues) { 
        this.vars = vars ; 
        this.out = null ;
        this.protocol = out ;
        this.encodeValues = encodeValues ;
        varsRow() ;
    }

    public void output(Binding binding) {
        Iterator<Var> vIter = (vars == null ? null : vars.iterator()) ;
        if ( vIter == null )
            vIter = binding.vars() ;
        // ** Java8
//        vIter.forEachRemaining(v -> {
//            Node n = binding.get(v) ;
//            RDF_Term rt = ( n == null ) ? TRDF.tUNDEF : ThriftConvert.convert(n) ;
//            row.addToRow(rt) ;
//        }) ;
        while(vIter.hasNext()) {
            Var v = vIter.next();
            Node n = binding.get(v) ;
            RDF_Term rt = ( n == null ) ? TRDF.tUNDEF : ThriftConvert.convert(n, encodeValues) ;
            row.addToRow(rt) ;
        }
        try { row.write(protocol) ; }
        catch (TException e) { TRDF.exception(e) ; }
        row.clear() ;
    }

    @Override
    public void close() {
        TRDF.flush(protocol) ; 
    }
}
