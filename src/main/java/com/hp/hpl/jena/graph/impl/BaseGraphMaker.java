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

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;

/**
    This base class provides convenience functions for the three "usual" graph
    makers and a place to hold the reification style for the graphs it constructs.   
 
 	@author kers
*/
public abstract class BaseGraphMaker implements GraphMaker
    {
    /**
        Construct the base level of a graph maker.
     	@param style the reification style for all the graphs it makes
     */
    public BaseGraphMaker( ReificationStyle style )
        { this.style = style; }
        
    private int counter = 0;
    protected ReificationStyle style;
    
    /**
        Answer our reification style.
    */
    @Override
    public ReificationStyle getReificationStyle()
        { return style; }
        
    /**
        Answer the default graph for this maker. If we haven't already made it, make it
        now.
     */
    @Override
    public Graph getGraph()
        { 
        if (defaultGraph == null) { defaultGraph = createGraph(); }
        return defaultGraph;
        }
        
    private Graph defaultGraph;
    
    @Override
    public Graph openGraph()
        { if (defaultGraph == null) throw new DoesNotExistException
            ( "no default graph in this GraphMaker [" + this.getClass() + "]" ); 
        return defaultGraph; }
    
    /**
        Make a fresh anonymous graph.
    */
    @Override
    public Graph createGraph()
        { return createGraph( "anon_" + counter++ + "", false ); }
         
     /**
        A non-strict create.
      	@see com.hp.hpl.jena.graph.GraphMaker#createGraph(java.lang.String)
      */
    @Override
    public Graph createGraph(String name)
        { return createGraph( name, false ); }
        
    /**
        A non-strict open.
     	@see com.hp.hpl.jena.graph.GraphMaker#openGraph(java.lang.String)
     */
    @Override
    public Graph openGraph( String name )
        { return openGraph( name, false ); }

    }
