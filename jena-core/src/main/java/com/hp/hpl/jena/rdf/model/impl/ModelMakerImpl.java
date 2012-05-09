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

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;

/**
    A ModelMakerImpl implements a ModelMaker over a GraphMaker.
*/
public class ModelMakerImpl implements ModelMaker
    {
    protected GraphMaker maker;
    protected Model description;
    
    public ModelMakerImpl( GraphMaker maker )
        { this.maker = maker; }
        
    @Override
    public GraphMaker getGraphMaker()
        { return maker; }
        
    @Override
    public void close()
        { maker.close(); }
       
    public Model openModel()
        { return new ModelCom( maker.openGraph() ); }
    
    protected Model makeModel( Graph g )
        { return new ModelCom( g ); }
    
    @Override
    public Model openModelIfPresent( String name )
        { return maker.hasGraph( name ) ? openModel( name ) : null; }
    
    @Override
    public Model openModel( String name, boolean strict )
        { return makeModel( maker.openGraph( name, strict ) ); }
        
    @Override
    public Model openModel( String name )
        { return openModel( name, false ); }
        
    @Override
    public Model createModel( String name, boolean strict )
        { return makeModel( maker.createGraph( name, strict ) ); }
        
    @Override
    public Model createModel( String name )
        { return createModel( name, false ); }
        
    public Model createModelOver( String name )
        { return createModel( name ); }
        
    @Override
    public Model createFreshModel()
        { return makeModel( maker.createGraph() ); }
        
    @Override
    public Model createDefaultModel()
        { return makeModel( maker.getGraph() ); }
        
    @Override
    public void removeModel( String name )
        { maker.removeGraph( name ); }
        
    @Override
    public boolean hasModel( String name )
        { return maker.hasGraph( name ); }
      
    @Override
    public ExtendedIterator<String> listModels()
        { return maker.listGraphs(); }
    
    /**
        ModelGetter implementation component.     
    */
    @Override
    public Model getModel( String URL )
        { return hasModel( URL ) ? openModel( URL ) : null; }         
    
    @Override
    public Model getModel( String URL, ModelReader loadIfAbsent )
        { 
        Model already = getModel( URL );
        return already == null ? loadIfAbsent.readModel( createModel( URL ), URL ) : already;
        }
    }
