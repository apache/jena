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

package org.apache.jena.dboe.trans.bplustree;

import java.util.ArrayList ;
import java.util.List ;
import java.util.stream.Collectors ;

import org.apache.jena.atlas.lib.InternalErrorException ;

public class AccessPath {
    static class AccessStep {
        final BPTreeNode node ;
        final int idx ;
        final BPTreePage page ;
        AccessStep(BPTreeNode node, int idx, BPTreePage page) {
            this.node = node ;
            this.idx = idx ;
            this.page = page ;
        }
        
        @Override 
        public String toString() {
            return "("+node.label()+", "+idx+")->"+page.getId() ;
        }
    }
    
    private final BPTreeNode root ;
    private List<AccessStep> traversed = new ArrayList<>() ;
    
    public AccessPath(BPTreeNode root) {
        this.root = root ;
    }
    
    public void add(BPTreeNode node, int idx, BPTreePage page) {
        traversed.add(new AccessStep(node, idx, page)) ;
    }
    
    public void reset(BPTreeNode node, int idx, BPTreePage page) {
        AccessStep s = traversed.remove(traversed.size()-1) ;
        AccessStep s2 = new AccessStep(node, idx, page) ;
        if ( s.node != s2.node )
            throw new InternalErrorException("Bad attempt to reset: "+this+" with "+s2) ; 
        traversed.add(new AccessStep(node, idx, page)) ;
    }

    public List<AccessStep> getPath() { return traversed ; }
    
    @Override
    public String toString() {
        return traversed.stream().map(x-> x.toString()).collect(Collectors.toList()).toString() ;
    }
}

