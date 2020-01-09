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

package org.apache.jena.tdb.assembler;

import static org.apache.jena.sparql.util.graph.GraphUtils.exactlyOneProperty ;
import static org.apache.jena.sparql.util.graph.GraphUtils.getStringValue ;
import static org.apache.jena.tdb.assembler.VocabTDB.pLocation ;
import static org.apache.jena.tdb.assembler.VocabTDB.pUnionDefaultGraph ;
import org.apache.jena.assembler.Assembler ;
import org.apache.jena.assembler.Mode ;
import org.apache.jena.assembler.exceptions.AssemblerException ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.assembler.AssemblerUtils ;
import org.apache.jena.sparql.core.assembler.DatasetAssembler ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.base.file.Location ;

public class DatasetAssemblerTDB extends DatasetAssembler
{
    static { JenaSystem.init(); }
    
    @Override
    public Dataset createDataset(Assembler a, Resource root, Mode mode) {
        return make(root) ;
    }

    static Dataset make(Resource root) {
        if ( !exactlyOneProperty(root, pLocation) )
            throw new AssemblerException(root, "No location given") ;

        String dir = getStringValue(root, pLocation) ;
        Location loc = Location.create(dir) ;
        DatasetGraph dsg = TDBFactory.createDatasetGraph(loc) ;

        if ( root.hasProperty(pUnionDefaultGraph) ) {
            Node b = root.getProperty(pUnionDefaultGraph).getObject().asNode() ;
            NodeValue nv = NodeValue.makeNode(b) ;
            if ( nv.isBoolean() )
                dsg.getContext().set(TDB.symUnionDefaultGraph, nv.getBoolean()) ;
            else
                Log.warn(DatasetAssemblerTDB.class, "Failed to recognize value for union graph setting (ignored): " + b) ;
        }

        /*
        <r> rdf:type tdb:DatasetTDB ;
            tdb:location "dir" ;
            //ja:context [ ja:cxtName "arq:queryTimeout" ;  ja:cxtValue "10000" ] ;
            tdb:unionGraph true ; # or "true"
        */
        AssemblerUtils.mergeContext(root, dsg.getContext());
        return DatasetFactory.wrap(dsg) ; 
    }
    
}
