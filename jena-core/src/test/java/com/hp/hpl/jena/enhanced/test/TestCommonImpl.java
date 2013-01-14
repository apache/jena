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
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.*;

class TestCommonImpl extends EnhNode implements TestNode {

    /** Creates new TestCommonImpl */
    TestCommonImpl(Node n, EnhGraph m ) {
        super(n,m);
    }
    
    /**
       We can't return TestModel now, because it clashes with the getModel()
       in RDFNode, which we have to inherit because of the personality tests.
       Fortunately the EnhGraph test set doesn't /need/ getModel, so we give
       it return type Model and throw an exception if it's ever called.
    */
    public Model getModel() 
        { throw new JenaException( "getModel() should not be called in the EnhGraph/Node tests" ); }
    
    public Resource asResource()
        { throw new JenaException( "asResource() should not be called in the EnhGraph/Node tests" ); }
    
    public Literal asLiteral()
        { throw new JenaException( "asLiteral() should not be called in the EnhGraph/Node tests" ); }

    Triple findSubject()
        { return findNode( node, null, null ); }
        
    Triple findPredicate()
        { return findNode( null, node, null ); }
        
    Triple findObject()
        { return findNode( null, null, node ); }
        
    Triple findNode( Node s, Node p, Node o )
        {
        ClosableIterator<Triple> it = enhGraph.asGraph().find( s, p, o );
        try { return it.hasNext() ? it.next() : null; }
        finally { it.close(); }
        }
        
    // Convenience routines, that wrap the generic
    // routines from EnhNode.
    @Override
    public TestSubject asSubject() {
        return asInternal(TestSubject.class);
    }
    
    @Override
    public TestObject asObject() {
        return asInternal(TestObject.class);
    }
    
    @Override
    public TestProperty asProperty() {
        return asInternal(TestProperty.class);
    }

    public RDFNode inModel( Model m )
        {
        
        return null;
        }

    public Object visitWith( RDFVisitor rv )
        {
        
        return null;
        }
    
}
