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

package dev;

import org.openjena.atlas.event.Event ;
import org.openjena.atlas.event.EventManager ;
import org.openjena.atlas.event.EventType ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphWrapper ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** DatasetGraphWrapper that adds
 * events to change operations 
 * on a DatasetGraph. */

public class DSG_Notify extends DatasetGraphWrapper
{
    //Enum-ization?
    static final String URI                 = "http://openjena.org/TDB/dataset" ;
    static final EventType etAddQuad        = new EventType(URI+"addQuad") ;
    static final EventType etDeleteQuad     = new EventType(URI+"deleteQuad") ;
    static final EventType etDeleteAnyQuad  = new EventType(URI+"deleteAnyQuad") ;
    static final EventType etAddGraph       = new EventType(URI+"addGraph") ;
    static final EventType etRemoveQuad     = new EventType(URI+"removeGraph") ;
    static final EventType etClose          = new EventType(URI+"close") ;
    
    public DSG_Notify(DatasetGraph dsg)
    {
        super(dsg) ;
    }

    @Override
    public void add(Quad quad)
    {
        Event ev = new Event(etAddQuad, quad) ;
        EventManager.send(this, ev) ;
        super.add(quad) ;
    }

    @Override
    public void delete(Quad quad)
    {
        
    }

    @Override
    public void deleteAny(Node g, Node s, Node p, Node o)
    {}

    @Override
    public void setDefaultGraph(Graph g)
    {}

    @Override
    public void addGraph(Node graphName, Graph graph)
    {}

    @Override
    public void removeGraph(Node graphName)
    {}

    @Override
    public void close()
    {}
}
