/*
      (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
      [See end of file]
      $Id: ModelExtract.java,v 1.1 2004-08-07 15:45:58 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphExtract;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleBoundary;

/**
     ModelExtract - a wrapper for GraphExtract, allowing rooted sub-models to be
     extracted from other models with some boundary condition.
 	@author hedgehog
*/
public class ModelExtract
    {
    /**
         The statement boundary used to bound the extraction.
    */
    protected StatementBoundary boundary;
    
    /**
         Initialise this ModelExtract with a boundary condition.
    */
    public ModelExtract( StatementBoundary b )
        { boundary = b; }
    
    /**
         Answer the rooted sub-model.
    */
    public Model extract( Resource r, Model s )
        { return extractInto( ModelFactory.createDefaultModel(), r, s ); }
    
    /**
         Answer <code>model</code> after updating it with the sub-graph of
         <code>s</code> rooted at <code>r</code>, bounded by this instances
         <code>boundary</code>.
    */
    public Model extractInto( Model model, Resource r, Model s )
        { TripleBoundary tb = boundary.asTripleBoundary( s );
        Graph g = getGraphExtract( tb ) .extractInto( model.getGraph(), r.asNode(), s.getGraph() );
        return ModelFactory.createModelForGraph( g ); }
    
    /**
         Answer a GraphExtract initialised with <code>tb</code>; extension point
         for sub-classes (specifically TestModelExtract's mocks).
    */
    protected GraphExtract getGraphExtract( TripleBoundary tb )
        { return new GraphExtract( tb ); }
    
    /**
         Answer a TripleBoundary that is implemented in terms of a StatementBoundary. 
    */
    public static TripleBoundary convert( final Model s, final StatementBoundary b )
        {
        return new TripleBoundary()
            { public boolean stopAt( Triple t ) { return b.stopAt( s.asStatement( t ) ); } };
        }
    }

/*
    (c) Copyright 2004, Hewlett-Packard Development Company, LP
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