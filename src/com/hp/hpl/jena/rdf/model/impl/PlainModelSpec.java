/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: PlainModelSpec.java,v 1.8 2004-08-03 08:32:03 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
    A ModelSpec that describes plain (non-inference, non-ontological) models.
    
 	@author kers
*/
public class PlainModelSpec extends ModelSpecImpl implements ModelSpec
    {    
    /**
        Initialise this Spec with the ModelMaker that will be used to create all Models that
        this Spec is used to construct.
        
        @param maker the ModelMaker used to make models.
    */
    public PlainModelSpec( ModelMaker maker )
        { super( maker ); }
        
    /**
        Initialise this Spec with a description that will specify the ModelMaker that this
        Spec is used to construct.
        
        @param root the resource of rdf:type JMS.maker
        @param description the RDF who's JMS.maker describes the ModelMaker
    */
    public PlainModelSpec( Resource root, Model description )
        { super( root, description );
        this.root = root; this.description = description; }
    
    /**
        Answer a Model that satisfies the description that this ModelSpec was constructed
        with.
    */
    public Model createModel()
        { return loadFiles( maker.createModel() ); }
    
    /**
        Answer the Model obtained from the underlying ModelMaker with the given name. 
        
     	@see com.hp.hpl.jena.rdf.model.ModelSpec#createModelOver(java.lang.String)
     */
    public Model createModelOver( String name )
        { return maker.createModel( name, false ); }

    /**
        Answer the sub-property of JMS.maker that describes how this ModelSpec uses the
        attached ModelMaker.
        
        @return JMS.maker
    */
    public Property getMakerProperty()
        { return JMS.maker; }
    }

/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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