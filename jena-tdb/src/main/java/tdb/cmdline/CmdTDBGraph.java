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

import arq.cmd.CmdException ;
import arq.cmdline.ArgDecl ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;

public abstract class CmdTDBGraph extends CmdTDB
{
    private static final ArgDecl argNamedGraph          = new ArgDecl(ArgDecl.HasValue, "graph") ;
    protected String graphName = null ;
    
    protected CmdTDBGraph(String[] argv)
    {
        super(argv) ;
        super.add(argNamedGraph, "--graph=IRI", "Act on a named graph") ;
    }
    
    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        if ( contains(argNamedGraph) )
            graphName = getValue(argNamedGraph) ; 
    }
    
    protected Model getModel()
    {
        Dataset ds = tdbDatasetAssembler.getDataset() ;
        
        if ( graphName != null )
        {
            Model m = ds.getNamedModel(graphName) ;
            if ( m == null )
                throw new CmdException("No such named graph (is this a TDB dataset?)") ;
            return m ;
        }
        else
            return ds.getDefaultModel() ;
    }
    
    public Node getGraphName()  { return graphName == null ? null : NodeFactory.createURI(graphName) ; } 
    
    protected GraphTDB getGraph()
    {
        if ( graphName != null )
            return (GraphTDB)tdbDatasetAssembler.getDataset().getNamedModel(graphName).getGraph() ;
        else
            return (GraphTDB)tdbDatasetAssembler.getDataset().getDefaultModel().getGraph() ;
    }
    
    @Override
    protected String getCommandName()
    {
        return Utils.className(this) ;
    }
    
}
