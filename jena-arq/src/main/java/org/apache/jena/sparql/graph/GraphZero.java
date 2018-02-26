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

package org.apache.jena.sparql.graph;

import org.apache.jena.graph.Capabilities;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.AllCapabilities;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;

/** Invariant empty graph.  
 *  @see GraphSink
 */
public class GraphZero extends GraphBase {
    
    public static Graph instance() {
        // It has transaction state do unsafe to share one object on one thread. 
        return new GraphZero();
    }

    private GraphZero() {}
    
    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
        return NullIterator.instance();
    }
    
    private TransactionHandler transactionHandler = new TransactionHandlerNull();
    
    @Override 
    public TransactionHandler getTransactionHandler() {
        return transactionHandler; 
    }
    
    @Override
    protected PrefixMapping createPrefixMapping() { 
        return new PrefixMappingZero() ;
    }
    
    // Choice point: AddDeniedException/DeleteDeniedException or UnsupportedOperationException.
    //
    // AddDeniedException is more access centric, e.g. permissions, 
    // and may be different for different callers.
    //
    // UnsupportedOperationException is the general java "no" for not available ata ll,
    // but is different from the Jena core exceptions.
    @Override
    public void performAdd( Triple t ) { throw new UnsupportedOperationException("add triple"); }
    
    @Override
    public void performDelete( Triple t ) { throw new UnsupportedOperationException("delete triple"); }

    @Override
    public Capabilities getCapabilities() {
        if ( capabilities == null ) {
            capabilities = new AllCapabilities() {
                @Override public boolean addAllowed() { return false; }
                @Override public boolean addAllowed( boolean every ) { return false; } 
                @Override public boolean deleteAllowed() { return false; }
                @Override public boolean deleteAllowed( boolean every ) { return false; } 
            };
        }
        return capabilities;
    }
}
