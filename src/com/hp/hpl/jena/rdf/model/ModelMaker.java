/*
  (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ModelMaker.java,v 1.12 2005-02-11 19:20:05 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;

/**
    A ModelMaker contains a collection of named models, methods for creating
    new models [both named and anonymous] and opening previously-named
    models, removing models, and accessing a single "default" Model for this
    Maker.
    
 	@author kers
*/

public interface ModelMaker extends ModelSpec
    {
    /**
        Create a new Model associated with the given name. If there is no such
        association, create one and return it. If one exists but <code>strict</code>
        is false, return the associated Model. Otherwise throw an AlreadyExistsException.
    
        @param name the name to give to the new Model
        @param strict true to cause existing bindings to throw an exception
        @exception AlreadyExistsException if that name is already bound.
    */
    public Model createModel( String name, boolean strict );

    public Model getModel();
    
    /**
        Create a Model that does not already exist - equivalent to
        <br><code>createModel( name, false )</code>.
    */
    public Model createModel( String name );
    
    /**
        Create a new anonymous Model, as per ModelSpec.
        
        @return a fresh Model, not accessible under a[nother] name.
    */
    public Model createModel();

    /**
        Find an existing Model that this factory knows about under the given
        name. If such a Model exists, return it. Otherwise, if <code>strict</code>
        is false, create a new Model, associate it with the name, and return it.
        Otherwise throw a DoesNotExistException. 
    
        @param name the name of the Model to find and return
        @param strict false to create a new one if one doesn't already exist
        @exception DoesNotExistException if there's no such named Model
    */
    public Model openModel( String name, boolean strict );

    /**
        Equivalent to <code>openModel( name, false )</code> 
    */
    public Model openModel( String name );

    /**
        Remove the association between the name and the Model. create
        will now be able to create a Model with that name, and open will no
        longer be able to find it. Throws an exception if there's no such Model.
        The Model itself is not touched.
    
        @param name the name to disassociate
        @exception DoesNotExistException if the name is unbound
    */
    public void removeModel( String name );

    /**
        return true iff the factory has a Model with the given name
    
        @param name the name of the Model to look for
        @return true iff there's a Model with that name
    */
    public boolean hasModel( String name );

    /**
        Close the factory - no more requests need be honoured, and any clean-up
        can be done.
    */
    public void close();
    
    /**
        Answer a GraphMaker that makes graphs the same way this ModelMaker
        makes models. In general this will be an underlying GraphMaker.
    */
    public GraphMaker getGraphMaker();
    
    /**
        Answer an [extended] iterator where each element is the name of a model in
        the maker, and the complete sequence exhausts the set of names. No particular
        order is expected from the list.
        @return an extended iterator over the names of models known to this Maker.
    */
    ExtendedIterator listModels();
    }


/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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