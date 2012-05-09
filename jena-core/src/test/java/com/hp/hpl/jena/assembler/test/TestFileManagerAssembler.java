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

package com.hp.hpl.jena.assembler.test;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.FileManagerAssembler;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.*;

public class TestFileManagerAssembler extends AssemblerTestBase
    {
    public TestFileManagerAssembler( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return FileManagerAssembler.class; }

    public void testFileModelAssemblerType()
        { testDemandsMinimalType( new FileManagerAssembler(), JA.FileManager );  }
    
    public void testFileManagerVocabulary()
        {
        assertSubclassOf( JA.FileManager, JA.Object );
        assertDomain( JA.HasFileManager, JA.fileManager );
        assertDomain( JA.FileManager, JA.locationMapper );
        assertRange( JA.LocationMapper, JA.locationMapper );
        }
    
    public void testCreatesFileManager() 
        {
        Resource root = resourceInModel( "r rdf:type ja:FileManager" );
        Assembler a = new FileManagerAssembler();
        Object x = a.open( root );
        assertInstanceOf( FileManager.class, x );
        }
    
    public void testCreatesFileManagerWithLocationMapper()
        {
        Resource root = resourceInModel( "f rdf:type ja:FileManager; f ja:locationMapper r" );
        LocationMapper mapper = new LocationMapper();
        Assembler mock = new NamedObjectAssembler( resource( "r" ), mapper );
        Assembler a = new FileManagerAssembler();
        Object x = a.open( mock, root );
        assertInstanceOf( FileManager.class, x );
        assertSame( mapper, ((FileManager) x).getLocationMapper() );
        }
    
    /**
        Can't just test for equality of locators list, since locators don't support
        equality. Weak hack: check that the sizes are the same. TODO improve.
     */
    public void testCreatesFileManagerWIthHandlers()
        {
        Resource root = resourceInModel( "f rdf:type ja:FileManager" );
        Assembler a = new FileManagerAssembler();
        FileManager f = (FileManager) a.open( null, root );
        List<Locator> wanted = IteratorCollection.iteratorToList( standardLocators() );
        List<Locator> obtained = IteratorCollection.iteratorToList( f.locators() );
        assertEquals( wanted.size(), obtained.size() );
        assertEquals( wanted, obtained );
        }

    private Iterator<Locator> standardLocators()
        {
        FileManager fm = new FileManager();
        FileManager.setStdLocators( fm );
        return fm.locators();
        }
    }
