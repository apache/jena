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
 * @see TestObjectImpl
 */
public class TestSubjectImpl extends TestCommonImpl implements TestSubject {

    public static final Implementation factory = new Implementation() {
    @Override
    public boolean canWrap( Node n, EnhGraph eg )
        { return true; }
    @Override
    public EnhNode wrap(Node n,EnhGraph eg) {
        return new TestSubjectImpl(n,eg);
    }
};
    
    /** Creates a new instance of TestAllImpl */
    private TestSubjectImpl(Node n,EnhGraph eg) {
        super( n, eg );
    }
    
    @Override public <X extends RDFNode> boolean supports( Class<X> t )
        { return t.isInstance( this ) && isSubject(); }
        
    @Override
    public boolean isSubject() {
        return findSubject() != null;
    }
    
    @Override
    public TestProperty aProperty() {
        if (!isSubject())
            throw new IllegalStateException("Node is not the subject of a triple.");
        return enhGraph.getNodeAs(findSubject().getPredicate(),TestProperty.class);
    }
}
