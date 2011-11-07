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

package com.hp.hpl.jena.assembler.acceptance;

import junit.framework.Assert;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.test.AssemblerTestBase;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.*;

public class TestDatabaseModes extends AssemblerTestBase
    {
    public TestDatabaseModes( String name )
        { super( name ); }

    public void testRDBModelOpenedWhenExists()
        {
        openWith( "square", false, true );
        openWith( "circle", true, true );
        }

    public void testRDBModelCreatedWhenMissing()
        {
        openWith( "line", true, true );
        openWith( "edge", true, false );
        }

    public void testRDBModelFailsIfExists()
        {
        try
            {
            openWith( "triangle", true, false );
            Assert.fail( "should trap existing model" );
            }
        catch (AlreadyExistsException e)
            {
            Assert.assertEquals( "triangle", e.getMessage() );
            }
        try
            {
            openWith( "hex", false, false );
            Assert.fail( "should trap existing model" );
            }
        catch (AlreadyExistsException e)
            {
            Assert.assertEquals( "hex", e.getMessage() );
            }
        }

    public void testRDBModelFailsIfMissing()
        {
        try
            {
            openWith( "parabola", false, true );
            Assert.fail( "should trap missing model" );
            }
        catch (NotFoundException e)
            {
            Assert.assertEquals( "parabola", e.getMessage() );
            }
        try
            {
            openWith( "curve", false, false );
            Assert.fail( "should trap missing model" );
            }
        catch (NotFoundException e)
            {
            Assert.assertEquals( "curve", e.getMessage() );
            }
        }

    private void openWith( String name, boolean mayCreate, boolean mayReuse )
        {
        Assembler.general.openModel
            ( getRoot( name ), new Mode( mayCreate, mayReuse ) ).close();
        }

    private Resource getRoot( String name )
        {
        return resourceInModel( getDescription( name ) );
        }

    private String getDescription( String modelName )
        {
        return ("x rdf:type ja:RDBModel; x ja:modelName 'spoo'; x ja:connection C"
                + "; C ja:dbURLProperty 'jena.db.url'"
                + "; C ja:dbUserProperty 'jena.db.user'"
                + "; C ja:dbPasswordProperty 'jena.db.password'"
                + "; C ja:dbTypeProperty 'jena.db.type'"
                + "; C ja:dbClassProperty 'jena.db.driver'").replaceAll(
                "spoo", modelName );
        }
    }
