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

package org.apache.jena.web;


import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.DatasetAccessor ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

/** Adapter between Dataset/Model level and DatasetGraph/Graph level */ 
public class DatasetAdapter implements DatasetAccessor
{
    private final DatasetGraphAccessor updater ;

    public DatasetAdapter(DatasetGraphAccessor updater) { this.updater = updater ; }
    
    /** Get the default model of a Dataset */
    @Override
    public Model getModel()
    {
        Graph g = updater.httpGet() ;
        return ModelFactory.createModelForGraph(g) ;
    }

    /** Get a named model of a Dataset */
    @Override
    public Model getModel(String graphUri)
    {
        Graph g = updater.httpGet(NodeFactory.createURI(graphUri)) ;
        if ( g == null )
            return null ;
        return ModelFactory.createModelForGraph(g) ;
    }

    @Override
    public boolean containsModel(String graphUri)
    {
        return updater.httpHead(NodeFactory.createURI(graphUri)) ;
    }

    /** Put (replace) the default model of a Dataset */
    @Override
    public void putModel(Model data)
    {
        updater.httpPut(data.getGraph()) ;
    }

    /** Put (create/replace) a named model of a Dataset */
    @Override
    public void putModel(String graphUri, Model data)
    {
        updater.httpPut(NodeFactory.createURI(graphUri), data.getGraph()) ;
    }

    /** Delete (which means clear) the default model of a Dataset */
    @Override
    public void deleteDefault()
    {
        updater.httpDelete() ;
    }

    /** Delete a named model of a Dataset */
    @Override
    public void deleteModel(String graphUri)
    {
        updater.httpDelete(NodeFactory.createURI(graphUri)) ;
    }

    /** Add statements to the default model of a Dataset */
    @Override
    public void add(Model data)
    {
        updater.httpPost(data.getGraph()) ;   
    }
    
    /** Add statements to a named model of a Dataset */
    @Override
    public void add(String graphUri, Model data)
    {
        updater.httpPost(NodeFactory.createURI(graphUri), data.getGraph()) ;
    }
}
