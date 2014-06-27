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

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.AlreadyExistsException ;
import com.hp.hpl.jena.shared.DoesNotExistException ;
import com.hp.hpl.jena.util.iterator.*;

/**
    A ModelMaker contains a collection of named models, methods for creating
    new models [both named and anonymous] and opening previously-named
    models, removing models, and accessing a single "default" Model for this
    Maker.
    
    <p>Additional constraints are placed on a ModelMaker as compared to its
    ancestor <code>ModelSource</code>. ModelMakers do not arbitrarily forget
    their contents - once they contain a named model, that model stays inside
    the ModelMaker until that ModelMaker goes away, and maybe for longer
    (eg if the ModelMaker fronted a database or directory). And new models
    can be added to a ModelMaker.
*/

public interface ModelMaker extends ModelSource
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
    
    /**
        Create a Model with the given name if no such model exists. Otherwise,
        answer the existing model. Equivalent to 
        <br><code>createModel( name, false )</code>.
    */
    public Model createModel( String name );

    /**
        Find an existing Model that this factory knows about under the given
        name. If such a Model exists, return it. Otherwise, if <code>strict</code>
        is false, create a new Model, associate it with the name, and return it.
        Otherwise throw a DoesNotExistException. 
        
        <p>When called with <code>strict=false</code>, is equivalent to the
        ancestor <code>openModel(String)</code> method.
    
        @param name the name of the Model to find and return
        @param strict false to create a new one if one doesn't already exist
        @exception DoesNotExistException if there's no such named Model
    */
    public Model openModel( String name, boolean strict );

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
    public ExtendedIterator<String> listModels();
    }
