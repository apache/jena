/******************************************************************
 * File:        TestBugs.java
 * Created by:  Dave Reynolds
 * Created on:  22-Aug-2003
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestBugs.java,v 1.32 2005-04-08 14:20:48 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import java.io.*;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;

/**
 * Unit tests for reported bugs in the rule system.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.32 $ on $Date: 2005-04-08 14:20:48 $
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
//        suite.addTest(new TestBugs( "xxtest_oh_01" ));
//        return suite;
    }  

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
            ExtendedIterator ci = infgraph.find(null, RDF.Nodes.type, OWL.Class.asNode());
            ci.close();
        } catch (ReasonerException e) {
            foundBadList = true;
        }
        assertTrue("Correctly detected the illegal list", foundBadList);
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
    
    /**
     * Report of functor literals leaking out of inference graphs and raising CCE
     * in iterators.
     */
    public void testFunctorCCE() {
        Model base = ModelFactory.createDefaultModel();
        base.read("file:testing/reasoners/bugs/cceTest.owl");
        InfModel test = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), base);

        boolean b = anyInstancesOfNothing(test);
        ResIterator rIter = test.listSubjects();
        while (rIter.hasNext()) { 
            Resource res = rIter.nextResource();
        }
    }
    
    /** Helper function used in testFunctorCCE */
    private boolean anyInstancesOfNothing(Model model) { 
        boolean hasAny = false;
        try {
            ExtendedIterator it = model.listStatements(null, RDF.type, OWL.Nothing);
            hasAny = it.hasNext();
            it.close();
        } catch (ConversionException x) {
            hasAny = false;
        }
        return hasAny;
    }
    
    /**
     * Report of functor literals leaking out in DAML processing as well.
     */
    public void testDAMLCCE() {
        DAMLModel m = ModelFactory.createDAMLModel();
        m.getDocumentManager().setMetadataSearchPath( "file:etc/ont-policy-test.rdf", true );
        m.read( "file:testing/reasoners/bugs/literalLeak.daml", 
                "http://www.daml.org/2001/03/daml+oil-ex",  null );
        ResIterator rIter = m.listSubjects();
        while (rIter.hasNext()) { 
            Resource res = rIter.nextResource();
            if (res.getNode().isLiteral()) {
                assertTrue("Error in resource " + res, false);
            }
        }
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

    /**
     * This test exposes an apparent problem in the reasoners.  If the input data is 
     * changed from daml:subClassOf to rdfs:subClassOf, the asserts all pass.  As is, 
     * the assert for res has rdf:type cls1 fails.
     */
    public void testSubClass() {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.DAML_MEM_RDFS_INF, null);
        model.getDocumentManager().setMetadataSearchPath( "file:etc/ont-policy-test.rdf", true );
        
        String base = "http://localhost:8080/axis/daml/a.daml#";
        model.read( new ByteArrayInputStream( INPUT_SUBCLASS.getBytes() ), base );
        OntResource res = (OntResource) model.getResource( base+"test").as(OntResource.class);
            
        OntClass cls1 = (OntClass) model.getResource(base+"cls1").as(OntClass.class);
        OntClass cls2 = (OntClass) model.getResource(base+"cls2").as(OntClass.class);
        
        assertTrue( "cls2 should be a super-class of cls1", cls2.hasSuperClass( cls1 ) );
        assertTrue( "res should have rdf:type cls1", res.hasRDFType( cls1 ) );
        assertTrue( "res should have rdf:type cls2", res.hasRDFType( cls2 ) );
    }

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
     * This test exposes an apparent problem in the reasoners.  If the input data is 
     * changed from daml:subPropertyOf to rdfs:subPropertyOf, the asserts all pass.  As is, 
     * the assert for a1 p a0 fails.
     */
    public void testSubProperty() {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.DAML_MEM_RDFS_INF, null);
        
        String base = "urn:x-hp-jena:test#";
        model.read( new ByteArrayInputStream( INPUT_SUBPROPERTY.getBytes() ), base );
        
        OntResource a0 = (OntResource) model.getResource( base+"a0").as(OntResource.class);
        OntResource a1 = (OntResource) model.getResource( base+"a1").as(OntResource.class);
            
        ObjectProperty p = model.getObjectProperty( base+"p" );
        ObjectProperty q = model.getObjectProperty( base+"q" );
        
        assertTrue("subProp relation present", q.hasProperty(RDFS.subPropertyOf, p));
        assertTrue( "a1 q a0", a1.hasProperty( q, a0 ) );   // asserted
        assertTrue( "a1 p a0", a1.hasProperty( p, a0 ) );   // entailed
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
     * Test for a reported bug in delete
     */
    public void testDeleteBug() {
        Model modelo = ModelFactory.createDefaultModel();
        modelo.read("file:testing/reasoners/bugs/deleteBug.owl");
        OntModel modeloOnt = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RULE_INF, modelo );
        Individual indi = modeloOnt.getIndividual("http://decsai.ugr.es/~ontoserver/bacarex2.owl#JS");
        indi.remove();
        ClosableIterator it = modeloOnt.listStatements(indi, null, (RDFNode) null);
        boolean ok = ! it.hasNext();
        it.close();
        assertTrue(ok);
      }
    
    /**
     * Test looping on recursive someValuesFrom.
     */
    public void hiddenTestOWLLoop() {
        Model data = ModelLoader.loadModel("file:testing/reasoners/bugs/loop.owl");
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
        for (Iterator i = infmodel.listStatements(null, RDF.type, C); i.hasNext(); ) {
            Statement st = (Statement)i.next();
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
        Model model = ModelLoader.loadModel("file:testing/reasoners/bugs/rangeBug.owl");
        Model m = ModelFactory.createDefaultModel();
        Reasoner r = ReasonerRegistry.getOWLReasoner();
        InfModel omodel = ModelFactory.createInfModel(r, model);
        String baseuri = "http://decsai.ugr.es/~ontoserver/bacarex2.owl#";
        Resource js = omodel.getResource(baseuri + "JS");
        Resource surname = omodel.getResource(baseuri + "surname");
        Statement s = omodel.createStatement(surname, RDFS.range, OWL.Nothing);
        assertTrue(! omodel.contains(s));
    }
    
    /**
     * Test change of RDF specs to allow plain literals w/o lang and XSD string to be the same.
     */
    public void testLiteralBug() {
        Model model = ModelLoader.loadModel("file:testing/reasoners/bugs/dtValidation.owl");
        Model m = ModelFactory.createDefaultModel();
        Reasoner r = ReasonerRegistry.getOWLReasoner();
        InfModel infmodel = ModelFactory.createInfModel(r, model);
        ValidityReport validity = infmodel.validate();
        assertTrue (validity.isValid());                
    }
    
    /**
     * Test that prototype nodes are now hidden
     */
    public void testHide() {
        String NS = "http://jena.hpl.hp.com/bugs#";
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, null);
        OntClass c = m.createClass(NS + "C");
        OntResource i = m.createIndividual(c);
        Iterator res = m.listStatements(null, RDF.type, c);
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
            Iterator i = m.listOntProperties();
            while (i.hasNext()) {
                Resource r = (Resource)i.next();
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
        Model schema = ModelLoader.loadModel("file:testing/reasoners/bugs/sbug.owl");
        Model data = ModelLoader.loadModel("file:testing/reasoners/bugs/sbug.rdf");
        
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
       Statement s2=m.createStatement(source, prop, "value2");

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
     * Limitation of someValuesFrom applied to datatype properties.
     */
    public void testSomeDatatype() throws IOException {
        String uri = "http://www.daml.org/2001/03/daml+oil-ex-dt";
        String filename = "testing/xsd/daml+oil-ex-dt.xsd";
        TypeMapper tm = TypeMapper.getInstance();
        XSDDatatype.loadUserDefined(uri, new FileReader(filename), null, tm);
        
        Model data = ModelFactory.createDefaultModel();
        data.read("file:testing/reasoners/bugs/userDatatypes.owl");
        InfModel inf = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), data);
        
        String egNS = "http://jena.hpl.hp.com/eg#";
        Resource meR = inf.getResource(egNS + "me");
        Resource TestR = inf.getResource(egNS + "Test");
        assertTrue("somevalues inf for datatypes", inf.contains(meR, RDF.type, TestR));
        
        Resource Test2R = inf.getResource(egNS + "Test2");
        Resource me2R = inf.getResource(egNS + "me2");
        assertTrue("somevalues inf for datatypes", inf.contains(me2R, RDF.type, Test2R));
        assertTrue("somevalues inf for user datatypes", inf.contains(meR, RDF.type, Test2R));
    }
    
    /* Report on jena-dev that DAMLMicroReasoner infmodels don't support daml:subClassOf, etc */
    public void testDAMLMicroReasonerSupports() {
        Reasoner r = DAMLMicroReasonerFactory.theInstance().create( null );
        assertTrue( "Should support daml:subClassOf", r.supportsProperty( DAML_OIL.subClassOf ));
        assertTrue( "Should support daml:subPropertyOf", r.supportsProperty( DAML_OIL.subPropertyOf));
        assertTrue( "Should support daml:domain", r.supportsProperty( DAML_OIL.domain ));
        assertTrue( "Should support daml:range", r.supportsProperty( DAML_OIL.range ));
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
    public void test_oh_01() {
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
        List rules = FBRuleReasoner.loadRules("testing/reasoners/owl/nondetbug.rules");
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
        
        List q = new ArrayList();
        Set seen = new HashSet();
        q.add( r );
        
        while (!q.isEmpty()) {
            OntClass c = (OntClass) q.remove( 0 );
            seen.add( c );
            
            for (Iterator i = c.listSubClasses( true ); i.hasNext(); ) {
                OntClass sub = (OntClass) i.next();
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
        
        for (Iterator k = seen.iterator();  k.hasNext(); ) {
            Resource res = (Resource) k.next();
            boolean isExpected = false;
            for (int j = 0;  !isExpected && j < expected.length; j++) {
                isExpected = expected[j].equals( res );
            }
            if (!isExpected) {
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
    
    // debug assistant
    private void tempList(Model m, Resource s, Property p, RDFNode o) {
        System.out.println("Listing of " + PrintUtil.print(s) + " " + PrintUtil.print(p) + " " + PrintUtil.print(o));
        for (StmtIterator i = m.listStatements(s, p, o); i.hasNext(); ) {
            System.out.println(" - " + i.next());
        }
    }   
    
    public static void main(String[] args) {
        TestBugs test = new TestBugs("test");
//        test.hiddenTestDeleteBug();
    } 
}

/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/