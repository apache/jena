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

package arq.examples.constructquads;

import java.io.* ;
import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.PrintUtil;

public class ExampleConstructQuads {
	public static void main(String[] args) throws FileNotFoundException, IOException {

		// create testing data :
		// 1) default graph data
		Model model = ModelFactory.createDefaultModel();
		Resource s = model.createResource("http://eg.com/s");
		Property p = model.createProperty("http://eg.com/p");
		Resource o = model.createResource("http://eg.com/o");
		model.add(s, p, o);
		Dataset dataset = DatasetFactory.create(model);
		// 2) named graph data
		Model model1 = ModelFactory.createDefaultModel();
		Resource s1 = model.createResource("http://eg.com/s1");
		Property p1 = model.createProperty("http://eg.com/p1");
		Resource o1 = model.createResource("http://eg.com/o1");
		model1.add(s1, p1, o1);
		dataset.addNamedModel("http://eg.com/g1", model1);
		
		PrintUtil.printOut(dataset.asDatasetGraph().find());

		// construct named graph
		System.out.println("construct named graph:");
		String queryString = "CONSTRUCT { GRAPH ?g {<http://eg.com/s1> <http://eg.com/p1> ?o} } WHERE{ GRAPH ?g {<http://eg.com/s1> <http://eg.com/p1> ?o} }";
		Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		try (QueryExecution qexec = QueryExecutionFactory
				.create(query, dataset)) {
			Iterator<Quad> quads = qexec.execConstructQuads();
			PrintUtil.printOut(quads);
		}
		System.out.println(query);

		// construct default graph 1
		System.out.println("construct default graph 1:");
		queryString = "CONSTRUCT {  {<http://eg.com/s1> <http://eg.com/p1> ?o} } WHERE{ GRAPH ?g {<http://eg.com/s1> <http://eg.com/p1> ?o} }";
		try (QueryExecution qexec = QueryExecutionFactory
				.create(query, dataset)) {
			Iterator<Quad> quads = qexec.execConstructQuads();
			PrintUtil.printOut(quads);
		}
        System.out.println(query);

		// construct default graph 2
		System.out.println("construct default graph 2:");
		queryString = "CONSTRUCT {<http://eg.com/s1> <http://eg.com/p1> ?o}  WHERE{ GRAPH ?g {<http://eg.com/s1> <http://eg.com/p1> ?o} }";
		query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		try (QueryExecution qexec = QueryExecutionFactory
				.create(query, dataset)) {
			Iterator<Quad> quads = qexec.execConstructQuads();
			PrintUtil.printOut(quads);
		}
        System.out.println(query);

		// construct triples
		System.out.println("construct default graph 2:");
		queryString = "CONSTRUCT {<http://eg.com/s1> <http://eg.com/p1> ?o}  WHERE{ GRAPH ?g {<http://eg.com/s1> <http://eg.com/p1> ?o} }";
		query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		try (QueryExecution qexec = QueryExecutionFactory
				.create(query, dataset)) {
			Iterator<Triple> triples = qexec.execConstructTriples();
			PrintUtil.printOut(triples);
		}
        System.out.println(query);

		// construct dataset
		System.out.println("construct dataset:");
		queryString = "CONSTRUCT { GRAPH ?g {<http://eg.com/s1> <http://eg.com/p1> ?o} } WHERE{ GRAPH ?g {<http://eg.com/s1> <http://eg.com/p1> ?o} }";
		query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		try (QueryExecution qexec = QueryExecutionFactory
				.create(query, dataset)) {
			Dataset d = qexec.execConstructDataset();
			RDFDataMgr.write(System.out, d, Lang.TRIG);
		}
        System.out.println(query);

		// short form 1
		System.out.println("short form 1:");
		queryString = "CONSTRUCT WHERE{ GRAPH ?g {<http://eg.com/s1> <http://eg.com/p1> ?o} }";
		query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		try (QueryExecution qexec = QueryExecutionFactory
				.create(query, dataset)) {
			Iterator<Quad> quads = qexec.execConstructQuads();
			PrintUtil.printOut(quads);
		}
		System.out.println(query);

		// short form 2
		System.out.println("short form 2:");
		queryString = "CONSTRUCT WHERE{ <http://eg.com/s> <http://eg.com/p> ?o }";
		query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		try (QueryExecution qexec = QueryExecutionFactory
				.create(query, dataset)) {
			Iterator<Quad> quads = qexec.execConstructQuads();
			PrintUtil.printOut(quads);
		}
		System.out.println(query);

		// short form 3
		System.out.println("short form 3:");
		queryString = "CONSTRUCT WHERE{ <http://eg.com/s> <http://eg.com/p> ?o }";
		query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		try (QueryExecution qexec = QueryExecutionFactory
				.create(query, dataset)) {
			Iterator<Triple> triples = qexec.execConstructTriples();
			PrintUtil.printOut(triples);
		}
		System.out.println(query);

		// short form 4
		System.out.println("short form 4:");
		queryString = "CONSTRUCT WHERE{ {<http://eg.com/s> <http://eg.com/p> ?o} }";
		query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		try (QueryExecution qexec = QueryExecutionFactory
				.create(query, dataset)) {
			Iterator<Quad> quads = qexec.execConstructQuads();
			PrintUtil.printOut(quads);
		}
		System.out.println(query);
		
		// short form 4
		System.out.println("default graph projection:");
		queryString = "CONSTRUCT { {<http://eg.com/s> <http://eg.com/p> ?o} GRAPH ?g {<http://eg.com/s1> <http://eg.com/p1> ?o} } WHERE{ GRAPH ?g {<http://eg.com/s1> <http://eg.com/p1> ?o} }";
		query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		try (QueryExecution qexec = QueryExecutionFactory
				.create(query, dataset)) {
			Iterator<Triple> triples = qexec.execConstructTriples();
			PrintUtil.printOut(triples);
		}
		System.out.println(query);

		// run-construct-quad-test
		System.out.println("run-construct-quad-test:");
		File[] tests = new File("testing/ARQ/Syntax/Syntax-ARQ")
				.listFiles((dir, fname)->{
						if (fname.startsWith("syntax-quad-construct-")) {
							return true;
						}
						return false;
				});
		for (File test : tests) {
			System.out.println("======== File: "+test.getName());
			try (BufferedReader br = new BufferedReader(new FileReader(test))) {
				String line = null;
				while( (line = br.readLine()) != null){
					System.out.println(line);
				}
			}
			System.out.println("==== Output of parse/print file "+ test.getName());
			try {
			    Query q = QueryFactory.read(test.getAbsolutePath(), Syntax.syntaxARQ) ;
			    System.out.println(q);
			} catch (QueryParseException ex) {
			    System.out.println("File "+test.getAbsolutePath()+ " : "+ex.getMessage()) ;
			}
		}
	}
}
