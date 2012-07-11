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

package org.openjena.riot.out;

import java.io.OutputStream ;
import java.io.Writer ;
import java.util.HashMap ;
import java.util.HashSet ;
import java.util.Map ;
import java.util.Set ;

import org.openjena.atlas.lib.Pair ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.riot.system.Prologue ;
import org.openjena.riot.system.SyntaxLabels ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.query.QueryHandler ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

public class RDFJSONWriter {

    public RDFJSONWriter() {}
    
	public static void write (OutputStream out, Graph graph) {
        Prologue prologue = Prologue.create(null, null) ; // (null, graph.getPrefixMapping()) ;
		Sink<Pair<Node, Map<Node, Set<Node>>>> sink = new SinkEntityOutput(out, prologue, SyntaxLabels.createNodeToLabel()) ;
		write ( sink, graph ) ;
	}
	
	public static void write (Writer out, Graph graph) {
        Prologue prologue = Prologue.create(null, null) ; // (null, graph.getPrefixMapping()) ;
		Sink<Pair<Node, Map<Node, Set<Node>>>> sink = new SinkEntityOutput(out, prologue, SyntaxLabels.createNodeToLabel()) ;
		write ( sink, graph ) ;
	}

	private static void write (Sink<Pair<Node, Map<Node, Set<Node>>>> sink, Graph graph) {
		QueryHandler queryHandler = graph.queryHandler() ;
		ExtendedIterator<Node> subjects = queryHandler.subjectsFor(Node.ANY, Node.ANY) ;
		try {
			Map<Node, Set<Node>> predicates = new HashMap<Node, Set<Node>>() ;
			while ( subjects.hasNext() ) {
				Node subject = subjects.next() ;
				ExtendedIterator<Triple> triples = graph.find(subject, Node.ANY, Node.ANY) ;
				try {
					while ( triples.hasNext() ) {
						Triple triple = triples.next() ;
						Node p = triple.getPredicate() ;
						if ( predicates.containsKey(p) ) {
							predicates.get(p).add(triple.getObject()) ; 
						} else {
							Set<Node> objects = new HashSet<Node>() ;
							objects.add(triple.getObject()) ;
							predicates.put(p, objects) ;
						}
					}				
				} finally {
					if ( triples != null ) triples.close() ;
				}
				sink.send(new Pair<Node, Map<Node, Set<Node>>>(subject, predicates)) ;
				predicates.clear() ;
			}			
		} finally {
			if ( subjects != null ) subjects.close() ;
			sink.close() ;
		}
	}
	
}
