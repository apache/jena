package org.apache.jena.osgi.test;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.io.Writer;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class JenaOSGITestImpl implements JenaOSGITest {

	@Override
	public void testJenaCore() throws Exception {
		Model model = ModelFactory.createDefaultModel();
		Resource alice = model.createResource("http://example.com/alice");		
		Property knows = model.createProperty("http://xmlns.com/foaf/0.1/knows");
		Resource bob = model.createResource("http://example.com/bob");
		model.add(model.createStatement(alice, knows, bob));
		
		model.setWriterClassName("someWriter", "com.hp.hpl.jena.rdf.model.impl.NTripleWriter");
		Writer writer = new StringWriter();
		model.write(writer, "someWriter");
		assertEquals("<http://example.com/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.com/bob> .", 
				writer.toString().trim());
	}

	@Override
	public void testJenaArq() throws Exception {
		//fail("Not quite");
	}

	@Override
	public void testJenaIRI()throws Exception {
		IRIFactory iriFactory = IRIFactory.jenaImplementation();
		IRI iri = iriFactory.create("http://example.com/");
		assertEquals("http://example.com/", iri.toASCIIString());
	}

	@Override
	public void testJenaTdb() throws Exception {
		//assertEquals(1,1);
		
	}
}
