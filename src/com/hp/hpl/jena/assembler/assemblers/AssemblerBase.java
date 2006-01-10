/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: AssemblerBase.java,v 1.4 2006-01-10 10:36:45 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.assemblers;

import java.util.List;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.exceptions.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.IteratorCollection;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.vocabulary.RDF;

public abstract class AssemblerBase implements Assembler
    {
    protected static class MapObjectToContent implements Map1
        {
        protected final Assembler a;
        
        public MapObjectToContent( Assembler a ) 
            { this.a = a; }
        
        public Object map1( Object o )
            { return a.open( ((Statement) o).getResource() ); }
        }

    static final Map1 getObject = new Map1() 
        {
        public Object map1( Object o ) { return ((Statement) o).getObject(); }
        };

    public final Object open( Resource root )
        { return open( this, root ); }

    public final Object open( Assembler a, Resource root )
        { return open( a, root, Mode.DEFAULT ); }

    public abstract Object open( Assembler a, Resource root, Mode mode );

    protected static Resource getUniqueResource( Resource root, Property property )
        { return (Resource) getUnique( root, property ); }

    protected static Literal getUniqueLiteral( Resource root, Property property )
        { return (Literal) getUnique( root, property ); }

    protected static RDFNode getUnique( Resource root, Property property )
        {
        List nodes = IteratorCollection.iteratorToList( root.listProperties( property ) .mapWith( getObject ) );
        if (nodes.size() == 0) return null;
        if (nodes.size() == 1) return (RDFNode) nodes.get(0);
        throw new NotUniqueException( root, property );
        }

    protected void checkType( Resource root, Resource type )
        {
        if (!root.hasProperty( RDF.type, type ))
            throw new CannotConstructException( this.getClass(), root, type );
        }

    public Model openModel( Resource root, Mode mode )
        { return (Model) open( this, root, mode ); }
    
    public Model openModel( Resource root )
        { return openModel( root, Mode.DEFAULT ); }

    protected Resource getRequiredResource( Resource root, Property p )
        {
        Resource R = getUniqueResource( root, p );
        if (R == null) throw new PropertyRequiredException( root, p );
        return R;
        }
    
    protected Literal getRequiredLiteral( Resource root, Property p )
        {
        Literal L = getUniqueLiteral( root, p );
        if (L == null) throw new PropertyRequiredException( root, p );
        return L;
        }

    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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