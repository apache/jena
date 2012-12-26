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

package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.graph.*;
import junit.framework.*;

/**
    Test graph capabilities.
*/
public class TestCapabilities extends GraphTestBase
    {
    protected final class AllFalse implements Capabilities
        {
        @Override
        public boolean sizeAccurate()
            { return false; }

        @Override
        public boolean addAllowed()
            { return false; }

        @Override
        public boolean addAllowed( boolean everyTriple )
            { return false; }

        @Override
        public boolean deleteAllowed()
            { return false; }

        @Override
        public boolean deleteAllowed( boolean everyTriple )
            { return false; }

        @Override
        public boolean iteratorRemoveAllowed()
            { return false; }

        @Override
        public boolean canBeEmpty()
            { return false; }

        @Override
        public boolean findContractSafe()
            { return false; }

        @Override
        public boolean handlesLiteralTyping()
            { return false; }
        }

    public TestCapabilities( String name )
        { super( name ); }
        
    public static TestSuite suite()
        { return new TestSuite( TestCapabilities.class ); }   

    /**
        pending on use-cases.
    */
    public void testTheyreThere()
        {
        Graph g = Factory.createDefaultGraph();
        g.getCapabilities();
        }
    
    public void testCanConstruct()
        {
        Capabilities c = new AllFalse();
        }
    
    public void testCanAccess()
        {
        Capabilities c = new AllFalse();
        boolean b = false;
        b = c.addAllowed();
        b = c.addAllowed( true );
        b = c.canBeEmpty();
        b = c.deleteAllowed();
        b = c.deleteAllowed( false );
        b = c.sizeAccurate();
        b = c.iteratorRemoveAllowed();
        b = c.findContractSafe();
        b = c.handlesLiteralTyping();
        }
    }
