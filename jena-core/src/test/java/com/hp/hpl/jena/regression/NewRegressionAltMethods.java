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

package com.hp.hpl.jena.regression;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
//import com.hp.hpl.jena.regression.Regression.*;

public class NewRegressionAltMethods extends NewRegressionContainerMethods
    {
    public NewRegressionAltMethods( String name )
        { super( name ); }

    public static Test suite()
        { return new TestSuite( NewRegressionAltMethods.class ); }

    @Override
    protected Container createContainer()
        { return m.createAlt(); }

    @Override
    protected Resource getContainerType()
        { return RDF.Alt; }
    
    public void testDefaults()
        {
        Alt a = m.createAlt();
        Literal tvLiteral = m.createLiteral( "test 12 string 2" );
//        Resource tvResObj = m.createResource( new ResTestObjF() );
        Bag tvBag = m.createBag();
        Alt tvAlt = m.createAlt();
        Seq tvSeq = m.createSeq();
    //
        Resource tvResource = m.createResource();
        assertEquals( tvLiteral, a.setDefault( tvLiteral ).getDefault() );
        assertEquals( tvLiteral, a.getDefaultLiteral() );
        assertEquals( tvResource, a.setDefault( tvResource ).getDefaultResource() );
        assertEquals( tvByte, a.setDefault( tvByte ).getDefaultByte() );
        assertEquals( tvShort, a.setDefault( tvShort ).getDefaultShort() );
        assertEquals( tvInt, a.setDefault( tvInt ).getDefaultInt() );
        assertEquals( tvLong, a.setDefault( tvLong ).getDefaultLong() );
        assertEquals( tvFloat, a.setDefault( tvFloat ).getDefaultFloat(), fDelta );
        assertEquals( tvDouble, a.setDefault( tvDouble ).getDefaultDouble(), dDelta );
        assertEquals( tvChar, a.setDefault( tvChar ).getDefaultChar() );
        assertEquals( tvString, a.setDefault( tvString ).getDefaultString() );
//        assertEquals( tvResObj, a.setDefault( tvResObj ).getDefaultResource() );
//        assertEquals( tvLitObj, a.setDefault( tvLitObj ).getDefaultObject( new LitTestObjF() ) );
        assertEquals( tvAlt, a.setDefault( tvAlt ).getDefaultAlt() );
        assertEquals( tvBag, a.setDefault( tvBag ).getDefaultBag() );
        assertEquals( tvSeq, a.setDefault( tvSeq ).getDefaultSeq() );
        }
    }
