/*
 	(c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: Mode.java,v 1.6 2007-01-02 11:52:47 andy_seaborne Exp $
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
    
    @author kers
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


/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/