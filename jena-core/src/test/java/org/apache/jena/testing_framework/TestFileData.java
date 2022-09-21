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

package org.apache.jena.testing_framework;

import static org.junit.Assert.assertTrue ;

import java.io.* ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.graph.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.junit.Test ;

/**
 * Class that produces RDF and TTL data, a Graph and a Model that all contain
 * the same data. This is used for various tests where files are read/written
 * 
 */
public class TestFileData {

	public static final String NS = "uri:urn:x-rdf:test#";

	private static Map<String, String[]> rdfData = new HashMap<>();
	private static Map<String, String[]> ttlData = new HashMap<>();

	static {
		rdfData.put(
				"", // default set must be equiv to TTL default
				new String[] {
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
						"<rdf:RDF",
						"  xmlns:u=\"uri:\"",
						"  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"",
						String.format("  xmlns:ex=\"%s\">", NS),
						String.format(
								"  <rdf:Description rdf:about=\"%ssubject\">",
								NS),
						String.format(
								"    <ex:predicate rdf:resource=\"%sobject\"/>",
								NS), "  </rdf:Description>",
						"  <rdf:Description rdf:about=\"uri:e\">",
						"    <u:p5>verify base works</u:p5>",
						"  </rdf:Description>", "</rdf:RDF>" });
		rdfData.put(
				"realtiveURI", // has relative URI in description rdf:about
				new String[] {
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
						"<rdf:RDF",
						"  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"",
						"  xmlns:ex=\"http://example.com/\">",
						"  <rdf:Description rdf:about=\"http://example.com/subject\">",
						"    <ex:predicate rdf:resource=\"http://example.com/object\"/>",
						"  </rdf:Description>",
						"  <rdf:Description rdf:about=\"e\">",
						"    <ex:p5>verify base works</ex:p5>",
						"  </rdf:Description>", "</rdf:RDF>" });
		rdfData.put(
				"OntologyList0",
				new String[] {
						"<?xml version='1.0' encoding='ISO-8859-1'?>",
						"<!DOCTYPE rdf:RDF [",
						"    <!ENTITY rdf   'http://www.w3.org/1999/02/22-rdf-syntax-ns#'>",
						"    <!ENTITY rdfs  'http://www.w3.org/2000/01/rdf-schema#'>",
						"]>",
						"<rdf:RDF",
						"    xmlns:rdf   =\"&rdf;\"",
						"    xmlns:rdfs  =\"&rdfs;\"",
						String.format("    xml:base    =\"%s\"",
								NS.substring(0, NS.length() - 1)),
						String.format("    xmlns       =\"%s\"", NS), ">",
						"<rdf:Description rdf:ID=\"root\">",
						"   <p rdf:parseType=\"Collection\">", "   </p>",
						"</rdf:Description>", "</rdf:RDF>" });
		rdfData.put(
				"OntologyList1",
				new String[] {
						"<?xml version='1.0' encoding='ISO-8859-1'?>",
						"<!DOCTYPE rdf:RDF [",
						"    <!ENTITY rdf   'http://www.w3.org/1999/02/22-rdf-syntax-ns#'>",
						"    <!ENTITY rdfs  'http://www.w3.org/2000/01/rdf-schema#'>",
						"]>",
						"<rdf:RDF",
						"    xmlns:rdf   =\"&rdf;\"",
						"    xmlns:rdfs  =\"&rdfs;\"",
						String.format("    xml:base    =\"%s\"",
								NS.substring(0, NS.length() - 1)),
						String.format("    xmlns       =\"%s\"", NS), ">",
						"<rdf:Description rdf:ID=\"root\">",
						"   <p rdf:parseType=\"Collection\">",
						"    <rdf:Description rdf:ID=\"a\" />", "   </p>",
						"</rdf:Description>", "</rdf:RDF>" });
		rdfData.put(
				"OntologyList2",
				new String[] {
						"<?xml version='1.0' encoding='ISO-8859-1'?>",
						"<!DOCTYPE rdf:RDF [",
						"    <!ENTITY rdf   'http://www.w3.org/1999/02/22-rdf-syntax-ns#'>",
						"    <!ENTITY rdfs  'http://www.w3.org/2000/01/rdf-schema#'>",
						"]>",
						"<rdf:RDF",
						"    xmlns:rdf   =\"&rdf;\"",
						"    xmlns:rdfs  =\"&rdfs;\"",
						String.format("    xml:base    =\"%s\"",
								NS.substring(0, NS.length() - 1)),
						String.format("    xmlns       =\"%s\"", NS), ">",
						"<rdf:Description rdf:ID=\"root\">",
						"   <p rdf:parseType=\"Collection\">",
						"    <rdf:Description rdf:ID=\"a\" />",
						"    <rdf:Description rdf:ID=\"b\" />", "   </p>",
						"</rdf:Description>", "</rdf:RDF>" });
		rdfData.put(
				"OntologyList3",
				new String[] {
						"<?xml version='1.0' encoding='ISO-8859-1'?>",
						"<!DOCTYPE rdf:RDF [",
						"    <!ENTITY rdf   'http://www.w3.org/1999/02/22-rdf-syntax-ns#'>",
						"    <!ENTITY rdfs  'http://www.w3.org/2000/01/rdf-schema#'>",
						"]>",
						"<rdf:RDF",
						"    xmlns:rdf   =\"&rdf;\"",
						"    xmlns:rdfs  =\"&rdfs;\"",
						String.format("    xml:base    =\"%s\"",
								NS.substring(0, NS.length() - 1)),
						String.format("    xmlns       =\"%s\"", NS), ">",
						"<rdf:Description rdf:ID=\"root\">",
						"   <p rdf:parseType=\"Collection\">",
						"    <rdf:Description rdf:ID=\"a\" />",
						"    <rdf:Description rdf:ID=\"b\" />",
						"    <rdf:Description rdf:ID=\"c\" />", "   </p>",
						"</rdf:Description>", "</rdf:RDF>" });
		rdfData.put(
				"OntologyList4",
				new String[] {
						"<?xml version='1.0' encoding='ISO-8859-1'?>",
						"<!DOCTYPE rdf:RDF [",
						"    <!ENTITY rdf   'http://www.w3.org/1999/02/22-rdf-syntax-ns#'>",
						"    <!ENTITY rdfs  'http://www.w3.org/2000/01/rdf-schema#'>",
						"]>",
						"<rdf:RDF",
						"    xmlns:rdf   =\"&rdf;\"",
						"    xmlns:rdfs  =\"&rdfs;\"",
						String.format("    xml:base    =\"%s\"",
								NS.substring(0, NS.length() - 1)),
						String.format("    xmlns       =\"%s\"", NS), ">",
						"<rdf:Description rdf:ID=\"root\">",
						"   <p rdf:parseType=\"Collection\">",
						"    <rdf:Description rdf:ID=\"a\" />",
						"    <rdf:Description rdf:ID=\"b\" />",
						"    <rdf:Description rdf:ID=\"c\" />",
						"    <rdf:Description rdf:ID=\"d\" />", "   </p>",
						"</rdf:Description>", "</rdf:RDF>" });
		rdfData.put(
				"OntologyList5",
				new String[] {
						"<?xml version='1.0' encoding='ISO-8859-1'?>",
						"<!DOCTYPE rdf:RDF [",
						"    <!ENTITY rdf   'http://www.w3.org/1999/02/22-rdf-syntax-ns#'>",
						"    <!ENTITY rdfs  'http://www.w3.org/2000/01/rdf-schema#'>",
						"]>",
						"<rdf:RDF",
						"    xmlns:rdf   =\"&rdf;\"",
						"    xmlns:rdfs  =\"&rdfs;\"",
						String.format("    xml:base    =\"%s\"",
								NS.substring(0, NS.length() - 1)),
						String.format("    xmlns       =\"%s\"", NS), ">",
						"<rdf:Description rdf:ID=\"root\">",
						"   <p rdf:parseType=\"Collection\">",
						"    <rdf:Description rdf:ID=\"a\" />",
						"    <rdf:Description rdf:ID=\"b\" />",
						"    <rdf:Description rdf:ID=\"c\" />",
						"    <rdf:Description rdf:ID=\"d\" />",
						"    <rdf:Description rdf:ID=\"e\" />", "   </p>",
						"</rdf:Description>", "</rdf:RDF>" });

		ttlData.put(
				"", // default set must be equiv to RDF default and must be
					// parsable as N-TRIPLE
				new String[] {
						String.format("<%ssubject> <%spredicate> <%sobject> .",
								NS, NS, NS), "<e> <p5> \"verify base works\" ." });

	}

	private static String toDataString(String[] lines) {
		String eol = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		for (String l : lines) {
			sb.append(l).append(eol);
		}
		return sb.toString();
	}

	public static Graph getGraph() {
		
		Graph g = Factory.createGraphMem();

		g.add(Triple.create(NodeFactory.createURI("http://example.com/subject"),
				NodeFactory.createURI("http://example.com/predicate"),
				NodeFactory.createURI("http://example.com/object")));

		g.add(Triple.create(NodeFactory.createBlankNode(BlankNodeId.create("a")),
				NodeFactory.createURI("http://example.com/p1"), NodeFactory
						.createBlankNode(BlankNodeId.create("b"))));

		g.add(Triple.create(NodeFactory.createBlankNode(BlankNodeId.create("b")),
				NodeFactory.createURI("http://example.com/p2"), NodeFactory
						.createLiteral("foo")));

		g.add(Triple.create(NodeFactory.createURI("http://example.com/ns/e"),
				NodeFactory.createURI("http://example.com/ns/p5"), NodeFactory
						.createLiteral("verify base works")));

		return g;
	}

	public static Model getModel() {
		return ModelFactory.createModelForGraph(getGraph());
	}

	public static Model populateRDFModel(Model model, String name)
			throws IOException {
		ModelHelper.txnBegin(model);
		model.read(getRDFInput(name), "http://example.com/test/");
		ModelHelper.txnCommit(model);
		return model;
	}

	public static Model getRDFModel(String name) throws IOException {
		return populateRDFModel(ModelFactory.createDefaultModel(), name);
	}

	private static String[] getRDFData(String name) throws IOException {
		String[] data = rdfData.get(name);
		if (data == null) {
			throw new IOException("Can not find RDF data " + name);
		}
		return data;
	}

	public static InputStream getRDFInput(String name) throws IOException {
		return new ByteArrayInputStream(toDataString(getRDFData(name))
				.getBytes());
	}

	public static InputStream getRDFInput() throws IOException {
		return getRDFInput("");
	}

	private static String[] getTTLData(String name) throws IOException {
		String[] data = ttlData.get(name);
		if (data == null) {
			throw new IOException("Can not find TTL data " + name);
		}
		return data;
	}

	public static InputStream getTTLInput(String name) throws IOException {
		return new ByteArrayInputStream(toDataString(getTTLData(name))
				.getBytes());
	}

	public static InputStream getTTLInput() throws IOException {
		return getTTLInput("");
	}

	public static Reader getRDFReader(String name) throws IOException {

		return new StringReader(toDataString(getRDFData(name)));
	}

	public static Reader getRDFReader() throws IOException {

		return getRDFReader("");
	}

	public static Reader getTTLReader() throws IOException {
		return getTTLReader("");
	}

	public static Reader getTTLReader(String name) throws IOException {
		return new StringReader(toDataString(getTTLData(name)));
	}

	public static String getRDFName(String name) throws IOException {
		return createFile(toDataString(getRDFData(name)), ".rdf");
	}

	public static String getRDFName() throws IOException {
		return getRDFName("");
	}

	private static String createFile(String data, String extension)
			throws IOException {
		File f = File.createTempFile("tfd", extension);
		f.deleteOnExit();
		try (FileOutputStream fos = new FileOutputStream(f); ) {
		    // fos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
		    // fos.write(System.getProperty("line.separator").getBytes());
		    fos.write(data.getBytes());
		}
		return f.toURI().toURL().toExternalForm();
	}

	public static String getTTLName() throws IOException {
		return getTTLName("");
	}

	public static String getTTLName(String name) throws IOException {
		return createFile(toDataString(getTTLData(name)), ".ttl");
	}

	@Test
	public void testEquality() throws Exception {
		Model ttl = ModelFactory.createDefaultModel().read(getTTLInput(), NS,
				"TTL");
		Model rdf = ModelFactory.createDefaultModel().read(getRDFInput(), NS,
				"RDF/XML-ABBREV");

		assertTrue(ttl.isIsomorphicWith(rdf));
		assertTrue(rdf.isIsomorphicWith(ttl));
	}

	public static void main(String... argv) throws Exception {
		// //Model model = ModelFactory.createDefaultModel() ;
		// //String x = "<s> <p> 'verify it works' ." ;
		//
		//
		// //Reader sr = getTTLReader();
		// //model.read(sr, "http://example/", "TTL") ;
		// //model.read(sr, "", "TTL") ;
		// //model.read( getRDFInput() );
		// Model ttl = ModelFactory.createDefaultModel().read( getTTLInput(),
		// "", "TTL");
		// Model rdf = ModelFactory.createDefaultModel().read( getRDFInput(),
		// "", "RDF/XML-ABBREV");
		//
		// ttl.write(System.out, "RDF/XML-ABBREV") ;
		// System.out.println("-----") ;
		// // model.setNsPrefix("ex", "http://example/") ;
		// rdf.write(System.out, "N-TRIPLES") ;
		// System.out.println("-----") ;
		// System.out.println( getTTLName() );
		// System.out.println( "ttl iso rdf: "+ttl.isIsomorphicWith(rdf));
		//
		// System.out.println( getRDFName() );
		// System.out.println( "rdf iso ttl: "+rdf.isIsomorphicWith(ttl));

		String[] lines = { "<rdf:RDF",
				"  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">",
				"  <rdf:Description rdf:about=\"e\">",
				"    <p5>verify base works</p5>", "  </rdf:Description>",
				"</rdf:RDF>" };

		String eol = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		for (String l : lines) {
			sb.append(l).append(eol);
		}

		Model model = ModelFactory.createDefaultModel();

		StringReader sr = new StringReader(sb.toString());
		model.read(sr, "http://example/");
		model.write(System.out, "N-TRIPLES");
		System.out.println("-----");
		model.setNsPrefix("ex", "http://example/");
		model.write(System.out, "RDF/XML-ABBREV", "http://another/");
	}

}
