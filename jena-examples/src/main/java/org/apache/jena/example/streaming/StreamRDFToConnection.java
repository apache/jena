/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.example.streaming;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.RDF;

/**
 * Example of how to implement a StreamRDF that caches and writes to 
 * an RDFConnection.
 *
 */
public class StreamRDFToConnection implements StreamRDF {
	private RDFConnection connection;
	private int bufferSize = 1000;
	private Set<Quad> quads = new HashSet<Quad>();
	private Model model = ModelFactory.createMemModelMaker().createFreshModel();
	
	/**
	 * Constructs the StreamRDFToConnection using default 1000 quad buffer size.
	 * @param connection the connection to talk to.
	 */
	public StreamRDFToConnection( RDFConnection connection ) {
		this.connection = connection;
	}
	
	/**
	 * Constructs the StreamRDFToConnection with the specified buffer size
	 * @param connection the connection to talk to.
	 * @param bufferSize the buffersize.
	 */
	public StreamRDFToConnection( RDFConnection connection, int bufferSize ) {
		this.connection = connection;
		this.bufferSize = bufferSize;
	}
	
	/**
	 * See if we should flush the buffer.
	 */
	private void isBufferFull() {
		if (model.size() + quads.size() >= bufferSize)
		{
			flush();
		}
	}
	
	/**
	 * Flushes the buffer to the connection.
	 */
	private void flush() {
		UpdateBuilder builder = new UpdateBuilder();
		builder.addPrefixes( model );
		builder.addInsert( model );
		builder.addInsertQuads( quads );
		connection.begin( TxnType.WRITE );
		connection.update( builder.build() );
		connection.commit();
		model.removeAll();
		quads.clear();
	}
	
	@Override
	public void start() {
		// does nothing.
	}
	@Override
	public void triple(Triple triple) {
		model.add( model.asStatement(triple));
		isBufferFull();
	}
	@Override
	public void quad(Quad quad) {
		quads.add(quad);
		isBufferFull();
	}
	@Override
	public void base(String base) {
		// do nothing
	}
	@Override
	public void prefix(String prefix, String iri) {
		model.setNsPrefix(prefix, iri);
	}
	@Override
	public void finish() {
		flush();
	}
	
	public static void main(String [] args) {
		Dataset dataset = DatasetFactory.create();
		RDFConnection connection = RDFConnectionFactory.connect(dataset);
		StreamRDFToConnection stream = new StreamRDFToConnection( connection );
		
		Resource s = ResourceFactory.createResource( "s" );
		Property p = ResourceFactory.createProperty( "p" );
		RDFNode o = ResourceFactory.createPlainLiteral("OHHHH");
		Resource t = ResourceFactory.createResource( "t" );
		Resource g = ResourceFactory.createResource( "g" );
		Statement stmt1 = new StatementImpl( s, p ,o );
		Statement stmt2 = new StatementImpl( s, RDF.type, t );
		stream.start();
		stream.triple( stmt1.asTriple() );
		stream.quad( new Quad( g.asNode(), stmt2.asTriple()));
		stream.finish();
		
		System.out.println( "Contains model 'g': "+dataset.containsNamedModel("g") );
		Model m = dataset.getDefaultModel();
		System.out.println( "Default model contains <s,p,o>: "+	m.contains( stmt1 ));
		m = dataset.getNamedModel( "g" );
		System.out.println( "model 'g' contains <s,RDF.type,t>: "+	m.contains( stmt2 ));
		
	}
}
