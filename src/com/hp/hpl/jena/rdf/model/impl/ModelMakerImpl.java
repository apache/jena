/*
  (c) Copyright 2002, 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: ModelMakerImpl.java,v 1.6 2003-08-20 13:02:12 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;

/**
    A ModelMakerImpl implements a ModelMaker over a GraphMaker.
*/
public class ModelMakerImpl implements ModelMaker
    {
    protected GraphMaker maker;
    protected Model description;
    
    public ModelMakerImpl( GraphMaker maker )
        { this.maker = maker; }
        
    public GraphMaker getGraphMaker()
        { return maker; }
        
    public void close()
        { maker.close(); }
        
    public Model openModel( String name, boolean strict )
        { return new ModelCom( maker.openGraph( name, strict ) ); }
        
    public Model openModel( String name )
        { return openModel( name, false ); }
        
    public Model createModel( String name, boolean strict )
        { return new ModelCom( maker.createGraph( name, strict ) ); }
        
    public Model createModel( String name )
        { return createModel( name, false ); }
        
    public Model createModel()
        { return new ModelCom( maker.createGraph() ); }
        
    public Model getModel()
        { return new ModelCom( maker.getGraph() ); }
        
    public Model getDescription()
        { 
        if (description == null) description = new ModelCom( maker.getDescription() );    
        return description; 
        }
        
    public Model getDescription( Resource root )
        { return new ModelCom( maker.getDescription( root.asNode() ) ); }
        
    public Model addDescription( Model m, Resource self )
        { return new ModelCom( maker.addDescription( m.getGraph(), self.asNode() ) ); }
        
    public void removeModel( String name )
        { maker.removeGraph( name ); }
        
    public boolean hasModel( String name )
        { return maker.hasGraph( name ); }
    }

/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/