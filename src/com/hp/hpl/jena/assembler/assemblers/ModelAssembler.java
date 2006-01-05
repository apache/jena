/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: ModelAssembler.java,v 1.1 2006-01-05 13:40:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.assemblers;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.exceptions.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;

public abstract class ModelAssembler extends AssemblerBase implements Assembler
    {    
    protected abstract Model createModel( Assembler a, Resource root );
    
    public Object create( Assembler a, Resource root )
        { 
        Model m = createModel( a, root );
        Content c = getContent( a, root );
        if (m.supportsTransactions())
            {
            m.begin();
            try { c.fill( m ); m.commit(); }
            catch (Throwable t) { m.abort(); throw new TransactionAbortedException( t ); }
            }
        else
            c.fill( m );
        m.setNsPrefixes( getPrefixMapping( a, root ) );
        return m; 
        }
    
    public static ReificationStyle getReificationStyle( Resource root )
        {
        Resource r = getUniqueResource( root, JA.reificationMode );
        return r == null ? ReificationStyle.Standard : styleFor( root, r );
        }
    
    public static ReificationStyle styleFor( Resource root, Resource r )
        {
        if (r.equals( JA.minimal )) return ReificationStyle.Minimal;
        if (r.equals( JA.standard )) return ReificationStyle.Standard;
        if (r.equals( JA.convenient )) return ReificationStyle.Convenient;
        throw new UnknownStyleException( root, r );
        }

    private PrefixMapping getPrefixMapping( Assembler a, Resource root )
        {
        return PrefixMappingAssembler.getPrefixes
            ( a, root, PrefixMapping.Factory.create() );
        }

    public Model createModel( Resource resource )
        { return (Model) create( resource ); }

    protected Content getContent( Assembler a, Resource root )
        { 
        return new Content( ContentAssembler.loadContent( new ArrayList(), a, root ) );
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