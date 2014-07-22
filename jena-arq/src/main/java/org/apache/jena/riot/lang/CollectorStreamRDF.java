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

package org.apache.jena.riot.lang;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.apache.jena.riot.system.StreamRDF ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/**
 * StreamRDF implementations which store received triples and quads 
 * in a {@link java.util.Collection}. 
 * 
 * The resulting collection can be retrieved via the
 * {@link #getTriples()} and {@link #getQuads()} 
 * methods.
 * 
 * The implementations are suitable for single-threaded parsing, for use with small
 * data or distributed computing frameworks (e.g. Hadoop) where the overhead
 * of creating many threads for a push-pull parser setup is significant.
 */
public class CollectorStreamRDF implements StreamRDF {
	private PrefixMap prefixes = PrefixMapFactory.createForInput();
	private String baseIri;
	
	private List<Triple> triples = new ArrayList<>();
	private List<Quad> quads = new ArrayList<>();
    
    @Override
    public void start() {
        triples.clear() ;
        quads.clear() ;
        prefixes = PrefixMapFactory.createForInput();
    }

	@Override
	public void finish() {}
	
	@Override
	public void triple(Triple triple) { triples.add(triple) ; }
	
	@Override
	public void quad(Quad quad) { quads.add(quad) ; }
	
	@Override
	public void base(String base) {
		this.baseIri = base;
	}
	
	@Override
	public void prefix(String prefix, String iri) {
		prefixes.add(prefix, iri);
	}
	
	public PrefixMap getPrefixes() {
		return prefixes;
	}

	public String getBaseIri() {
		return baseIri;
	}

	public List<Triple> getTriples()        { return triples ; } 
    public List<Quad> getQuads()            { return quads ; } 
}
