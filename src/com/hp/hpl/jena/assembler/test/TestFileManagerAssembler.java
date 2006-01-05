/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestFileManagerAssembler.java,v 1.1 2006-01-05 13:40:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.FileManagerAssembler;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.*;

public class TestFileManagerAssembler extends AssemblerTestBase
    {
    public TestFileManagerAssembler( String name )
        { super( name ); }

    protected Class getAssemblerClass()
        { return FileManagerAssembler.class; }

    public void testFileModelAssemblerType()
        { testDemandsMinimalType( new FileManagerAssembler(), JA.FileManager );  }
    
    public void testFileManagerVocabulary()
        {
        assertSubclassOf( JA.FileManager, JA.Object );
        assertDomain( JA.FileManager, JA.locationMapper );
        assertRange( JA.LocationMapper, JA.locationMapper );
        }
    
    public void testCreatesFileManager() 
        {
        Resource root = resourceInModel( "r rdf:type ja:FileManager" );
        Assembler a = new FileManagerAssembler();
        Object x = a.create( root );
        assertInstanceOf( FileManager.class, x );
        }
    
    public void testCreatesFileManagerWithLocationMapper()
        {
        Resource root = resourceInModel( "f rdf:type ja:FileManager; f ja:locationMapper r" );
        LocationMapper mapper = new LocationMapper();
        Assembler mock = new NamedObjectAssembler( resource( "r" ), mapper );
        Assembler a = new FileManagerAssembler();
        Object x = a.create( mock, root );
        assertInstanceOf( FileManager.class, x );
        assertSame( mapper, ((FileManager) x).getLocationMapper() );
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