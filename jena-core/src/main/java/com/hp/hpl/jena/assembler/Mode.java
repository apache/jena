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

package com.hp.hpl.jena.assembler;

import com.hp.hpl.jena.rdf.model.Resource;

/**
    A Mode object controls whether persistent objects can be created or reused
    by an assembler. Their methods are expected to be called when an
    assembler is about to create a new object (because no object with the given
    name exists) or reuse an existing one (because one does).
    
<p>The default behaviour of the methods is dictated by booleans bound into
    the mode object. Subclasses of mode may exploit the ability to inspect the
    name of the object or its other RDF properties.
*/
public class Mode
    {
    /**
        Mode that demands a new object be created and no existing object
        should exist.
    */
    public static final Mode CREATE = new Mode( true, false );
    
    /**
        Default mode; existing objects are reused, new objects are not created
    */
    public static final Mode DEFAULT = new Mode( false, true );
    
    /**
        Mode that requires that objects should already exist; new objects cannot
        be created.
     */
    public static final Mode REUSE = new Mode( false, true );
    
    /**
        Mode that permits existing objects to be reused and new objects to
        be created.
    */
    public static final Mode ANY = new Mode( true, true );
    
    protected final boolean mayCreate;
    protected final boolean mayReuse;
    
    public Mode( boolean mayCreate, boolean mayReuse )
        { this.mayCreate = mayCreate; this.mayReuse = mayReuse; }
    
    /**
        Answer true if the object <code>root</code> with the given <code>name</code>
        can be created if it does not already exist.
    */
    public boolean permitCreateNew( Resource root, String name )
        { return mayCreate; }

    /**
        Answer true if the existing object <code>root</code> with the given
        <code>name</code> can be reused.
    */
    public boolean permitUseExisting( Resource root, String name )
        { return mayReuse; }
    }
