/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: InfModelSpec.java,v 1.2 2003-08-25 10:26:19 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.vocabulary.*;

/**
    A ModelSpec for InfModels. The description of an InfModel is the description of a 
    ModelMaker [for the base graph] plus the JMS.reasonsWith property to give the
    Resource who's URI identifies the reasoner to use [as per the ReasonerRegistry].
    
 	@author kers
*/
public class InfModelSpec extends ModelSpecImpl
    {
    /**
        The Resource who's URI identifies the reasoner to use.
    */
    protected Resource reasonerResource;
    
    /**
        Initialise an InfModelSpec using the ModelMaker specification and the value of
        the JMS.reasoner property.
    */
    public InfModelSpec( Model description )
        {
        super( description );
        Statement st = description.getRequiredProperty( null, JMS.reasoner );
        reasonerResource = st.getResource();
        }   

    /**
        Answer an InfModel that does the reasoning as defined by the reasoner URI over
        a new graph that is created by the ModelMaker.
        
        @return a new InfModel reasoning over a new base graph.
    */
    public Model createModel()
        {
        String URI = reasonerResource.getURI();
        Reasoner reasoner = ReasonerRegistry.theRegistry().create( URI, null );
        Graph baseGraph = maker.getGraphMaker().createGraph();
        InfGraph graph = reasoner.bind( baseGraph );
        return new InfModelImpl( graph );
        }
    
    /**
        Answer the maker property needed by descriptions.
        @return JMS.maker
    */
    public Property getMakerProperty()
        { return JMS.maker; }
    
    /**
        Add this ModelMaker and Reasoner description to the supplied model under the
        given name, and answer the descrption model.
        
        @param desc the model to augment with this description
        @param self the resource to use as our name
        @return desc, for cascading
    */
    public Model addDescription( Model desc, Resource self )
        {
        super.addDescription( desc, self );
        Resource r = desc.createResource();
        desc.add( self, JMS.reasonsWith, r );
        desc.add( r, JMS.reasoner, reasonerResource );
        return desc;    
        }
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