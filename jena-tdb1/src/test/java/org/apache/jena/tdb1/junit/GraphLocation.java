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

package org.apache.jena.tdb1.junit;


import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.tdb1.TDB1Factory;
import org.apache.jena.tdb1.base.file.Location;

/** Manage a graph at a fixed location */
public class GraphLocation
{
    private Location loc = null ;
    private Graph graph = null ;
    private Model model = null ;
    private DatasetGraph dsg = null ;
    
    public GraphLocation(Location loc)
    {
        this.loc = loc ;
    }
    
    public void clearDirectory() { FileOps.clearDirectory(loc.getDirectoryPath()) ; }
    
    public Graph getGraph() { return graph ; }
    
    public Model getModel() { return model ; }
    
    public Dataset getDataset()
    { 
        if ( dsg == null ) return null ;
        return DatasetFactory.wrap(dsg) ;
    }

    public Dataset createDataset() 
    {
        if ( dsg != null )
            throw new TDBTestException("dataset already in use") ;
        dsg = TDB1Factory.createDatasetGraph(loc) ;
        return DatasetFactory.wrap(dsg) ;
    }
    
    public Graph createGraph()
    {
        if ( graph != null )
            throw new TDBTestException("Graph already in use") ;
        graph = TDB1Factory.createDatasetGraph(loc).getDefaultGraph() ;
        model = ModelFactory.createModelForGraph(graph) ;
        return graph ;
    }
    
    public void clearGraph()
    { 
        if ( graph != null )
        {
            Iterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
            List<Triple> triples = Iter.toList(iter) ;
            
            for ( Triple t : triples )
                graph.delete(t) ;
        }
    }

    public void release()
    {
        if ( graph != null )
        {
            graph.close();
            graph = null ;
            model = null ;
        }

        if ( dsg != null )
        {
            dsg.close();
            dsg = null ;
        }
    }
}
