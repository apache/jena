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
import java.util.* ;

import junit.framework.TestCase ;
import junit.framework.TestSuite ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory ;
import com.hp.hpl.jena.ontology.* ;
import com.hp.hpl.jena.rdf.listeners.StatementListener ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.reasoner.* ;
import com.hp.hpl.jena.reasoner.rulesys.* ;
import com.hp.hpl.jena.reasoner.rulesys.builtins.BaseBuiltin ;
import com.hp.hpl.jena.reasoner.test.TestUtil ;
import com.hp.hpl.jena.util.FileManager ;
import com.hp.hpl.jena.util.PrintUtil ;
import com.hp.hpl.jena.util.iterator.ClosableIterator ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.vocabulary.OWL ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary ;


/**
 * Unit tests for reported bugs in the rule system.
 */
public class TestBugs extends TestCase {

    /**
     * Boilerplate for junit
     */
    public TestBugs( String name ) {
        super( name );
    }

    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestBugs.class );
//        TestSuite suite = new TestSuite();
//        suite.addTest(new TestBugs( "testLayeredValidation" ));
//        return suite;
    }

    @Override
    public void setUp() {
        // ensure the ont doc manager is in a consistent state
        OntDocumentManager.getInstance().reset( true );
    }

    /**
     * Report of NPE during processing on an ontology with a faulty intersection list,
     * from Hugh Winkler.
     */
    public void testIntersectionNPE() {
        Model base = ModelFactory.createDefaultModel();
        base.read("file:testing/reasoners/bugs/bad-intersection.owl");
        boolean foundBadList = false;
        try {
            InfGraph infgraph = ReasonerRegistry.getOWLReasoner().bind(base.getGraph());
            ExtendedIterator<Triple> ci = infgraph.find(null, RDF.Nodes.type, OWL.Class.asNode());
            ci.close();
        } catch (ReasonerException e) {
            foundBadList = true;
        }
        assertTrue("Correctly detected the illegal list", foundBadList);
    }

    /**
     * Report of functor literals leaking out of inference graphs and raising CCE
     * in iterators.
     */
    public void testFunctorCCE() {
        Model base = ModelFactory.createDefaultModel();
        base.read("file:testing/reasoners/bugs/cceTest.owl");
        InfModel test = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), base);

//        boolean b =
            anyInstancesOfNothing(test);
        ResIterator rIter = test.listSubjects();
        while (rIter.hasNext()) {
//            Resource res =
                rIter.nextResource();
        }
    }

    /** Helper function used in testFunctorCCE */
    private boolean anyInstancesOfNothing(Model model) {
        boolean hasAny = false;
        try {
            ExtendedIterator<Statement> it = model.listStatements(null, RDF.type, OWL.Nothing);
            hasAny = it.hasNext();
            it.close();
        } catch (ConversionException x) {
            hasAny = false;
        }
        return hasAny;
    }

    public static final String INPUT_SUBCLASS =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "" +
        "<rdf:RDF" +
        "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" +
        "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"" +
        "    xmlns:daml=\"http://www.daml.org/2001/03/daml+oil#\"" +
        "    xmlns:ex=\"http://localhost:8080/axis/daml/a.daml#\"" +
        "    xml:base=\"http://localhost:8080/axis/daml/a.daml\">" +
        " " +
        "    <daml:Ontology rdf:about=\"\">" +
        "        <daml:imports rdf:resource=\"http://www.daml.org/2001/03/daml+oil\"/>" +
        "    </daml:Ontology>" +
        " " +
        "    <daml:Class rdf:ID=\"cls1\"/>" +
        "    <daml:Class rdf:ID=\"cls2\">" +
        "        <daml:subClassOf rdf:resource=\"#cls1\"/>" +
        "    </daml:Class>" +
        "    <ex:cls2 rdf:ID=\"test\"/>" +
        "</rdf:RDF>";

    public static final String INPUT_SUBPROPERTY =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "" +
        "<rdf:RDF" +
        "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" +
        "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"" +
        "    xmlns:daml=\"http://www.daml.org/2001/03/daml+oil#\"" +
        "    xmlns=\"urn:x-hp-jena:test#\"" +
        "    xml:base=\"urn:x-hp-jena:test\">" +
        " " +
        "    <daml:Ontology rdf:about=\"\">" +
        "        <daml:imports rdf:resource=\"http://www.daml.org/2001/03/daml+oil\"/>" +
        "    </daml:Ontology>" +
        " " +
        "    <daml:Class rdf:ID=\"A\"/>" +
        "" +
        "    <daml:ObjectProperty rdf:ID=\"p\" />" +
        "    <daml:ObjectProperty rdf:ID=\"q\">" +
        "        <daml:subPropertyOf rdf:resource=\"#p\"/>" +
        "    </daml:ObjectProperty>" +
        "" +
        "    <A rdf:ID=\"a0\"/>" +
        "    <A rdf:ID=\"a1\">" +
        "       <q rdf:resource=\"#a0\" />" +
        "    </A>" +
        "</rdf:RDF>";

    /**
     * Test for a reported bug in delete
     */
    public void testDeleteBug() {
        Model modelo = ModelFactory.createDefaultModel();
        modelo.read("file:testing/reasoners/bugs/deleteBug.owl");
        OntModel modeloOnt = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RULE_INF, modelo );
        Individual indi = modeloOnt.getIndividual("http://decsai.ugr.es/~ontoserver/bacarex2.owl#JS");
        indi.remove();
        ClosableIterator<Statement> it = modeloOnt.listStatements(indi, null, (RDFNode) null);
        boolean ok = ! it.hasNext();
        it.close();
        assertTrue(ok);
      }

    /**
     * Test bug caused by caching of deductions models.
     */
    public void testDeteleBug2() {
        Model m = ModelFactory.createDefaultModel();
        String NS = PrintUtil.egNS;
        Resource r = m.createResource(NS + "r");
        Resource A = m.createResource(NS + "A");
        Resource B = m.createResource(NS + "B");
        Statement s = m.createStatement(r, RDF.type, A);
        m.add(s);
        String rules = "(?r rdf:type eg:A) -> (?r rdf:type eg:B).";
        GenericRuleReasoner grr = new GenericRuleReasoner(Rule.parseRules(rules));
        InfModel im = ModelFactory.createInfModel(grr, m);
        assertTrue(im.contains(r, RDF.type, B));
        assertTrue(im.getDeductionsModel().contains(r, RDF.type, B));
        im.remove(s);
        assertFalse(im.contains(r, RDF.type, B));
        assertFalse(im.getDeductionsModel().contains(r, RDF.type, B));
    }

    /**
     * Test that prototype nodes are now hidden
     */
    public void testHide() {
        String NS = "http://jena.hpl.hp.com/bugs#";
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, null);
        OntClass c = m.createClass(NS + "C");
        OntResource i = m.createIndividual(c);
        Iterator<Statement> res = m.listStatements(null, RDF.type, c);
        TestUtil.assertIteratorValues(this, res, new Statement[] {
            m.createStatement(i, RDF.type, c)
        });
    }

    /**
     * Also want to have hidden rb:xsdRange
     */
    public void testHideXSDRange() {
        OntModelSpec[] specs = new OntModelSpec[] {
                OntModelSpec.OWL_MEM_RULE_INF,
                OntModelSpec.OWL_MEM_RDFS_INF,
                OntModelSpec.OWL_MEM_MINI_RULE_INF,
                OntModelSpec.OWL_MEM_MICRO_RULE_INF
        };
        for (int os = 0; os < specs.length; os++) {
            OntModelSpec spec = specs[os];
            OntModel m = ModelFactory.createOntologyModel(spec, null);
            Iterator<OntProperty> i = m.listOntProperties();
            while (i.hasNext()) {
                Resource r = i.next();
                if (r.getURI() != null && r.getURI().startsWith(ReasonerVocabulary.RBNamespace)) {
                    assertTrue("Rubrik internal property leaked out: " + r + "(" + os + ")", false);
                }
            }
        }
    }

    /**
     * Test problem with bindSchema not interacting properly with validation.
     */
    public void testBindSchemaValidate() {
        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
        Model schema = FileManager.get().loadModel("file:testing/reasoners/bugs/sbug.owl");
        Model data = FileManager.get().loadModel("file:testing/reasoners/bugs/sbug.rdf");

        // Union version
        InfModel infu = ModelFactory.createInfModel(reasoner, data.union(schema));
        ValidityReport validity = infu.validate();
        assertTrue( ! validity.isValid());
        // debug print
//        for (Iterator i = validity.getReports(); i.hasNext(); ) {
//            System.out.println(" - " + i.next());
//        }

        // bindSchema version
        InfModel inf = ModelFactory.createInfModel(reasoner.bindSchema(schema), data);
        validity = inf.validate();
        assertTrue( ! validity.isValid());
    }

    /**
     * Delete bug in generic rule reasoner.
     */
    public void testGenericDeleteBug() {
        Model data = ModelFactory.createDefaultModel();
        String NS = "urn:x-hp:eg/";
        Property p = data.createProperty(NS, "p");
        Resource x = data.createResource(NS + "x");
        Resource y = data.createResource(NS + "y");
        Statement sy = data.createStatement(y, p, "foo");
        data.add(sy);
        data.add(x, p, "foo");
//        String rule = "[(?x eg:p ?m) -> (?x eg:same ?x)]";
        String rule = "[(?x eg:p ?m) (?y eg:p ?m) -> (?x eg:same ?y) (?y eg:same ?x)]";
        GenericRuleReasoner reasoner = (GenericRuleReasoner) GenericRuleReasonerFactory.theInstance().create(null);
        reasoner.setMode(GenericRuleReasoner.FORWARD_RETE);
        reasoner.setRules(Rule.parseRules(rule));
        InfModel inf = ModelFactory.createInfModel(reasoner, data);
        TestUtil.assertIteratorLength(inf.listStatements(y, null, (RDFNode)null), 3);
        inf.remove(sy);
        TestUtil.assertIteratorLength(inf.listStatements(y, null, (RDFNode)null), 0);
    }

    /**
     * RETE incremental processing bug.
     */
    public void testRETEInc() {
       String rule = "(?x ?p ?y) -> (?p rdf:type rdf:Property) .";
       Reasoner r = new GenericRuleReasoner(Rule.parseRules(rule));
       InfModel m = ModelFactory.createInfModel(r, ModelFactory.createDefaultModel());

       Resource source = m.createResource("urn:alfie:testResource");
       Property prop   = m.createProperty("urn:alfie:testProperty");
       Statement s1=m.createStatement(source, prop, "value1");
       Statement s2=m.createStatement(source, prop, "value2");

       m.add(s1);
       assertIsProperty(m, prop);
       m.add(s2);
       m.remove(s1);
       assertIsProperty(m, prop);
    }

    /**
     * RETE incremental processing bug.
     */
    public void testRETEDec() {
       String rule = "(?x ?p ?y) -> (?p rdf:type rdf:Property) .";
       Reasoner r = new GenericRuleReasoner(Rule.parseRules(rule));
       InfModel m = ModelFactory.createInfModel(r, ModelFactory.createDefaultModel());

       Resource source = m.createResource("urn:alfie:testResource");
       Property prop   = m.createProperty("urn:alfie:testProperty");
       Statement s1=m.createStatement(source, prop, "value1");
       m.createStatement(source, prop, "value2");

       m.add(prop, RDF.type, RDF.Property);
       m.add(s1);
       m.prepare();
       m.remove(s1);
       assertIsProperty(m, prop);
    }

    private void assertIsProperty(Model m, Property prop) {
        assertTrue(m.contains(prop, RDF.type, RDF.Property));
    }


    /**
     * Bug that exposed prototypes of owl:Thing despite hiding being switched on.
     */
    public void testHideOnOWLThing() {
        Reasoner r = ReasonerRegistry.getOWLReasoner();
        Model data = ModelFactory.createDefaultModel();
        InfModel inf = ModelFactory.createInfModel(r, data);
        StmtIterator things = inf.listStatements(null, RDF.type, OWL.Thing);
        TestUtil.assertIteratorLength(things, 0);
    }

    /**
     * Utility function.
     * Create a model from an N3 string with OWL and EG namespaces defined.
     */
    public static Model modelFromN3(String src) {
        String fullSource = "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
        "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
        "@prefix eg: <http://jena.hpl.hp.com/eg#> .\n" +
                    "@prefix : <#> .\n"+ src + "\n";
        Model result = ModelFactory.createDefaultModel();
        result.read(new StringReader(fullSource), "", "N3");
        return result;
    }

    /** Bug report from Ole Hjalmar - direct subClassOf not reporting correct result with rule reasoner */
    /** TODO: ijd - I temporarily disabled this test: I think we either have to remove it, rewrite it or get a license from Ole */
    public void xxtest_oh_01() {
        String NS = "http://www.idi.ntnu.no/~herje/ja/";
        Resource[] expected = new Resource[] {
                ResourceFactory.createResource( NS+"reiseliv.owl#Reiseliv" ),
                ResourceFactory.createResource( NS+"hotell.owl#Hotell" ),
                ResourceFactory.createResource( NS+"restaurant.owl#Restaurant" ),
                ResourceFactory.createResource( NS+"restaurant.owl#UteRestaurant" ),
                ResourceFactory.createResource( NS+"restaurant.owl#UteBadRestaurant" ),
                ResourceFactory.createResource( NS+"restaurant.owl#UteDoRestaurant" ),
                ResourceFactory.createResource( NS+"restaurant.owl#SkogRestaurant" ),
            };

        test_oh_01scan( OntModelSpec.OWL_MEM, "No inf", expected );
        test_oh_01scan( OntModelSpec.OWL_MEM_MINI_RULE_INF, "Mini rule inf", expected );
        test_oh_01scan( OntModelSpec.OWL_MEM_MICRO_RULE_INF, "Micro rule inf", expected );
        test_oh_01scan( OntModelSpec.OWL_MEM_RULE_INF, "Full rule inf", expected );
    }

    /** Problem with bindSchema and validation rules */
    public void test_der_validation() {
        Model abox = FileManager.get().loadModel("file:testing/reasoners/owl/nondetbug.rdf");
        List<Rule> rules = FBRuleReasoner.loadRules("testing/reasoners/owl/nondetbug.rules");
        GenericRuleReasoner r = new GenericRuleReasoner(rules);
//        r.setTraceOn(true);
        for (int i = 0; i < 10; i++) {
            InfModel im = ModelFactory.createInfModel(r, abox);
            assertTrue("failed on count " + i, im.contains(null, ReasonerVocabulary.RB_VALIDATION_REPORT, (RDFNode)null));
        }
    }

    // Temporary for debug
    private void test_oh_01scan( OntModelSpec s, String prompt, Resource[] expected ) {
        String NS = "http://www.idi.ntnu.no/~herje/ja/reiseliv.owl#";
        OntModel m = ModelFactory.createOntologyModel(s, null);
        m.read( "file:testing/ontology/bugs/test_oh_01.owl");

//        System.out.println( prompt );
        OntClass r = m.getOntClass( NS + "Reiseliv" );

        List<OntClass> q = new ArrayList<>();
        Set<OntClass> seen = new HashSet<>();
        q.add( r );

        while (!q.isEmpty()) {
            OntClass c = q.remove( 0 );
            seen.add( c );

            for (Iterator<OntClass> i = c.listSubClasses( true ); i.hasNext(); ) {
                OntClass sub = i.next();
                if (!seen.contains( sub )) {
                    q.add( sub );
                }
            }

//            System.out.println( "  Seen class " + c );
        }

        // check we got all classes
        int mask = (1 << expected.length) - 1;

        for (int j = 0;  j < expected.length; j++) {
            if (seen.contains( expected[j] )) {
                mask &= ~(1 << j);
            }
            else {
//                System.out.println( "Expected but did not see " + expected[j] );
            }
        }

        for ( OntClass res : seen )
        {
            boolean isExpected = false;
            for ( int j = 0; !isExpected && j < expected.length; j++ )
            {
                isExpected = expected[j].equals( res );
            }
            if ( !isExpected )
            {
//                System.out.println( "Got unexpected result " + res );
            }
        }

        assertEquals( "Some expected results were not seen", 0, mask );
    }

    /**
     * Bug report from David A Bigwood
     */
    public void test_domainInf() {
        // create an OntModel
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, null );
        // populate the model with stuff
        String NS = "http://m3t4.com/ont/#";
        OntClass c1 = m.createClass( NS + "c1" );
        OntClass c2 = m.createClass( NS + "c2" );
        OntClass c3 = m.createClass( NS + "c3" );
        OntProperty p1 = m.createObjectProperty( NS + "p1" );
        // create a union class to contain the union operands
        UnionClass uc = m.createUnionClass(null, null);
        // add an operand
        uc.addOperand( c1 );
        assertEquals( "Size should be 1", 1, uc.getOperands().size() );
        assertTrue( "uc should have c1 as union member", uc.getOperands().contains( c1 ) );
        // add another operand
        uc.addOperand( c2 );
        assertEquals( "Size should be 2", 2, uc.getOperands().size() );
        TestUtil.assertIteratorValues(this, uc.listOperands(), new Object[] { c1, c2 } );
        // add a third operand
        uc.addOperand( c3 );
        assertEquals( "Size should be 3", 3, uc.getOperands().size() );
        TestUtil.assertIteratorValues(this,  uc.listOperands(), new Object[] { c1, c2, c3} );
        // add union class as domain of a property
        p1.addDomain(uc);
    }

    /**
     * Bug report on bad conflict resolution between two non-monotonic rules.
     */
    public void testNonmonotonicCR() {
        String ruleSrc = "(eg:IndA eg:scoreA ?score), sum(?score 40 ?total), noValue(eg:IndA eg:flag_1 'true') -> drop(0), (eg:IndA eg:scoreA ?total), (eg:IndA eg:flag_1 'true')." +
        "(eg:IndA eg:scoreA ?score), sum(?score 33 ?total), noValue(eg:IndA eg:flag_2 'true') -> drop(0), (eg:IndA eg:scoreA ?total), (eg:IndA eg:flag_2 'true').";
        List<Rule> rules = Rule.parseRules(ruleSrc);
        Model data = ModelFactory.createDefaultModel();
        String NS = PrintUtil.egNS;
        Resource i = data.createResource(NS + "IndA");
        Property scoreA = data.createProperty(NS, "scoreA");
        i.addProperty(scoreA, data.createTypedLiteral(100));
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        InfModel inf = ModelFactory.createInfModel(reasoner, data);
        Iterator<RDFNode> values = inf.listObjectsOfProperty(i, scoreA);
        TestUtil.assertIteratorValues(this, values, new Object[] { data.createTypedLiteral(173)});
    }

    /**
     * Bug report - intersection processing does not work incrementally.
     */
    public void testIncrementalIU() {
        OntModel ontmodel = ModelFactory.createOntologyModel(
                OntModelSpec.OWL_MEM_MINI_RULE_INF );
       String homeuri = "http://abc/bcd/";

       Individual ind[] = new Individual[6];
       OntClass classb = ontmodel.createClass(homeuri + "C");
       for (int i = 0; i < 6; i++){
           ind[i] = classb.createIndividual(homeuri + String.valueOf(i));
       }
       Individual subind[] = new Individual[] {ind[0], ind[1], ind[2]};

       EnumeratedClass class1 = ontmodel.createEnumeratedClass(
               homeuri+"C1", ontmodel.createList(subind) );
       EnumeratedClass class2 = ontmodel.createEnumeratedClass(
               homeuri+"C2", ontmodel.createList(ind));

       RDFList list = ontmodel.createList(new RDFNode[] { class1, class2 });
       IntersectionClass classI = ontmodel.createIntersectionClass(null, list);
       UnionClass classU = ontmodel.createUnionClass(null, list);

       // Works with rebind, bug is that it doesn't work without rebind
//       ontmodel.rebind();

       TestUtil.assertIteratorValues(this, classI.listInstances(), subind);
       TestUtil.assertIteratorValues(this, classU.listInstances(), ind);
    }

    /**
     * Fact rules with non-empty bodyies failed to fire.
     */
    public void testFactRules() {
        Model facts = ModelFactory.createDefaultModel();
        String NS = PrintUtil.egNS;
        Property p = facts.createProperty(NS + "p");
        List<Rule> rules = Rule.parseRules("makeTemp(?x) -> (?x, eg:p, eg:z). " +
                "makeTemp(?x) makeTemp(?y) -> (?x, eg:p, ?y) . " +
                "(?x, eg:p, eg:z) -> (?a, eg:p, eg:b). " +
                "-> [ (eg:a eg:p eg:y) <- ]."
                );

        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        InfModel inf = ModelFactory.createInfModel(reasoner, facts);
        inf.prepare();
        TestUtil.assertIteratorLength(inf.listStatements(null, p, (RDFNode)null), 4);
    }

    /**
     * Test chainging rules from axioms which broke while trying to
     * fix about test case.
     */
    public void testFactChainRules() {
        Model facts = ModelFactory.createDefaultModel();
        String NS = PrintUtil.egNS;
        Property mother = facts.createProperty(NS + "mother");
        Resource female = facts.createProperty(NS + "Female");
        mother.addProperty(RDFS.range, female);
        List<Rule> rules = Rule.parseRules(
                "-> tableAll(). \n" +
                "[rdfs6:  (?p rdfs:subPropertyOf ?q), notEqual(?p,?q) -> [ (?a ?q ?b) <- (?a ?p ?b)] ] \n" +
                 "-> (eg:range rdfs:subPropertyOf rdfs:range). \n" +
                 "-> (rdfs:range rdfs:subPropertyOf eg:range). \n" );
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        reasoner.setTransitiveClosureCaching(true);
        InfModel inf = ModelFactory.createInfModel(reasoner, facts);
        Property egRange = inf.createProperty(NS + "range");
        TestUtil.assertIteratorValues(this,
                    inf.listStatements(null, egRange, (RDFNode)null),
                    new Object[] {inf.createStatement(mother, egRange, female)} );
    }

    /**
     * test remove operator in case with empty data.
     */
    public void testEmptyRemove() {
        List<Rule> rules = Rule.parseRules(
                "-> (eg:i eg:prop eg:foo) ." +
                "(?X eg:prop ?V) -> (?X eg:prop2 ?V) ." +
                "(?X eg:prop eg:foo) noValue(?X eg:guard 'done') -> remove(0) (?X eg:guard 'done') ." );
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        InfModel im = ModelFactory.createInfModel(reasoner, ModelFactory.createDefaultModel());
        Resource i = im.createResource(PrintUtil.egNS + "i");
        Property guard = im.createProperty(PrintUtil.egNS + "guard");
        TestUtil.assertIteratorValues(this,
                im.listStatements(), new Object[] {im.createStatement(i, guard, "done")});
    }

    /**
     * test duplicate removal when using pure backward rules
     */
    public void testBackwardDupRemoval() {
        String NS = PrintUtil.egNS;
        Model base = ModelFactory.createDefaultModel();
        Resource i = base.createResource(NS + "i");
        Resource a = base.createResource(NS + "a");
        Property p = base.createProperty(NS, "p");
        Property q = base.createProperty(NS, "q");
        Property r = base.createProperty(NS, "r");
        base.add(i, p, a);
        base.add(i, q, a);
        List<Rule> rules = Rule.parseRules(
                "(eg:i eg:r eg:a) <- (eg:i eg:p eg:a). (eg:i eg:r eg:a) <- (eg:i eg:q eg:a).");
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        reasoner.setMode(GenericRuleReasoner.BACKWARD);
        InfModel im = ModelFactory.createInfModel(reasoner, base);
        TestUtil.assertIteratorLength(im.listStatements(i, r, a), 1);
    }

    /**
     * Test closure of grounded choice points
     */
    public void testGroundClosure() {
        Flag myFlag = new Flag();
        BuiltinRegistry.theRegistry.register(myFlag);
        String NS = "http://ont.com/";
        PrintUtil.registerPrefix("ns", NS);
        String rules =
            "[r1: (ns:a ns:p ns:b) <- (ns:a ns:p ns:a)] " +
            "[r2: (ns:a ns:p ns:b) <- flag()] " +
            "[rt: (?a ns:q ?b) <- (?a ns:p ?b)] ";
        Model m = ModelFactory.createDefaultModel();
        Resource a = m.createResource(NS + "a");
        Resource b = m.createResource(NS + "b");
        Property p = m.createProperty(NS + "p");
        Property q = m.createProperty(NS + "q");
        m.add(a, p, a);
        GenericRuleReasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        InfModel infModel = ModelFactory.createInfModel(reasoner, m);
        assertTrue( infModel.contains(a, q, b) );
        assertTrue( ! myFlag.fired );
    }

    /**
     * Test closure of grounded choice points
     */
    public void testGroundClosure2() {
        Flag myFlag = new Flag();
        BuiltinRegistry.theRegistry.register(myFlag);
        List<Rule> rules = Rule.rulesFromURL("file:testing/reasoners/bugs/groundClosure2.rules");
        GenericRuleReasoner reasoner = new GenericRuleReasoner( rules );
        InfModel inf = ModelFactory.createInfModel(reasoner, ModelFactory.createDefaultModel());

        String NS = "http://jena.hpl.hp.com/example#";
        Resource Phil = inf.getResource(NS + "Phil");
        Resource Paul = inf.getResource(NS + "Paul");
        Property parent = inf.getProperty(NS + "parent");
        assertTrue ( inf.contains(Paul, parent, Phil) );
        assertTrue( ! myFlag.fired );
    }

    /**
     * Test case for a reported CME bug in the transitive reasoner
     */
    public void testCMEInTrans() {
        OntModel model =
            ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
        model.read("file:testing/reasoners/bugs/tgcCMEbug.owl");
    }

    /**
     * Test case for reported problem in detecting cardinality violations
     */
    public void testIndCardValidation() {
        final String NS = "http://dummy#";

        // prepare TBox
        OntModel tBox =  ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        OntClass moleculeClass = tBox.createClass(NS + "Molecule");
        // add value constraint on molecule2atom
        ObjectProperty molecule2atomOntProperty  = tBox.createObjectProperty(NS + "molecule2atom");
        molecule2atomOntProperty.setDomain(moleculeClass);
        molecule2atomOntProperty.setRange(RDF.Bag);
        // add cardinality constraint on molecule2atom
        CardinalityRestriction molecule2atomCardinalityRestriction
                 = tBox.createCardinalityRestriction(null, molecule2atomOntProperty, 1);
        moleculeClass.addSuperClass(molecule2atomCardinalityRestriction);

        // prepare ABox
        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
        reasoner = reasoner.bindSchema(tBox);
        Model    model = ModelFactory.createDefaultModel();
        InfModel aBox  = ModelFactory.createInfModel(reasoner, model);

        // make sure rdfs:member properties are inferred
//        ((FBRuleInfGraph)aBox.getGraph()).addPreprocessingHook(new RDFSCMPPreprocessHook());

        // create an invalid molecule
        Bag bag1 = aBox.createBag();
        Bag bag2 = aBox.createBag();
        bag1.addProperty(OWL.differentFrom, bag2);
        Resource molecule = aBox.createResource();
        molecule.addProperty(molecule2atomOntProperty, bag1);
        molecule.addProperty(molecule2atomOntProperty, bag2);

        // check if model has become invalid
        assertTrue(aBox.contains(molecule, RDF.type, moleculeClass));
        assertFalse(aBox.validate().isValid()); // fails: why?
    }

    /**
     * Listeners on deductions graph should be preserved across rebind operations
     */
    public void testDeductionListener() {
        final String NS = PrintUtil.egNS;

        // Data: (eg:i eg:p 'foo')
        Model base = ModelFactory.createDefaultModel();
        Resource i = base.createResource(NS + "i");
        Property p = base.createProperty(NS + "p");
        i.addProperty(p, "foo");

        // Inf model
        List<Rule> rules = Rule.parseRules( "(?x eg:p ?y) -> (?x eg:q ?y). " );
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        InfModel infModel = ModelFactory.createInfModel(reasoner, base);

        TestListener listener = new TestListener();
        infModel.getDeductionsModel().register(listener);
        infModel.rebind(); infModel.prepare();
        assertEquals("foo", listener.getLastValue());

        i.removeAll(p);
        i.addProperty(p, "bar");
        infModel.rebind(); infModel.prepare();
        assertEquals("bar", listener.getLastValue());
    }

    /**
     * Listener class used in testing. Decects (* eg:q ?l) patterns
     * and notes the last value of ?l seen and returns it as a literal string.
     */
    private class TestListener extends StatementListener {
        final Property Q = ResourceFactory.createProperty(PrintUtil.egNS + "q");
        RDFNode lastValue = null;

        public Object getLastValue() {
            if (lastValue != null && lastValue.isLiteral()) {
                return ((Literal)lastValue).getLexicalForm();
            } else {
                return lastValue;
            }
        }

        @Override
        public void addedStatement( Statement s ) {
            if (s.getPredicate().equals(Q)) {
                lastValue = s.getObject();
            }
        }
    }

    /**
     * Problems with getDeductionsModel not rerunning prepare  at OntModel level
     */
    public void testOntModelGetDeductions() {
        List<Rule> rules = Rule.parseRules( "(?x rdfs:subClassOf ?y) (?i rdf:type ?x) -> (?i rdf:type ?y)." );
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
        spec.setReasoner(reasoner);
        OntModel om = ModelFactory.createOntologyModel(spec);
        OntClass A = om.createClass(PrintUtil.egNS + "A");
        OntClass B = om.createClass(PrintUtil.egNS + "B");
        OntResource i = om.createOntResource(PrintUtil.egNS + "i");
        A.addSuperClass(B);
        i.addRDFType(A);
        Model deductions = om.getDeductionsModel();
        i.removeRDFType(A);
        deductions = om.getDeductionsModel();
        assertFalse("Deductions model updating correctly", deductions.contains(i, RDF.type, B));
    }

    /**
     * Builtin which just records whether it has been called.
     * Used in implementing testGroundClosure.
     */
    private static class Flag extends BaseBuiltin {
        @Override
        public String getName() {  return "flag";  }
        public boolean fired = false;
        @Override
        public boolean bodyCall(Node[] args, int length, RuleContext context) {
            fired = true;
            return true;
        }
    }

    /**
     * Check ability to report literals as well as resources as culprits
     */
    public void testLiteralsInErrorReports() {
        RDFNode culprit = doTestLiteralsInErrorReports("-> (eg:a eg:p 42).  (?X rb:violation error('test', 'arg')) <- (?S eg:p ?X).");
        assertEquals( culprit, ResourceFactory.createTypedLiteral( new Integer(42) ));
        culprit = doTestLiteralsInErrorReports("-> (eg:a eg:p 'foo').  (?X rb:violation error('test', 'arg')) <- (?S eg:p ?X).");
        assertEquals( culprit, ResourceFactory.createPlainLiteral("foo"));
        BuiltinRegistry.theRegistry.register( new SomeTriple() );
        culprit = doTestLiteralsInErrorReports("-> (eg:a eg:p 42).  (?X rb:violation error('test', 'arg')) <- (?S eg:p ?Y), someTriple(?X).");
        assertTrue( culprit.isLiteral() );
        Object val = ((Literal)culprit).getValue();
        assertTrue( val instanceof Triple);
    }

    private RDFNode doTestLiteralsInErrorReports(String rules) {
        GenericRuleReasoner reasoner = new GenericRuleReasoner( Rule.parseRules(rules) );
        InfModel im = ModelFactory.createInfModel(reasoner, ModelFactory.createDefaultModel());
        ValidityReport validity = im.validate();
        assertTrue (! validity.isValid());
        ValidityReport.Report report = (validity.getReports().next());
        assertTrue( report.getExtension() instanceof RDFNode);
        return (RDFNode)report.getExtension();
    }

    /**
     * Builtin which generates an arbitrary Triple to for testing.
     */
    private static class SomeTriple extends BaseBuiltin {
        @Override
        public String getName() {  return "someTriple";  }
        @Override
        public int getArgLength() { return 1; }
        @Override
        public boolean bodyCall(Node[] args, int length, RuleContext context) {
            checkArgs(length, context);
            BindingEnvironment env = context.getEnv();
            Triple t = new Triple( NodeFactory.createAnon(), NodeFactory.createURI("http://jena.hpl.hp.com/example#"), NodeFactory.createAnon());
            Node l = NodeFactory.createLiteral( LiteralLabelFactory.create(t) );
            return env.bind(args[0], l);
        }
    }

    /**
     * Test a problem with the RDFS rule set.
     * Arguably this should be moved to ../test/TestRDFSReasoners but that requires more
     * fiddling with manifest files and declarative test specifications
     */
    public void testRDFSSimple() {
        doTestRDFSSimple(ReasonerVocabulary.RDFS_DEFAULT);
        doTestRDFSSimple(ReasonerVocabulary.RDFS_SIMPLE);
    }

    private void doTestRDFSSimple(String level) {
        Model model = ModelFactory.createDefaultModel();
        String NS = "http://jena.hpl.hp.com/example#";
        Property prop = model.createProperty(NS + "prop");
        model.add(prop, RDF.type, RDF.Property);

        Reasoner reasoner = RDFSRuleReasonerFactory.theInstance().create(null);
        reasoner.setParameter(ReasonerVocabulary.PROPsetRDFSLevel, level);
        InfModel im = ModelFactory.createInfModel(reasoner, model);
        assertTrue( im.contains(prop, RDFS.subPropertyOf, prop) );
    }


    /**
     * Layering one reasoner on another leads to exposed functors which
     * used to trip up validation
     */
    public void testLayeredValidation() {
        Model ont = FileManager.get().loadModel("testing/reasoners/bugs/layeredValidation.owl");
        InfModel infModel =
            ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), ont);
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF,
            infModel);
        ValidityReport validity = model.validate();
        assertTrue(validity.isClean());
    }

    // debug assistant
//    private void tempList(Model m, Resource s, Property p, RDFNode o) {
//        System.out.println("Listing of " + PrintUtil.print(s) + " " + PrintUtil.print(p) + " " + PrintUtil.print(o));
//        for (StmtIterator i = m.listStatements(s, p, o); i.hasNext(); ) {
//            System.out.println(" - " + i.next());
//        }
//    }
    
    /**
     * Potential problem in handling of maxCardinality(0) assertions in the
     * presence of disjointness.
     */
    public void testMaxCard2() {
        doTestmaxCard2(OntModelSpec.OWL_MEM_MINI_RULE_INF);
        doTestmaxCard2(OntModelSpec.OWL_MEM_RULE_INF);
    }
    
    
    private void doTestmaxCard2(OntModelSpec spec) {
        String NS = "http://jena.hpl.hp.com/eg#";
        Model base = FileManager.get().loadModel("testing/reasoners/bugs/terrorism.owl");
        OntModel model = ModelFactory.createOntologyModel(spec, base);
        OntClass event = model.getOntClass(NS + "Event");
        List<OntClass> subclasses = event.listSubClasses().toList();
        assertFalse( subclasses.contains( OWL.Nothing ) );
        assertEquals(3, subclasses.size());
    }

}
