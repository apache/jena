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

package sdb.cmd;

import jena.cmd.ArgDecl;
import jena.cmd.CmdArgModule;
import jena.cmd.CmdGeneral;
import jena.cmd.ModBase;

import org.apache.jena.graph.Graph ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.sdb.SDBFactory ;
import org.apache.jena.sdb.Store ;

public class ModGraph extends ModBase
{
    private static ArgDecl argDeclGraphName = new ArgDecl(true, "graph") ;

    private Graph graph = null ;
    private String graphName = null ;
    
    public ModGraph() {}
    
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.add(argDeclGraphName,
                    "--graph=URI", "Graph name") ;
    }

    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
        graphName = cmdLine.getValue(argDeclGraphName) ;
    }

    public String getGraphName() { return graphName ; }
    
    public Graph getGraph(Store store)
    { 
        if ( graphName == null || graphName.equals("default") )
            return SDBFactory.connectDefaultGraph(store) ;
        else
            return SDBFactory.connectNamedGraph(store, graphName) ;
    }
    
    public Model getModel(Store store)
    { 
        if ( graphName == null || graphName.equals("default") )
            return SDBFactory.connectDefaultModel(store) ;
        else
            return SDBFactory.connectNamedModel(store, graphName) ;
    }
}
