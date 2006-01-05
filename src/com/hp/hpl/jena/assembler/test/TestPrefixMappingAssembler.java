/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestPrefixMappingAssembler.java,v 1.1 2006-01-05 13:40:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.PrefixMappingAssembler;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestPrefixMappingAssembler extends AssemblerTestBase
    {
    public TestPrefixMappingAssembler( String name )
        { super( name ); }

    protected Class getAssemblerClass()
        { return PrefixMappingAssembler.class; }

    public void testPrefixMappingAssemblerType()
        { testDemandsMinimalType( new PrefixMappingAssembler(), JA.PrefixMapping );  }
    
    public void testConstructEmptyPrefixMapping()
        {
        Assembler a = new PrefixMappingAssembler();
        Resource root = resourceInModel( "pm rdf:type ja:PrefixMapping" );
        Object pm = a.create( root );
        assertInstanceOf( PrefixMapping.class, pm );
        }
    
    public void testSimplePrefixMapping()
        {
        PrefixMapping wanted = PrefixMapping.Factory.create()
            .setNsPrefix( "pre", "some:prefix/" );
        Assembler a = new PrefixMappingAssembler();
        Resource root = resourceInModel( "pm rdf:type ja:PrefixMapping; pm ja:prefix 'pre'; pm ja:namespace 'some:prefix/'" );
        PrefixMapping pm = (PrefixMapping) a.create( root );
        assertSamePrefixMapping( wanted, pm );
        }
    
    public void testIncludesSingleMapping()
        {
        PrefixMapping wanted = PrefixMapping.Factory.create()
            .setNsPrefix( "pre", "some:prefix/" );
        Assembler a = new PrefixMappingAssembler();
        Resource root = resourceInModel
            ( "root rdf:type ja:PrefixMapping; root ja:includes pm"
            + "; pm rdf:type ja:PrefixMapping; pm ja:prefix 'pre'; pm ja:namespace 'some:prefix/'" );
        PrefixMapping pm = (PrefixMapping) a.create( root );
        assertSamePrefixMapping( wanted, pm );
        }
    
    public void testIncludesMultipleMappings()
        {
        PrefixMapping wanted = PrefixMapping.Factory.create()
            .setNsPrefix( "p1", "some:prefix/" )
            .setNsPrefix( "p2", "other:prefix/" )
            .setNsPrefix( "p3", "simple:prefix#" );
        Assembler a = new PrefixMappingAssembler();
        Resource root = resourceInModel
            ( "root rdf:type ja:PrefixMapping"
            + "; root ja:includes pm1; pm1 rdf:type ja:PrefixMapping; pm1 ja:prefix 'p1'; pm1 ja:namespace 'some:prefix/'"
            + "; root ja:includes pm2; pm2 rdf:type ja:PrefixMapping; pm2 ja:prefix 'p2'; pm2 ja:namespace 'other:prefix/'"
            + "; root ja:prefix 'p3'; root ja:namespace 'simple:prefix#'" );
        PrefixMapping pm = (PrefixMapping) a.create( root );
        assertSamePrefixMapping( wanted, pm );
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