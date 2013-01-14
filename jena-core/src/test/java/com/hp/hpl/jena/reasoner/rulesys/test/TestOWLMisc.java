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

package com.hp.hpl.jena.reasoner.rulesys.test;

import java.io.StringReader ;
import java.util.Iterator ;

import junit.framework.TestCase ;
import junit.framework.TestSuite ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.ontology.DatatypeProperty ;
import com.hp.hpl.jena.ontology.OntDocumentManager ;
import com.hp.hpl.jena.ontology.OntModel ;
import com.hp.hpl.jena.ontology.OntModelSpec ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.reasoner.Reasoner ;
import com.hp.hpl.jena.reasoner.ReasonerRegistry ;
import com.hp.hpl.jena.reasoner.ValidityReport ;
import com.hp.hpl.jena.reasoner.rulesys.FBRuleInfGraph ;
import com.hp.hpl.jena.util.FileManager ;
import com.hp.hpl.jena.vocabulary.OWL ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;

/**
 * Misc. tests of the OWL rule engine configurations which 
 * have arisen from bug reports or user questions.
 */
public class TestOWLMisc extends TestCase  {

    /**
     * Boilerplate for junit
     */
    public TestOWLMisc( String name ) {
        super( name );
    }

    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestOWLMisc.class );
    }

    @Override
    public void setUp() {
        // ensure the ont doc manager is in a consistent state
        OntDocumentManager.getInstance().reset( true );
    }

    /**
     * Test sameAs/differentFrom interaction
     */
    public void testSameAsDifferentFrom() {
        doTestSameAsDifferentFrom(OntModelSpec.OWL_MEM_MINI_RULE_INF);
        doTestSameAsDifferentFrom(OntModelSpec.OWL_MEM_RULE_INF);
        // Following should not pass since OWL Micro does not claim support for differentFrom
//        doTestSameAsDifferentFrom(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
    }

    /**
     * Test sameAs/differentFrom interaction
     */
    public void doTestSameAsDifferentFrom(OntModelSpec os) {
        String test = "[ a owl:AllDifferent ; owl:distinctMembers ( :limited1 :limited2 :limited3 ) ] .\n" +
                ":limited4 owl:sameAs :limited1 .";
        OntModel inf = ModelFactory.createOntologyModel(os,  modelFromN3(test));
        Resource l4  = inf.getResource(NS + "limited4");
        Resource l2 = inf.getResource(NS + "limited2");
        Resource l3 = inf.getResource(NS + "limited3");
        assertTrue( inf.contains(l4, OWL.differentFrom, l2) );
        assertTrue( inf.contains(l4, OWL.differentFrom, l3) );
    }
    
    private void doTestDatatypeRangeValidation(RDFDatatype over12Type, OntModelSpec spec) {
        String NS = "http://jena.hpl.hp.com/example#";
        OntModel ont = ModelFactory.createOntologyModel(spec);
        Resource over12 = ont.createResource( over12Type.getURI() );
        DatatypeProperty hasValue = ont.createDatatypeProperty(NS + "hasValue");
        hasValue.addRange( over12 );
        
        ont.createResource(NS + "a").addProperty(hasValue, "15", over12Type);
        ont.createResource(NS + "b").addProperty(hasValue, "16", XSDDatatype.XSDinteger);
        ont.createResource(NS + "c").addProperty(hasValue, "10", XSDDatatype.XSDinteger);
        
        ValidityReport validity = ont.validate();
        assertTrue (! validity.isValid()); 
        
        // Check culprit reporting
        ValidityReport.Report report = (validity.getReports().next());
        Triple culprit = (Triple)report.getExtension();
        assertEquals(culprit.getSubject().getURI(), NS + "c");
        assertEquals(culprit.getPredicate(), hasValue.asNode());
    }

    /**
     * Test reported problem with OWL property axioms.
     */
    public void testOWLPropertyAxioms() {
        Model data = ModelFactory.createDefaultModel();
        Resource fp = data.createResource("urn:x-hp:eg/fp");
        Resource ifp = data.createResource("urn:x-hp:eg/ifp");
        Resource tp = data.createResource("urn:x-hp:eg/tp");
        Resource sp = data.createResource("urn:x-hp:eg/sp");
        data.add(fp, RDF.type, OWL.FunctionalProperty);
        data.add(ifp, RDF.type, OWL.InverseFunctionalProperty);
        data.add(tp, RDF.type, OWL.TransitiveProperty);
        data.add(sp, RDF.type, OWL.SymmetricProperty);
        InfModel infmodel = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), data);
        assertTrue("property class axioms", infmodel.contains(fp, RDF.type, RDF.Property));
        assertTrue("property class axioms", infmodel.contains(ifp, RDF.type, RDF.Property));
        assertTrue("property class axioms", infmodel.contains(tp, RDF.type, RDF.Property));
        assertTrue("property class axioms", infmodel.contains(sp, RDF.type, RDF.Property));
        assertTrue("property class axioms", infmodel.contains(ifp, RDF.type, OWL.ObjectProperty));
        assertTrue("property class axioms", infmodel.contains(tp, RDF.type,  OWL.ObjectProperty));
        assertTrue("property class axioms", infmodel.contains(sp, RDF.type,  OWL.ObjectProperty));
    }

    /**
     * Test  problems with inferring equivalence of some simple class definitions,
     * reported by Jeffrey Hau.
     */
    public void testEquivalentClass1() {
        Model base = ModelFactory.createDefaultModel();
        base.read("file:testing/reasoners/bugs/equivalentClassTest.owl");
        InfModel test = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), base);
        String NAMESPACE = "urn:foo#";
        Resource A = test.getResource(NAMESPACE + "A");
        Resource B = test.getResource(NAMESPACE + "B");
        assertTrue("hasValue equiv deduction", test.contains(A, OWL.equivalentClass, B));
    }

    /**
     * Test looping on recursive someValuesFrom.
     */
    public void hiddenTestOWLLoop() {
        Model data = FileManager.get().loadModel("file:testing/reasoners/bugs/loop.owl");
        InfModel infmodel = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), data);
        ((FBRuleInfGraph)infmodel.getGraph()).setTraceOn(true);
        String baseURI = "http://jena.hpl.hp.com/eg#";
        Resource C = infmodel.getResource(baseURI + "C");
        Resource I = infmodel.getResource(baseURI + "i");
        Property R = infmodel.getProperty(baseURI, "R");
//        ((FBRuleInfGraph)infmodel.getGraph()).setTraceOn(true);
        System.out.println("Check that the instance does have an R property");
        Statement s = I.getProperty(R);
        System.out.println(" - " + s);
        System.out.println("And that the type of the R property is C");
        Statement s2 = ((Resource)s.getObject()).getProperty(RDF.type);
        System.out.println(" - " + s2);
        System.out.println("But does that have an R property?");
        Statement s3 = ((Resource)s.getObject()).getProperty(R);
        System.out.println(" - " + s3);
        System.out.println("List all instances of C");
        int count = 0;
        for (Iterator<Statement> i = infmodel.listStatements(null, RDF.type, C); i.hasNext(); ) {
            Statement st = i.next();
            System.out.println(" - " + st);
            count++;
        }
        System.out.println("OK");
//        infmodel.write(System.out);
//        System.out.flush();
    }

    /**
     * Test bug with leaking variables which results in an incorrect "range = Nothing" deduction.
     */
    public void testRangeBug() {
        Model model = FileManager.get().loadModel("file:testing/reasoners/bugs/rangeBug.owl");
//        Model m = ModelFactory.createDefaultModel();
        Reasoner r = ReasonerRegistry.getOWLReasoner();
        InfModel omodel = ModelFactory.createInfModel(r, model);
        String baseuri = "http://decsai.ugr.es/~ontoserver/bacarex2.owl#";
//        Resource js = omodel.getResource(baseuri + "JS");
        Resource surname = omodel.getResource(baseuri + "surname");
        Statement s = omodel.createStatement(surname, RDFS.range, OWL.Nothing);
        assertTrue(! omodel.contains(s));
    }

    /**
     * Test change of RDF specs to allow plain literals w/o lang and XSD string to be the same.
     */
    public void testLiteralBug() {
        Model model = FileManager.get().loadModel("file:testing/reasoners/bugs/dtValidation.owl");
//        Model m = ModelFactory.createDefaultModel();
        Reasoner r = ReasonerRegistry.getOWLReasoner();
        InfModel infmodel = ModelFactory.createInfModel(r, model);
        ValidityReport validity = infmodel.validate();
        assertTrue (validity.isValid());
    }

    /**
     * Report of problems with cardinality v. maxCardinality usage in classification,
     * from Hugh Winkler.
     */
    public void testCardinality1() {
        Model base = ModelFactory.createDefaultModel();
        base.read("file:testing/reasoners/bugs/cardFPTest.owl");
        InfModel test = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), base);
        String NAMESPACE = "urn:foo#";
        Resource aDocument = test.getResource(NAMESPACE + "aDocument");
        Resource documentType = test.getResource(NAMESPACE + "Document");
        assertTrue("Cardinality-based classification", test.contains(aDocument, RDF.type, documentType));
    }

    public static final String NS = "http://jena.hpl.hp.com/example#";
    
    /**
     * Create a model from an N3 string with OWL and EG namespaces defined
     */
    public static Model modelFromN3(String src) {
        String fullSource = "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
        "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
        "@prefix owl:  <http://www.w3.org/2002/07/owl#> .\n" +
        "@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .\n" +
        "@prefix eg: <" + NS + ">.\n" +
        "@prefix : <" + NS + "> .\n"+ src + "\n";
//        System.out.println("Source = " + fullSource);
        Model result = ModelFactory.createDefaultModel();
        result.read(new StringReader(fullSource), "", "N3");
        return result;
    }
    
}
