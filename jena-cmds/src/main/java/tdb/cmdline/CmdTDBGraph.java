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

package tdb.cmdline;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb.store.GraphTDB;

public abstract class CmdTDBGraph extends CmdTDB {
    private static final ArgDecl argNamedGraph = new ArgDecl(ArgDecl.HasValue, "graph");
    protected String             graphName     = null;

    protected CmdTDBGraph(String[] argv) {
        super(argv);
        super.add(argNamedGraph, "--graph=IRI", "Act on a named graph");
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs();
        if ( contains(argNamedGraph) )
            graphName = getValue(argNamedGraph);
    }

    protected Model getModel() {
        Dataset ds = tdbDatasetAssembler.getDataset();

        if ( graphName != null ) {
            Model m = ds.getNamedModel(graphName);
            if ( m == null )
                throw new CmdException("No such named graph (is this a TDB dataset?)");
            return m;
        } else
            return ds.getDefaultModel();
    }

    public Node getGraphName() {
        return graphName == null ? null : NodeFactory.createURI(graphName);
    }

    protected GraphTDB getGraph() {
        DatasetGraph dsg = tdbDatasetAssembler.getDataset().asDatasetGraph();
        if ( graphName != null )
            return (GraphTDB)dsg.getGraph(getGraphName());
        else
            return (GraphTDB)dsg.getDefaultGraph();
    }

    @Override
    protected String getCommandName() {
        return Lib.className(this);
    }
}
