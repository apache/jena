/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: ModelSpec.java,v 1.6 2003-08-26 09:55:35 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model;

/**
    A ModelSpec allows Models to be created.
    
 	@author kers
*/
public interface ModelSpec
    {
    /**
        Answer a new Model which fits the specification of this ModelSpec.
    */
    Model createModel();
    
    /**
        Answer a Model that fits the specification of this ModelSpec and is known by
        the given name.
    */
    Model createModel( String name );
    
    /**
        Answer an RDF description of this ModelSpec using the JMS vocabulary. The
        description root will be a freshly-created bnode.
    */
    Model getDescription();
    
    /**
        Answer an RDF description of this ModelSpec using the JMS vocabulary, with
        the given Resource as root.
        
        @param root the resource to be used for all the top-level properties
        @return a description of this ModelSpec
    */
    Model getDescription( Resource root );
    
    /**
        Add this ModelSpec's description to a given model, under the given resource
        @param m the model to which the description is to be added
        @param self the resource to which the properties are to be added
        @return the model m (for cascading and convenience)
    */
    Model addDescription( Model m, Resource self );
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