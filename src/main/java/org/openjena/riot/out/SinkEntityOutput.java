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

package org.openjena.riot.out;

import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import org.openjena.atlas.json.io.JSWriter;
import org.openjena.atlas.lib.Pair;
import org.openjena.atlas.lib.Sink;
import org.openjena.riot.system.Prologue;
import org.openjena.riot.system.SyntaxLabels;

import com.hp.hpl.jena.graph.Node;

public class SinkEntityOutput implements Sink<Pair<Node, Map<Node, Set<Node>>>> {

    private Prologue prologue = null ;
    private JSWriter out ;
    private NodeToLabel labelPolicy = null ;
	
    public SinkEntityOutput(OutputStream outs)
    {
        this(outs, null, SyntaxLabels.createNodeToLabel()) ;
    }
    
    public SinkEntityOutput(OutputStream outs, Prologue prologue, NodeToLabel labels)
    {
    	out = new JSWriter(outs) ;
    	setPrologue(prologue) ;
    	setLabelPolicy(labels) ;
    	out.startOutput() ;
    	out.startArray() ;
    }

    public void setPrologue(Prologue prologue)
    {
    	this.prologue = prologue ;
    }

    public void setLabelPolicy(NodeToLabel labels)
    {
    	this.labelPolicy = labels ;
    }

    //@Override
	public void send(Pair<Node, Map<Node, Set<Node>>> item) {
		Node s = item.getLeft() ;
		// out.pair(key, value) ;
		Map<Node, Set<Node>> predicates = item.getRight() ;
		for (Node p : predicates.keySet() ) {
			Set<Node> objects = predicates.get(p) ;
			out.startArray() ;
			for ( Node o : objects ) {
				// out.arrayElement(o) ;
				// TODO
			}
			out.finishArray() ;
		}
	}

    //@Override
	public void flush() {
		out.finishArray() ;
		out.finishOutput();
	}
	
    //@Override
	public void close() {
		flush() ;
	}

}
