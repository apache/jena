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

package org.apache.jena.tdb.store.bulkloader3;

import java.util.HashMap;
import java.util.Map;

import org.openjena.riot.lang.LabelToNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;

public class CustomLabelToNode extends LabelToNode { 

	private static final Logger log = LoggerFactory.getLogger(LabelToNode.class);
	
    public CustomLabelToNode(String runId, String filename) {
        super(new SingleScopePolicy(), new LabelAllocator(runId, filename));
    }
    
    private static class SingleScopePolicy implements ScopePolicy<String, Node, Node> { 
        private Map<String, Node> map = new HashMap<String, Node>() ;
        @Override public Map<String, Node> getScope(Node scope) { return map ; }
        @Override public void clear() { map.clear(); }
    }
    
    private static class LabelAllocator implements Allocator<String, Node> {
        
        private String runId ;
        private String filename ;

        public LabelAllocator (String runId, String filename) {
        	// This is to ensure that blank node allocation policy is constant when subsequent processing happens
            this.runId = runId ;
            this.filename = filename ;
        	log.debug("LabelAllocator({}, {})", runId, filename) ;
        }

        @Override 
        public Node create(String label) {
        	String strLabel = "tdbloader3_" + runId.hashCode() + "_" + filename.hashCode() + "_" + label;
        	log.debug ("create({}) = {}", label, strLabel);
            return Node.createAnon(new AnonId(strLabel)) ;
        }

        @Override public void reset() {}
    };
    
}
