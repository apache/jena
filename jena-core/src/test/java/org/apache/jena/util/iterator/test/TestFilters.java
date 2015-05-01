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

package org.apache.jena.util.iterator.test;

import java.util.*;
import java.util.function.Predicate;

import junit.framework.TestSuite;

import org.apache.jena.rdf.model.test.ModelTestBase ;
import org.apache.jena.util.iterator.* ;

public class TestFilters extends ModelTestBase
    {
    public TestFilters( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestFilters.class ); }

    protected Predicate<String> containsA = o -> contains( o, 'a' );    
    
    public void testFilterIterator()
        {
        Iterator<String> i = iteratorOfStrings( "there's an a in some animals" );
        Iterator<String> it = new FilterIterator<>( containsA, i );
        assertEquals( listOfStrings( "an a animals" ), iteratorToList( it ) );
        }
    
    protected boolean contains( Object o, char ch )
        { return o.toString().indexOf( ch ) > -1; }
    }
