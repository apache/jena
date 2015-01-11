package org.apache.jena.osgi.test;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.io.Writer;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.iri.impl.IRIFactoryImpl;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.SymmetricProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.Resource;

public class JenaOSGITestImpl implements JenaOSGITest {

	@Override
	public void testJenaCore() throws Exception {
		Model model = ModelFactory.createDefaultModel();
		Resource alice = model.createResource("http://example.com/alice");		
		Property knows = model.createProperty("http://xmlns.com/foaf/0.1/knows");
		Resource bob = model.createResource("http://example.com/bob");
		model.add(model.createStatement(alice, knows, bob));
		
		// Does Model's Class.forName() still work?  
		model.setWriterClassName("someWriter", "com.hp.hpl.jena.rdf.model.impl.NTripleWriter");
		Writer writer = new StringWriter();
		model.write(writer, "someWriter");
		// yes, but only as long as that classname is accessible within jena-osgi bundle		
		assertEquals("<http://example.com/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.com/bob> .", 
				writer.toString().trim());
		
		// Let's also test com.hp.hpl.jena.ontology		
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF);
		ObjectProperty knowsObjProp = ontModel.createObjectProperty(knows.getURI());
		ObjectProperty hasFriend = ontModel.createObjectProperty("http://example.com/has_friend");
		hasFriend.addSuperProperty(knowsObjProp);
		
		Individual aliceIndividual = ontModel.createIndividual(alice);
		Individual bobIndividiual = ontModel.createIndividual(bob);
		ontModel.add(aliceIndividual, hasFriend, bobIndividiual);
		assertTrue(aliceIndividual.hasProperty(knowsObjProp, bobIndividiual));
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
	
		// This should not work within OSGi
		//assertTrue(iriFactory instanceof IRIFactoryImpl);
	}

	@Override
	public void testJenaTdb() throws Exception {
		//assertEquals(1,1);
		
	}
}
