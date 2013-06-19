/**
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

package org.apache.jena.riot.lang ;

import java.util.concurrent.atomic.AtomicLong ;

import org.apache.jena.riot.SysRIOT ;
import org.apache.jena.riot.out.NodeFmtLib ;
import org.apache.jena.riot.out.NodeToLabel ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.rdf.model.AnonId ;

/**
 * Allocate blank nodes according to the label given. 
 * This alloctor reconstructs labels made by
 * {@linkplain NodeToLabel#createBNodeByLabelEncoded()}
 */

public class BlankNodeAllocatorLabelEncoded implements BlankNodeAllocator {
    private AtomicLong counter = new AtomicLong(0) ;

    public BlankNodeAllocatorLabelEncoded() {}

    @Override
    public void reset() {}

    @Override
    public Node alloc(String label) {
        return NodeFactory.createAnon(new AnonId(NodeFmtLib.decodeBNodeLabel(label))) ;
    }

    @Override
    public Node create() {
        String label = SysRIOT.BNodeGenIdPrefix + (counter.getAndIncrement()) ;
        return NodeFactory.createAnon(new AnonId(label)) ;
    }
}
