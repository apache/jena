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

package com.hp.hpl.jena.mem.test;

import java.util.*;

import junit.framework.*;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public abstract class TestConcurrentModificationException extends ModelTestBase
    {
    public TestConcurrentModificationException( String name )
        { super( name ); }

    public abstract TripleBunch getBunch();
    
    public static TestSuite suite()
        { 
        TestSuite result = new TestSuite();
        result.addTestSuite( TestArrayBunchCME.class ); 
        result.addTestSuite( TestSetBunchCME.class ); 
        result.addTestSuite( TestHashedBunchCME.class ); 
        return result;
        }

    public static class TestArrayBunchCME extends TestConcurrentModificationException
        {
        public TestArrayBunchCME(String name)
            { super( name ); }

        @Override public TripleBunch getBunch()
            { return new ArrayBunch(); }
        }
    
    public static class TestSetBunchCME extends TestConcurrentModificationException
        {
        public TestSetBunchCME(String name)
            { super( name ); }

        @Override public TripleBunch getBunch()
            { return new SetBunch( new ArrayBunch() ); }
        }
    
    public static class TestHashedBunchCME extends TestConcurrentModificationException
        {
        public TestHashedBunchCME(String name)
            { super( name ); }

        @Override
        public TripleBunch getBunch()
            { return new HashedTripleBunch( new ArrayBunch() ); }
        }

    public void testAddThenNextThrowsCME()
        { 
        TripleBunch b = getBunch();
        b.add( NodeCreateUtils.createTriple( "a P b" ) );
        b.add( NodeCreateUtils.createTriple( "c Q d" ) );
        ExtendedIterator<Triple> it = b.iterator();
        it.next();
        b.add( NodeCreateUtils.createTriple( "change its state" ) );
        try { it.next(); fail( "should have thrown ConcurrentModificationException" ); }
        catch (ConcurrentModificationException e) { pass(); } 
        }

    public void testDeleteThenNextThrowsCME()
        { 
        TripleBunch b = getBunch();
        b.add( NodeCreateUtils.createTriple( "a P b" ) );
        b.add( NodeCreateUtils.createTriple( "c Q d" ) );
        ExtendedIterator<Triple> it = b.iterator();
        it.next();
        b.remove( NodeCreateUtils.createTriple( "a P b" ) );
        try { it.next(); fail( "should have thrown ConcurrentModificationException" ); }
        catch (ConcurrentModificationException e) { pass(); } 
        }
    }
