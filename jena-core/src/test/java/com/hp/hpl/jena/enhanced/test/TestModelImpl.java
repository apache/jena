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
import com.hp.hpl.jena.util.iterator.*;

public class TestModelImpl extends EnhGraph implements TestModel {
    
    /** Creates a new instance of TestModelImpl */
    public TestModelImpl(Graph g, Personality<RDFNode> p) {
        super(g,p);
    }
    private Triple aTriple() 
        {
        ClosableIterator<Triple> it = null;
        try 
            {
            it = graph.find( null, null, null );
            return it.hasNext() ? it.next() : null;
            }
        finally 
            { if (it != null) it.close(); }
        }
        
    @Override
    public TestObject anObject() {
        return getNodeAs(aTriple().getObject(),TestObject.class);
    }
    
    @Override
    public TestProperty aProperty() {
        return getNodeAs(aTriple().getPredicate(),TestProperty.class);
    }
    
    @Override
    public TestSubject aSubject() {
        return getNodeAs(aTriple().getSubject(),TestSubject.class);
    }
    
}
