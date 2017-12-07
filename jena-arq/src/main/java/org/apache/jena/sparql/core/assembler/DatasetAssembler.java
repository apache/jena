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

package org.apache.jena.sparql.core.assembler ;

import static org.apache.jena.assembler.JA.data;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pNamedGraph;
import static org.apache.jena.sparql.util.graph.GraphUtils.multiValueResource;

import java.util.List ;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.assembler.Mode ;
import org.apache.jena.assembler.assemblers.AssemblerBase ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.graph.GraphFactory ;
import org.apache.jena.sparql.util.FmtUtils ;
import org.apache.jena.sparql.util.graph.GraphUtils ;
import org.apache.jena.system.Txn;

public class DatasetAssembler extends AssemblerBase implements Assembler {
    public static Resource getType() {
        return DatasetAssemblerVocab.tDataset ;
    }

    @Override
    public Dataset open(Assembler a, Resource root, Mode mode) {
        Dataset ds = createDataset(a, root, mode) ;
        return ds ;
    }

    public Dataset createDataset(Assembler a, Resource root, Mode mode) {
        checkType(root, DatasetAssemblerVocab.tDataset);
        // use TIM if quads are loaded or if all named Graphs are loaded via data property
        final boolean allNamedGraphsLoadViaData = multiValueResource(root, pNamedGraph).stream().allMatch(g -> g.hasProperty(data));
        if (root.hasProperty(data) || allNamedGraphsLoadViaData) return new InMemDatasetAssembler().open(a, root, mode);

        // -------- Default graph
        // Can use ja:graph or ja:defaultGraph
        Resource dftGraph = GraphUtils.getResourceValue(root, DatasetAssemblerVocab.pDefaultGraph) ;
        if ( dftGraph == null )
            dftGraph = GraphUtils.getResourceValue(root, DatasetAssemblerVocab.pGraph) ;

        Model dftModel = null ;
        if ( dftGraph != null )
            dftModel = a.openModel(dftGraph) ;
        else
            // Assembler description did not define one.
            dftModel = GraphFactory.makeDefaultModel() ;
        Dataset ds = DatasetFactory.create(dftModel);

        Txn.executeWrite(ds, () -> {
            // -------- Named graphs
            List<RDFNode> nodes = GraphUtils.multiValue(root, DatasetAssemblerVocab.pNamedGraph) ;
            for ( RDFNode n : nodes ) {
                if ( !(n instanceof Resource) )
                    throw new DatasetAssemblerException(root, "Not a resource: " + FmtUtils.stringForRDFNode(n));
                Resource r = (Resource)n;
    
                String gName = GraphUtils.getAsStringValue(r, DatasetAssemblerVocab.pGraphName);
                Resource g = GraphUtils.getResourceValue(r, DatasetAssemblerVocab.pGraph);
                if ( g == null ) {
                    g = GraphUtils.getResourceValue(r, DatasetAssemblerVocab.pGraphAlt);
                    if ( g != null ) {
                        Log.warn(this, "Use of old vocabulary: use :graph not :graphData");
                    } else {
                        throw new DatasetAssemblerException(root, "no graph for: " + gName);
                    }
                }
    
                Model m = a.openModel(g);
                ds.addNamedModel(gName, m);
            }
        });
        AssemblerUtils.setContext(root, ds.getContext()) ;
        return ds ;
    }
}
