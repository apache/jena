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

package com.hp.hpl.jena.enhanced.test;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * See {@link TestObject} for more detailed documentation.
 */
public class TestObjectImpl extends TestCommonImpl implements TestObject {

    /** The  required field is the factory field, of
     * class Implementation.
     * This tells how to construct a new EnhNode of this typ
     * from a Node. Note that caching has already happened, so
     * there is no point in implementing another cache here.
     */
    public static final Implementation factory = new Implementation() 
        {
        /** This method should probably always just call a constructor.
            Note the constructor can/should be private.
        */
        @Override public EnhNode wrap(Node n,EnhGraph eg) 
            { return new TestObjectImpl(n,eg); }
    
        @Override public boolean canWrap( Node n, EnhGraph eg )
            { return true; }
        };
    
    /** Creates a new instance of TestAllImpl */
    private TestObjectImpl(Node n,EnhGraph eg) {
        super( n, eg );
    }
    
    @Override public <X extends RDFNode> boolean supports( Class<X> t )
        { return t.isInstance( this ) && isObject(); }
        
    @Override
    public boolean isObject() {
        return findObject() != null;
    }
    
    /**
     * The code first checks that the interface is appropriate at this point.
     * This is not obligatory but should be considered.
     * (If the underlying graph has changed for the worse will
     * users prefer an early and unambiguous exception at this point).
     * 
     * @see com.hp.hpl.jena.enhanced.test.TestObject#aSubject()
     */
    @Override
    public TestSubject aSubject() {
        if (!isObject())
            throw new IllegalStateException("Node is not the object of a triple.");
        return enhGraph.getNodeAs(findObject().getSubject(),TestSubject.class);
    }
}
