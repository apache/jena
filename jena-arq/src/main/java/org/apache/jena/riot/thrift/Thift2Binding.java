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

import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.IteratorSlotted ;
import org.apache.jena.riot.thrift.wire.RDF_DataTuple ;
import org.apache.jena.riot.thrift.wire.RDF_Term ;
import org.apache.jena.riot.thrift.wire.RDF_VAR ;
import org.apache.jena.riot.thrift.wire.RDF_VarTuple ;
import org.apache.thrift.TException ;
import org.apache.thrift.protocol.TProtocol ;
import org.apache.thrift.transport.TIOStreamTransport ;
import org.apache.thrift.transport.TTransportException ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;

/** Converted from SPARQL result set encoded in Thrift to Bindings */
public class Thift2Binding extends IteratorSlotted<Binding> implements Iterator<Binding> {

    private List<Var> vars = new ArrayList<>() ;
    private List<String> varNames = new ArrayList<>() ;
    private final RDF_DataTuple row = new RDF_DataTuple() ;
    private InputStream in ;
    private TProtocol protocol ;

    public Thift2Binding(InputStream in) { 
        this.in = in ;
        TIOStreamTransport transport = new TIOStreamTransport(in) ;
        this.protocol = TRDF.protocol(transport) ;
        readVars() ;
    }
    
    public Thift2Binding(TProtocol out) { 
        this.in = null ;
        this.protocol = out ;
        readVars() ;
    }
    
    private void readVars() {
        RDF_VarTuple vrow = new RDF_VarTuple() ;
        try { vrow.read(protocol) ; }
        catch (TException e) { TRDF.exception(e) ; }
        // ** Java8
//        vrow.getVars().forEach(rv -> {
//            String vn = rv.getName() ;
//            Var v = Var.alloc(rv.getName()) ;
//            varNames.add(vn) ;
//        }) ;
        for ( RDF_VAR rv : vrow.getVars() ) {
            String vn = rv.getName() ;
            Var v = Var.alloc(rv.getName()) ;
            varNames.add(vn) ;
        }
        vars = Var.varList(varNames) ;
    }
    
    public List<Var> getVars()              { return vars ; }

    public List<String> getVarNames()       { return varNames ; }

    
    @Override
    protected Binding moveToNext() {
        try { row.read(protocol) ; }
        catch (TTransportException e) { return null ; }
        catch (TException e) { TRDF.exception(e) ; }
        
        if ( row.getRowSize() != vars.size() )
            throw new RiotThriftException(String.format("Vars %d : Row length : %d", vars.size(), row.getRowSize())) ;

        BindingMap b = BindingFactory.create() ;
        
        for ( int i = 0 ;  i < vars.size() ; i++ ) {
            // Old school
            Var v = vars.get(i) ;
            RDF_Term rt = row.getRow().get(i) ;
            if ( rt.isSetUndefined() )
                continue ;
            Node n = ThriftConvert.convert(rt) ;
            b.add(v, n) ;
        }
        row.clear() ;
        return b ; 
    }

    @Override
    protected boolean hasMore() {
        return true ;
    }
}
