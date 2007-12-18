/*
 	(c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: ModelAssembler.java,v 1.8 2007-12-18 16:01:31 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.assemblers;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.exceptions.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.vocabulary.RDF;

public abstract class ModelAssembler extends AssemblerBase implements Assembler
    {    
    protected abstract Model openModel( Assembler a, Resource root, Mode mode );
    
    public Object open( Assembler a, Resource root, Mode mode )
        { 
        Model m = openModel( a, root, mode );
        Content c = getContent( a, root );
        addContent( root, m, c );
        m.setNsPrefixes( getPrefixMapping( a, root ) );
        return m; 
        }

    private void addContent( Resource root, Model m, Content c )
        {
        if (m.supportsTransactions())
            {
            m.begin();
            try { c.fill( m ); m.commit(); }
            catch (Throwable t) { m.abort(); throw new TransactionAbortedException( root, t ); }
            }
        else
            c.fill( m );
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

    public Model openModel( Resource root, Mode mode )
        { return (Model) open( this, root, mode ); }

    protected Content getContent( Assembler a, Resource root )
        {
        final Resource newRoot = oneLevelClone( root );
        final Model fragment = newRoot.getModel();
        return fragment.isEmpty() ? new Content() : (Content) a.open( a, completedClone( root, newRoot, fragment ) );
        }

    private Resource completedClone( Resource root, Resource newRoot, Model fragment )
        {
        Model typed = fragment.add( newRoot, RDF.type, JA.Content );
        return (Resource) newRoot.inModel(  ModelFactory.createUnion( root.getModel(), typed ) );
        }

    private Resource oneLevelClone( Resource root )
        {
        Model partialCopy = ModelFactory.createDefaultModel();
        Resource result = partialCopy.createResource();
        Map1 replace = replaceSubjectMap( partialCopy, result );
        for (Iterator it = ContentAssembler.contentProperties.iterator(); it.hasNext();)
            partialCopy.add( copyProperties( root, replace, (Property) it.next() ) );
        return result;
        }

    private List copyProperties( Resource root, Map1 replace, Property property )
        { return root.listProperties( property  ).mapWith( replace ).toList(); }

    private Map1 replaceSubjectMap( final Model inModel, final Resource newSubject )
        {
        Map1 replace = new Map1() 
            {
            public Object map1( Object o )
                { 
                Statement s = (Statement) o;
                return inModel.createStatement( newSubject, s.getPredicate(), s.getObject() );
                }
            };
        return replace;
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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