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

package org.apache.jena.tdb1.assembler;

import static org.apache.jena.sparql.util.graph.GraphUtils.exactlyOneProperty ;
import static org.apache.jena.sparql.util.graph.GraphUtils.getStringValue ;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.assembler.exceptions.AssemblerException ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Node ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.assembler.AssemblerUtils ;
import org.apache.jena.sparql.core.assembler.DatasetAssembler ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.tdb1.TDB1;
import org.apache.jena.tdb1.TDB1Factory;
import org.apache.jena.tdb1.base.file.Location;

@SuppressWarnings("removal")
public class DatasetAssemblerTDB1 extends DatasetAssembler
{
    // This is not a NamedDatasetAssembler
    // Sharing is done by "same location" and must be system wide (not just assemblers).
    // In-memory TDB1 dataset can use named memory locations e.g. "--mem--/NAME"

    static { JenaSystem.init(); }

    public DatasetAssemblerTDB1() {}

    @Override
    public DatasetGraph createDataset(Assembler a, Resource root) {
        return make(a, root);
    }

    public static DatasetGraph make(Assembler a, Resource root) {
        if ( !exactlyOneProperty(root, VocabTDB1.pLocation) )
            throw new AssemblerException(root, "No location given");

        String dir = getStringValue(root, VocabTDB1.pLocation);
        Location loc = Location.create(dir);
        DatasetGraph dsg = TDB1Factory.createDatasetGraph(loc);

        if ( root.hasProperty(VocabTDB1.pUnionDefaultGraph) ) {
            Node b = root.getProperty(VocabTDB1.pUnionDefaultGraph).getObject().asNode();
            NodeValue nv = NodeValue.makeNode(b);
            if ( nv.isBoolean() )
                dsg.getContext().set(TDB1.symUnionDefaultGraph, nv.getBoolean());
            else
                Log.warn(DatasetAssemblerTDB1.class, "Failed to recognize value for union graph setting (ignored): " + b);
        }

        /*
            <r> rdf:type tdb:DatasetTDB2;
                tdb:location "dir";
                //ja:context [ ja:cxtName "arq:queryTimeout";  ja:cxtValue "10000" ] ;
                tdb:unionGraph true; # or "true"
         */
        AssemblerUtils.mergeContext(root, dsg.getContext());
        return dsg;
    }
}
