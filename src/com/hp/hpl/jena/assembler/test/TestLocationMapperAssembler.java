/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestLocationMapperAssembler.java,v 1.1 2006-01-05 13:40:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.test;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.LocationMapperAssembler;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.vocabulary.LocationMappingVocab;

public class TestLocationMapperAssembler extends AssemblerTestBase
    {
    public TestLocationMapperAssembler( String name )
        { super( name ); }

    protected Class getAssemblerClass()
        { return LocationMapperAssembler.class; }

    public void testLocationMapperAssemblerType()
        { testDemandsMinimalType( new LocationMapperAssembler(), JA.LocationMapper );  }
    
    public void testLocationMapperVocabulary()
        {
        assertSubclassOf( JA.LocationMapper, JA.Object );
        assertDomain( JA.LocationMapper, LocationMappingVocab.mapping );
        }
    
    public void testCreatesLocationMapper()
        {
        Resource root = resourceInModel( "r rdf:type ja:LocationMapper" );
        Assembler a = new LocationMapperAssembler();
        Object x = a.create( root );
        assertInstanceOf( LocationMapper.class, x );
        }
    
    public void testCreatesWithCorrectContent()
        { // TODO should really have some mroe of these
        Resource root = resourceInModel( "r rdf:type ja:LocationMapper; r lm:mapping _m; _m lm:name 'alpha'; _m lm:altName 'beta'" );
        Assembler a = new LocationMapperAssembler();
        Object x = a.create( root );
        assertInstanceOf( LocationMapper.class, x );
        assertEqualMaps( new LocationMapper( root.getModel() ), (LocationMapper) x );
        }

    private void assertEqualMaps( LocationMapper expected, LocationMapper got )
        {
        Set eAltEntryKeys = IteratorCollection.iteratorToSet( expected.listAltEntries() );
        Set gAltEntryKeys = IteratorCollection.iteratorToSet( got.listAltEntries() );
        Set eAltPrefixKeys = IteratorCollection.iteratorToSet( expected.listAltPrefixes() );
        Set gAltPrefixKeys = IteratorCollection.iteratorToSet( got.listAltPrefixes() );
        assertEquals( "altEntry keys dhould be equal", eAltEntryKeys, gAltEntryKeys );
        assertEquals( "prefixEntry keys should be equal", eAltPrefixKeys, gAltPrefixKeys );
        for (Iterator altKeys = eAltEntryKeys.iterator(); altKeys.hasNext();)
            {
            String key = (String) altKeys.next();
            assertEquals( "alt entrys should be equal", expected.getAltEntry( key ), got.getAltEntry( key ) );
            }        
        for (Iterator preKeys = eAltPrefixKeys.iterator(); preKeys.hasNext();)
                {
                String key = (String) preKeys.next();
                assertEquals( "prefix entiries should be equal", expected.getAltPrefix( key ), got.getAltPrefix( key ) );
                }
        }
    }


/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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