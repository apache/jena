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

import java.io.IOException;
import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.iterator.IteratorSlotted ;
import org.apache.jena.graph.Node ;
import org.apache.jena.riot.protobuf.wire.PB_RDF.RDF_DataTuple;
import org.apache.jena.riot.protobuf.wire.PB_RDF.RDF_Term;
import org.apache.jena.riot.protobuf.wire.PB_RDF.RDF_VarTuple;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingBuilder;

/** Converted from SPARQL result set encoded in Thrift to Bindings */
public class Protobuf2Binding extends IteratorSlotted<Binding> implements Iterator<Binding> {

    private List<Var> vars = new ArrayList<>() ;
    private List<String> varNames = new ArrayList<>() ;
    private InputStream input ;
    private BindingBuilder b = Binding.builder() ;

    public Protobuf2Binding(InputStream input) {
        this.input = input ;
        readVars() ;
    }

    private void readVars() {
        try {
            RDF_VarTuple vrow = RDF_VarTuple.parseDelimitedFrom(input);
            if ( vrow != null )
                vrow.getVarsList().forEach(rv->varNames.add(rv.getName()));
            vars = Var.varList(varNames) ;
        } catch (IOException ex) { IO.exception(ex); }
    }

    public List<Var> getVars()              { return vars ; }

    public List<String> getVarNames()       { return varNames ; }

    @Override
    protected Binding moveToNext() {
        b.reset();
        try {
            RDF_DataTuple dataTuple = RDF_DataTuple.parseDelimitedFrom(input);
            if ( dataTuple == null )
                return null;
            List<RDF_Term> row = dataTuple.getRowList();
            if ( row.size() != vars.size() )
                throw new RiotProtobufException(String.format("Vars %d : Row length : %d", vars.size(), row.size())) ;
            for ( int i = 0 ;  i < vars.size() ; i++ ) {
                // Old school
                Var v = vars.get(i) ;
                RDF_Term rt = row.get(i) ;
                if ( rt.hasUndefined() )
                    continue ;
                Node n = ProtobufConvert.convert(rt) ;
                b.add(v, n) ;
            }
        } catch (IOException ex) { IO.exception(ex); }
        return b.build() ;
    }

    @Override
    protected boolean hasMore() {
        return true ;
    }
}
