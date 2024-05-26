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

package org.apache.jena.tdb2.assembler;

import static org.apache.jena.sparql.util.graph.GraphUtils.getAsStringValue;
import static org.apache.jena.sparql.util.graph.GraphUtils.getResourceValue;
import static org.apache.jena.sparql.util.graph.GraphUtils.getStringValue;
import static org.apache.jena.tdb2.assembler.VocabTDB2.*;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.tdb2.DatabaseMgr;

public class TDB2GraphAssembler extends AssemblerBase implements Assembler
{
    static { JenaSystem.init(); }

    @Override
    public Model open(Assembler a, Resource root, Mode mode) {
        Graph g = createGraph(a, root, mode);
        return ModelFactory.createModelForGraph(g);
    }

    public Graph createGraph(Assembler a, Resource root, Mode mode) {
        // Make a model - the default model of the TDB dataset
        // [] rdf:type tdb:GraphTDB;
        //    tdb:location "dir";

        // Make a named model.
        // [] rdf:type tdb:GraphTDB;
        //    tdb:location "dir";
        //    tdb:graphName <http://example/name>;

        // Location or dataset reference.
        String locationDir = getStringValue(root, pLocation);
        Resource dataset = getResourceValue(root, pDataset);

        if ( locationDir != null && dataset != null )
            throw new AssemblerException(root, "Both location and dataset given: exactly one required");

        if ( locationDir == null && dataset == null )
            throw new AssemblerException(root, "Must give location or refer to a dataset description");

        String graphName = null;
        if ( root.hasProperty(pGraphName1) )
            graphName = getAsStringValue(root, pGraphName1);
        if ( root.hasProperty(pGraphName2) )
            graphName = getAsStringValue(root, pGraphName2);

        if ( root.hasProperty(pIndex) )
            Log.warn(this, "Custom indexes not implemented yet - ignored");

        DatasetGraph dsg;

        if ( locationDir != null )
        {
            Location location = Location.create(locationDir);
            dsg = DatabaseMgr.connectDatasetGraph(location);
        }
        else
            dsg = DatasetAssemblerTDB2.make(a, dataset);

        try {
            if ( graphName != null )
                return dsg.getGraph(NodeFactory.createURI(graphName));
            else
                return dsg.getDefaultGraph();
        } catch (RuntimeException ex)
        {
            ex.printStackTrace(System.err);
            throw ex;
        }
    }
}
