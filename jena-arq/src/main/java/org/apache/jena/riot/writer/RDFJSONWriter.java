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

package org.apache.jena.riot.writer;

import java.io.OutputStream ;
import java.io.Writer ;
import java.util.HashMap ;
import java.util.HashSet ;
import java.util.Map ;
import java.util.Set ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.Prologue ;
import org.apache.jena.riot.system.SyntaxLabels ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.GraphUtil ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

public class RDFJSONWriter extends WriterGraphRIOTBase
{
    public RDFJSONWriter() {}
    
	public static void output(OutputStream out, Graph graph) {
        Prologue prologue = Prologue.create(null, null) ; // (null, graph.getPrefixMapping()) ;
		Sink<Pair<Node, Map<Node, Set<Node>>>> sink = new SinkEntityOutput(out, prologue, SyntaxLabels.createNodeToLabel()) ;
		output( sink, graph ) ;
	}
	
	public static void output(Writer out, Graph graph) {
        Prologue prologue = Prologue.create(null, null) ; // (null, graph.getPrefixMapping()) ;
		Sink<Pair<Node, Map<Node, Set<Node>>>> sink = new SinkEntityOutput(out, prologue, SyntaxLabels.createNodeToLabel()) ;
		output( sink, graph ) ;
	}

	private static void output(Sink<Pair<Node, Map<Node, Set<Node>>>> sink, Graph graph) {
		ExtendedIterator<Node> subjects = GraphUtil.listSubjects(graph, Node.ANY, Node.ANY) ;
		try {
			Map<Node, Set<Node>> predicates = new HashMap<>() ;
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
							Set<Node> objects = new HashSet<>() ;
							objects.add(triple.getObject()) ;
							predicates.put(p, objects) ;
						}
					}				
				} finally {
					if ( triples != null ) triples.close() ;
				}
				sink.send(new Pair<>(subject, predicates)) ;
				predicates.clear() ;
			}			
		} finally {
			if ( subjects != null ) subjects.close() ;
			sink.close() ;
		}
	}

    @Override
    public Lang getLang()
    {
        return Lang.RDFJSON ;
    }

    @Override
    public void write(Writer out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
    {
        output(out, graph) ;
    }

    @Override
    public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
    {
        output(out, graph) ;
    }
	
}
