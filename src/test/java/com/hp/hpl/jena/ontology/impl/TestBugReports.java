/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian_dickinson@users.sourceforge.net
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            16-Jun-2003
 * Filename           $RCSfile: TestBugReports.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2010-01-11 09:17:06 $
 *               by   $Author: chris-dollin $
 *
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;

// Imports
///////////////
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.SimpleGraphMaker;
import com.hp.hpl.jena.graph.impl.SimpleTransactionHandler;
import com.hp.hpl.jena.graph.query.SimpleQueryHandler;
import com.hp.hpl.jena.mem.faster.GraphMemFasterQueryHandler;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelMakerImpl;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.shared.ClosedException;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.DAML_OIL;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * <p>
 * Unit tests that are derived from user bug reports
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a  href="mailto:ian_dickinson@users.sourceforge.net" >
 *         email</a>)
 * @version CVS $Id: TestBugReports.java,v 1.23 2003/11/20 17:53:10
 *          ian_dickinson Exp $
 */
public class TestBugReports
    extends TestCase
{
    // Constants
    //////////////////////////////////

    public static String NS = "http://example.org/test#";

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    public TestBugReports(String name) {
        super(name);
    }

    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    @Override
    public void setUp() {
        // ensure the ont doc manager is in a consistent state
        OntDocumentManager.getInstance().reset( true );
        OntDocumentManager.getInstance().setMetadataSearchPath( "file:etc/ont-policy.rdf", false );
    }


    /** Bug report by Danah Nada - listIndividuals returning too many results */
    public void test_dn_0() {
        OntModel schema = ModelFactory.createOntologyModel( OntModelSpec.OWL_LITE_MEM_RULES_INF, null );

        schema.read( "file:doc/inference/data/owlDemoSchema.xml", null );

        int count = 0;
        for (Iterator<Individual> i = schema.listIndividuals(); i.hasNext(); ) {
            //Resource r = (Resource) i.next();
            i.next();
            count++;
            /* Debugging * /
            for (StmtIterator j = r.listProperties(RDF.type); j.hasNext(); ) {
                System.out.println( "ind - " + r + " rdf:type = " + j.nextStatement().getObject() );
            }
            System.out.println("----------"); /**/
        }

        assertEquals( "Expecting 6 individuals", 6, count );
    }


    /* Bug report by Danah Nada - duplicate elements in property domain */
    public void test_dn_01() {
        // direct reading for the model method 1
        OntModel m0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM_RULE_INF, null );
        m0.read( "file:testing/ontology/bugs/test_hk_07B.owl" );

        OntProperty p0 = m0.getOntProperty( "file:testing/ontology/bugs/test_hk_07B.owl#PropB" );
        int count = 0;
        for (Iterator<? extends OntResource> i = p0.listDomain(); i.hasNext();) {
            count++;
            i.next();
        }
        assertEquals( 3, count );

        // repeat test - thus using previously cached model for import

        OntModel m1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM_RULE_INF, null );
        m1.read( "file:testing/ontology/bugs/test_hk_07B.owl" );

        OntProperty p1 = m1.getOntProperty( "file:testing/ontology/bugs/test_hk_07B.owl#PropB" );
        count = 0;
        for (Iterator<? extends OntResource> i = p1.listDomain(); i.hasNext();) {
            count++;
            i.next();
        }
        assertEquals( 3, count );
    }

    /** Bug report by Danah Nada - cannot remove import */
    public void test_dn_02() {
        OntModel mymod = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
        mymod.read( "file:testing/ontology/testImport3/a.owl" );

        assertEquals( "Graph count..", 2, mymod.getSubGraphs().size() );

//        for (Iterator it = mymod.listImportedModels(); it.hasNext();) {
        for (Iterator<OntModel> it = mymod.listSubModels(); it.hasNext();) {
                mymod.removeSubModel( it.next() );
        }

        assertEquals( "Graph count..", 0, mymod.getSubGraphs().size() );
    }


    /**
     * Bug report by Mariano Rico Almod???var [Mariano.Rico@uam.es] on June 16th.
     * Said to raise exception.
     */
    public void test_mra_01() {
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.DAML_MEM, null, null);
        String myDicURI = "http://somewhere/myDictionaries/1.0#";
        String damlURI = "http://www.daml.org/2001/03/daml+oil#";
        m.setNsPrefix("DAML", damlURI);

        String c1_uri = myDicURI + "C1";
        OntClass c1 = m.createClass(c1_uri);

        DatatypeProperty p1 = m.createDatatypeProperty(myDicURI + "P1");
        p1.setDomain(c1);

        ByteArrayOutputStream strOut = new ByteArrayOutputStream();

        m.write(strOut, "RDF/XML-ABBREV", myDicURI);
        //m.write(System.out,"RDF/XML-ABBREV", myDicURI);

    }

    /**
     * Bug report from Holger Knublauch on July 25th 2003. Cannot convert
     * owl:Class to an OntClass
     */
    public void test_hk_01() {
        // synthesise a mini-document
        String base = "http://jena.hpl.hp.com/test#";
        String doc =
            "<rdf:RDF"
                + "   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
                + "   xmlns:owl=\"http://www.w3.org/2002/07/owl#\">"
                + "  <owl:Ontology rdf:about=\"\">"
                + "    <owl:imports rdf:resource=\"http://www.w3.org/2002/07/owl\" />"
                + "  </owl:Ontology>"
                + "</rdf:RDF>";

        // read in the base ontology, which includes the owl language
        // definition
        // note OWL_MEM => no reasoner is used
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        m.getDocumentManager().setMetadataSearchPath( "file:etc/ont-policy-test.rdf", true );
        m.read(new ByteArrayInputStream(doc.getBytes()), base);

        // we need a resource corresponding to OWL Class but in m
        Resource owlClassRes = m.getResource(OWL.Class.getURI());

        // now can we see this as an OntClass?
        OntClass c = owlClassRes.as(OntClass.class);
        assertNotNull("OntClass c should not be null", c);

        //(OntClass) (ontModel.getProfile().CLASS()).as(OntClass.class);

    }

    /**
     * Bug report from Hoger Knublauch on Aug 19th 2003. NPE when setting all
     * distinct members
     */
    public void test_hk_02() {
        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
        spec.setReasoner(null);
        OntModel ontModel = ModelFactory.createOntologyModel(spec, null); // ProfileRegistry.OWL_LANG);
        ontModel.createAllDifferent();
        assertTrue(ontModel.listAllDifferent().hasNext());
        AllDifferent allDifferent = ontModel.listAllDifferent().next();
        //allDifferent.setDistinct(ontModel.createList());
        assertFalse(allDifferent.listDistinctMembers().hasNext());
    }

    /** Bug report from Holger Knublauch on Aug 19th, 2003. Initialisation error */
    public void test_hk_03() {
        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
        spec.setReasoner(null);
        OntModel ontModel = ModelFactory.createOntologyModel(spec, null);
        OntProperty property = ontModel.createObjectProperty("http://www.aldi.de#property");
        /* MinCardinalityRestriction testClass = */
        ontModel.createMinCardinalityRestriction(null, property, 42);

    }

    /**
     * Bug report from Holger Knublauch on Aug 19th, 2003. Document manager alt
     * mechanism breaks relative name translation
     */
    public void test_hk_04() {
        OntModel m = ModelFactory.createOntologyModel();
        m.getDocumentManager().addAltEntry(
            "http://jena.hpl.hp.com/testing/ontology/relativenames",
            "file:testing/ontology/relativenames.rdf");

        m.read("http://jena.hpl.hp.com/testing/ontology/relativenames");
        assertTrue(
            "#A should be a class",
            m.getResource("http://jena.hpl.hp.com/testing/ontology/relativenames#A").canAs(OntClass.class));
        assertFalse(
            "file: #A should not be a class",
            m.getResource("file:testing/ontology/relativenames.rdf#A").canAs(OntClass.class));
    }

    /** Bug report from Holger Knublach: not all elements of a union are removed */
    public void test_hk_05() {
        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
        spec.setReasoner(null);
        OntModel ontModel = ModelFactory.createOntologyModel(spec, null);
        String ns = "http://foo.bar/fu#";
        OntClass a = ontModel.createClass(ns + "A");
        OntClass b = ontModel.createClass(ns + "B");

        int oldCount = getStatementCount(ontModel);

        RDFList members = ontModel.createList(new RDFNode[] { a, b });
        IntersectionClass intersectionClass = ontModel.createIntersectionClass(null, members);
        intersectionClass.remove();

        assertEquals("Before and after statement counts are different", oldCount, getStatementCount(ontModel));
    }

    /**
     * Bug report from Holger Knublach: moving between ontology models - comes
     * down to a test for a resource being in the base model
     */
    public void test_hk_06() throws Exception {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        ontModel.read("file:testing/ontology/bugs/test_hk_06/a.owl");

        String NSa = "http://jena.hpl.hp.com/2003/03/testont/a#";
        String NSb = "http://jena.hpl.hp.com/2003/03/testont/b#";

        OntClass A = ontModel.getOntClass(NSa + "A");
        assertTrue("class A should be in the base model", ontModel.isInBaseModel(A));

        OntClass B = ontModel.getOntClass(NSb + "B");
        assertFalse("class B should not be in the base model", ontModel.isInBaseModel(B));

        assertTrue(
            "A rdf:type owl:Class should be in the base model",
            ontModel.isInBaseModel(ontModel.createStatement(A, RDF.type, OWL.Class)));
        assertFalse(
            "B rdf:type owl:Class should not be in the base model",
            ontModel.isInBaseModel(ontModel.createStatement(B, RDF.type, OWL.Class)));
    }

    /** Bug report 1408253 from Holger - rdfs:Datatype should be recognised as a RDFS class
     * even without the reasoner
     */
    public void test_hk_07() {
        // owl full
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        Resource c = m.createResource();
        c.addProperty( RDF.type, RDFS.Datatype );
        assertTrue( c.canAs( OntClass.class ));

        // owl dl
        m = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM );
        c = m.createResource();
        c.addProperty( RDF.type, RDFS.Datatype );
        assertTrue( c.canAs( OntClass.class ));

        // owl lite
        m = ModelFactory.createOntologyModel( OntModelSpec.OWL_LITE_MEM );
        c = m.createResource();
        c.addProperty( RDF.type, RDFS.Datatype );
        assertTrue( c.canAs( OntClass.class ));

        // rdfs
        m = ModelFactory.createOntologyModel( OntModelSpec.RDFS_MEM );
        c = m.createResource();
        c.addProperty( RDF.type, RDFS.Datatype );
        assertTrue( c.canAs( OntClass.class ));
    }

    public void test_hk_importCache() {
        final String BASE = "http://protege.stanford.edu/plugins/owl/testdata/";
        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
        spec.setReasoner(null);
        OntDocumentManager dm = OntDocumentManager.getInstance();
        dm.reset();
        dm.setCacheModels(false);
        dm.addAltEntry( "http://protege.stanford.edu/plugins/owl/testdata/Import-normalizerBug.owl",
                        "file:testing/ontology/bugs/test_hk_import/Import-normalizerBug.owl" );
        dm.addAltEntry( "http://protege.stanford.edu/plugins/owl/testdata/normalizerBug.owl",
                        "file:testing/ontology/bugs/test_hk_import/normalizerBug.owl" );
        spec.setDocumentManager(dm);

        OntModel oldOntModel = ModelFactory.createOntologyModel(spec, null);
        oldOntModel.read(BASE + "Import-normalizerBug.owl", FileUtils.langXMLAbbrev);
        Graph oldSubGraph = oldOntModel.getSubGraphs().iterator().next();
        final int oldTripleCount = getTripleCount(oldSubGraph);
        OntClass ontClass = oldOntModel.getOntClass(BASE + "normalizerBug.owl#SuperClass");
        oldSubGraph.add(new Triple(ontClass.asNode(), RDF.type.asNode(), OWL.DeprecatedClass.asNode()));
        assertEquals(oldTripleCount + 1, getTripleCount(oldSubGraph));

        // TODO this workaround to be removed
        SimpleGraphMaker sgm = (SimpleGraphMaker) ((ModelMakerImpl) spec.getImportModelMaker()).getGraphMaker();
        List<String> toGo = new ArrayList<String>();
        for (Iterator<String> i = sgm.listGraphs(); i.hasNext(); toGo.add( i.next() )) {/**/}
        for (Iterator<String> i = toGo.iterator(); i.hasNext(); sgm.removeGraph( i.next() )) {/**/}
        dm.clearCache();

        OntModel newOntModel = ModelFactory.createOntologyModel(spec, null);
        newOntModel.read(BASE + "Import-normalizerBug.owl", FileUtils.langXMLAbbrev);
        Graph newSubGraph = newOntModel.getSubGraphs().iterator().next();
        assertFalse(newOntModel == oldOntModel);  // OK!
        assertFalse(newSubGraph == oldSubGraph);  // FAILS!
        final int newTripleCount = getTripleCount(newSubGraph);
        assertEquals(oldTripleCount, newTripleCount);
    }


    private int getTripleCount(Graph graph) {
        int count = 0;
        for (Iterator<Triple> it = graph.find(null, null, null); it.hasNext();) {
            it.next();
            count++;
        }
        return count;
    }

    /**
     * Bug report by federico.carbone@bt.com, 30-July-2003. A literal can be
     * turned into an individual.
     */
    public void test_fc_01() {
        OntModel m = ModelFactory.createOntologyModel();

        ObjectProperty p = m.createObjectProperty(NS + "p");
        Restriction r = m.createRestriction(p);
        HasValueRestriction hv = r.convertToHasValueRestriction(m.createTypedLiteral(1));

        RDFNode n = hv.getHasValue();
        assertFalse("Should not be able to convert literal to individual", n.canAs(Individual.class));
    }

    /**
     * Bug report by Christoph Kunze (Christoph.Kunz@iao.fhg.de). 18/Aug/03 No
     * transaction support in ontmodel.
     */
    public void test_ck_01() {
        MockTransactionHandler m_t = new MockTransactionHandler();
        Graph g = Factory.createGraphMemWithTransactionHandler( m_t );
        Model m0 = ModelFactory.createModelForGraph( g );
        OntModel m1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_LITE_MEM, m0 );

        assertFalse( "should not initially be in a transaction", m_t.m_inTransaction );
        m1.begin();
        assertTrue( "should be in a transaction", m_t.m_inTransaction );
        m1.abort();
        assertFalse( "should not still be in transaction",  m_t.m_inTransaction );
        assertTrue( "transaction should have been aborted", m_t.m_aborted );
        m1.begin();
        assertTrue( "should be in a (new) transaction", m_t.m_inTransaction );
        m1.commit();
        assertFalse( "should not be in transaction post-commit", m_t.m_inTransaction );
        assertTrue( "should be marked committed post-commit", m_t.m_committed );
    }

    /**
     * Bug report by Christoph Kunz, 26/Aug/03. CCE when creating a statement
     * from a vocabulary
     *
     */
    public void test_ck_02() {
        OntModel vocabModel = ModelFactory.createOntologyModel();
        ObjectProperty p = vocabModel.createObjectProperty("p");
        OntClass A = vocabModel.createClass("A");

        OntModel workModel = ModelFactory.createOntologyModel();
        Individual sub = workModel.createIndividual("uri1", A);
        Individual obj = workModel.createIndividual("uri2", A);
        workModel.createStatement(sub, p, obj);
    }

    /**
     * Bug report from Christoph Kunz - reification problems and
     * UnsupportedOperationException
     */
    public void test_ck_03() {
        // part A - surprising reification
        OntModel model1 = ModelFactory.createOntologyModel(OntModelSpec.DAML_MEM, null);
        OntModel model2 = ModelFactory.createOntologyModel(OntModelSpec.DAML_MEM_RULE_INF, null);

        Individual sub = model1.createIndividual("http://mytest#i1", model1.getProfile().CLASS());
        OntProperty pred = model1.createOntProperty("http://mytest#");
        Individual obj = model1.createIndividual("http://mytest#i2", model1.getProfile().CLASS());
        OntProperty probabilityP = model1.createOntProperty("http://mytest#prob");

        Statement st = model1.createStatement(sub, pred, obj);
        model1.add(st);
        st.createReifiedStatement().addLiteral(probabilityP, 0.9);
        assertTrue("st should be reified", st.isReified());

        Statement st2 = model2.createStatement(sub, pred, obj);
        model2.add(st2);
        st2.createReifiedStatement().addLiteral(probabilityP, 0.3);
        assertTrue("st2 should be reified", st2.isReified());

        sub.addLiteral(probabilityP, 0.3);
        sub.removeAll(probabilityP).addLiteral(probabilityP, 0.3); //!!!
                                                                    // exception

        // Part B - exception in remove All
        Individual sub2 = model2.createIndividual("http://mytest#i1", model1.getProfile().CLASS());

        sub.addLiteral(probabilityP, 0.3);
        sub.removeAll(probabilityP); //!!! exception

        sub2.addLiteral(probabilityP, 0.3);
        sub2.removeAll(probabilityP); //!!! exception

    }

    /**
     * Bug report by sjooseng [sjooseng@hotmail.com]. CCE in listOneOf in
     * Enumerated Class with DAML profile.
     */
    public void test_sjooseng_01() {
        String source =
            "<rdf:RDF xmlns:daml='http://www.daml.org/2001/03/daml+oil#'"
                + "    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
                + "    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' >"
                + "    <daml:Class rdf:about='http://localhost:8080/kc2c#C1'>"
                + "        <daml:subClassOf>"
                + "            <daml:Restriction>"
                + "                <daml:onProperty rdf:resource='http://localhost:8080/kc2c#p1'/>"
                + "                <daml:hasClass>"
                + "                    <daml:Class>"
                + "                        <daml:oneOf rdf:parseType=\"daml:collection\">"
                + "                            <daml:Thing rdf:about='http://localhost:8080/kc2c#i1'/>"
                + "                            <daml:Thing rdf:about='http://localhost:8080/kc2c#i2'/>"
                + "                        </daml:oneOf>"
                + "                    </daml:Class>"
                + "                </daml:hasClass>"
                + "            </daml:Restriction>"
                + "        </daml:subClassOf>"
                + "    </daml:Class>"
                + "    <daml:ObjectProperty rdf:about='http://localhost:8080/kc2c#p1'>"
                + "        <rdfs:label>p1</rdfs:label>"
                + "    </daml:ObjectProperty>"
                + "</rdf:RDF>";

        OntModel m = ModelFactory.createOntologyModel(ProfileRegistry.DAML_LANG);
        m.read(new ByteArrayInputStream(source.getBytes()), "http://localhost:8080/kc2c");

        OntClass kc1 = m.getOntClass("http://localhost:8080/kc2c#C1");

        boolean found = false;

        Iterator<OntClass> it = kc1.listSuperClasses(false);
        while (it.hasNext()) {
            OntClass oc = it.next();
            if (oc.isRestriction()) {
                Restriction r = oc.asRestriction();
                if (r.isSomeValuesFromRestriction()) {
                    SomeValuesFromRestriction sr = r.asSomeValuesFromRestriction();
                    OntClass sc = (OntClass) sr.getSomeValuesFrom();
                    if (sc.isEnumeratedClass()) {
                        EnumeratedClass ec = sc.asEnumeratedClass();
                        assertEquals("Enumeration size should be 2", 2, ec.getOneOf().size());
                        found = true;
                    }
                }
            }
        }

        assertTrue(found);
    }

    /**
     * Problem reported by Andy Seaborne - combine abox and tbox in RDFS with
     * ontmodel
     */
    public void test_afs_01() {
        String sourceT =
            "<rdf:RDF "
                + "    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
                + "    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'"
                + "   xmlns:owl=\"http://www.w3.org/2002/07/owl#\">"
                + "    <owl:Class rdf:about='http://example.org/foo#A'>"
                + "   </owl:Class>"
                + "</rdf:RDF>";

        String sourceA =
            "<rdf:RDF "
                + "    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
                + "    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' "
                + "   xmlns:owl=\"http://www.w3.org/2002/07/owl#\">"
                + "    <rdf:Description rdf:about='http://example.org/foo#x'>"
                + "    <rdf:type rdf:resource='http://example.org/foo#A' />"
                + "   </rdf:Description>"
                + "</rdf:RDF>";

        Model tBox = ModelFactory.createDefaultModel();
        tBox.read(new ByteArrayInputStream(sourceT.getBytes()), "http://example.org/foo");

        Model aBox = ModelFactory.createDefaultModel();
        aBox.read(new ByteArrayInputStream(sourceA.getBytes()), "http://example.org/foo");

        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
        reasoner = reasoner.bindSchema(tBox);

        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM_RULE_INF);
        spec.setReasoner(reasoner);

        OntModel m = ModelFactory.createOntologyModel(spec, aBox);

        List<Individual> inds = new ArrayList<Individual>();
        for (Iterator<Individual> i = m.listIndividuals(); i.hasNext();) {
            inds.add(i.next());
        }

        assertTrue("x should be an individual", inds.contains(m.getResource("http://example.org/foo#x")));

    }

    /**
     * Bug report by Thorsten Ottmann [Thorsten.Ottmann@rwth-aachen.de] -
     * problem accessing elements of DAML list
     */
    public void test_to_01() {
        String sourceT =
            "<rdf:RDF "
                + "    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
                + "    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'"
                + "    xmlns:daml='http://www.daml.org/2001/03/daml+oil#'>"
                + "  <daml:Class rdf:about='http://example.org/foo#A'>"
                + "    <daml:intersectionOf rdf:parseType=\"daml:collection\">"
                + "       <daml:Class rdf:ID=\"B\" />"
                + "       <daml:Class rdf:ID=\"C\" />"
                + "    </daml:intersectionOf>"
                + "  </daml:Class>"
                + "</rdf:RDF>";

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.DAML_MEM, null);
        m.read(new ByteArrayInputStream(sourceT.getBytes()), "http://example.org/foo");

        OntClass A = m.getOntClass("http://example.org/foo#A");
        assertNotNull(A);

        IntersectionClass iA = A.asIntersectionClass();
        assertNotNull(iA);

        RDFList intersection = iA.getOperands();
        assertNotNull(intersection);

        assertEquals(2, intersection.size());
        assertTrue(intersection.contains(m.getOntClass("http://example.org/foo#B")));
        assertTrue(intersection.contains(m.getOntClass("http://example.org/foo#C")));
    }

    /**
     * Bug report by Thorsten Liebig [liebig@informatik.uni-ulm.de] -
     * SymmetricProperty etc not visible in list ont properties
     */
    public void test_tl_01() {
        String sourceT =
            "<rdf:RDF "
                + "    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
                + "    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'"
                + "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\">"
                + "   <owl:SymmetricProperty rdf:about='http://example.org/foo#p1'>"
                + "   </owl:SymmetricProperty>"
                + "   <owl:TransitiveProperty rdf:about='http://example.org/foo#p2'>"
                + "   </owl:TransitiveProperty>"
                + "   <owl:InverseFunctionalProperty rdf:about='http://example.org/foo#p3'>"
                + "   </owl:InverseFunctionalProperty>"
                + "</rdf:RDF>";

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, null);
        m.read(new ByteArrayInputStream(sourceT.getBytes()), "http://example.org/foo");

        boolean foundP1 = false;
        boolean foundP2 = false;
        boolean foundP3 = false;

        // iterator of properties should include p1-3
        for (Iterator<OntProperty> i = m.listOntProperties(); i.hasNext();) {
            Resource r = i.next();
            foundP1 = foundP1 || r.getURI().equals("http://example.org/foo#p1");
            foundP2 = foundP2 || r.getURI().equals("http://example.org/foo#p2");
            foundP3 = foundP3 || r.getURI().equals("http://example.org/foo#p3");
        }

        assertTrue("p1 not listed", foundP1);
        assertTrue("p2 not listed", foundP2);
        assertTrue("p3 not listed", foundP3);

        foundP1 = false;
        foundP2 = false;
        foundP3 = false;

        // iterator of object properties should include p1-3
        for (Iterator<ObjectProperty> i = m.listObjectProperties(); i.hasNext();) {
            Resource r = i.next();
            foundP1 = foundP1 || r.getURI().equals("http://example.org/foo#p1");
            foundP2 = foundP2 || r.getURI().equals("http://example.org/foo#p2");
            foundP3 = foundP3 || r.getURI().equals("http://example.org/foo#p3");
        }

        assertTrue("p1 not listed", foundP1);
        assertTrue("p2 not listed", foundP2);
        assertTrue("p3 not listed", foundP3);
    }

    /** Bug report by Dave Reynolds - SF bug report 810492 */
    public void test_der_01() {
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_TRANS_INF, null);
        Resource a = m.createResource("http://example.org#A");
        Resource b = m.createResource("http://example.org#B");
        OntClass A = new OntClassImpl(a.asNode(), (EnhGraph) m) {
            @Override
            protected boolean hasSuperClassDirect(Resource cls) {
                throw new RuntimeException("did not find direct reasoner");
            }
        };

        // will throw an exception if the wrong code path is taken
        A.hasSuperClass(b, true);
    }

    /**
     * Bug report by Ivan Ferrari (ivan_ferrari_75 [ivan_ferrari_75@yahoo.it]) -
     * duplicate nodes in output
     */
    public void test_if_01() {
        //create a new default model
        OntModel m = ModelFactory.createOntologyModel();

        m.getDocumentManager().addAltEntry(
            "http://www.w3.org/2001/sw/WebOnt/guide-src/wine",
            "file:testing/ontology/bugs/oldwine.owl");
        m.getDocumentManager().addAltEntry(
            "http://www.w3.org/2001/sw/WebOnt/guide-src/food",
            "file:testing/ontology/bugs/oldfood.owl");

        // note: due to bug in the Wine example, we have to manually read the
        // imported food document
        m.getDocumentManager().setProcessImports(false);
        m.read("http://www.w3.org/2001/sw/WebOnt/guide-src/wine");
        m.getDocumentManager().setProcessImports(true);
        m.getDocumentManager().loadImport(m, "http://www.w3.org/2001/sw/WebOnt/guide-src/food");

        OntClass ontclass = m.getOntClass("http://www.w3.org/2001/sw/WebOnt/guide-src/wine#Wine");

        int nNamed = 0;
        int nRestriction = 0;
        int nAnon = 0;

        for (ExtendedIterator<OntClass> iter2 = ontclass.listSuperClasses(true); iter2.hasNext();) {
            OntClass ontsuperclass = iter2.next();

            //this is to view different anonymous IDs
            if (!ontsuperclass.isAnon()) {
                nNamed++;
            }
            else if (ontsuperclass.canAs(Restriction.class)) {
                ontsuperclass.asRestriction();
                nRestriction++;
            }
            else {
                nAnon++;
            }
        }

        assertEquals("Should be two named super classes ", 2, nNamed);
        assertEquals("Should be nine named super classes ", 9, nRestriction);
        assertEquals("Should be no named super classes ", 0, nAnon);
    }

    /** Bug report by Lawrence Tay - missing datatype property */
    public void test_lt_01() {
        OntModel m = ModelFactory.createOntologyModel();

        DatatypeProperty p = m.createDatatypeProperty(NS + "p");
        OntClass c = m.createClass(NS + "A");

        Individual i = m.createIndividual(NS + "i", c);
        i.addProperty(p, "testData");

        int count = 0;

        for (Iterator<RDFNode> j = i.listPropertyValues(p); j.hasNext();) {
            j.next();
            count++;
        }

        assertEquals("i should have one property", 1, count);
    }


    /** Bug report by David Kensche [david.kensche@post.rwth-aachen.de] - NPE in listDeclaredProperties */
    public void test_dk_01() {
        OntModel m = ModelFactory.createOntologyModel();
        m.read( "file:testing/ontology/bugs/test_dk_01.xml" );

        String ns = "http://localhost:8080/Repository/QueryAgent/UserOntology/qgen-example-1#";
        String[] classes = new String[] {ns+"C1", ns+"C3", ns+"C2"};

        for (int i = 0; i < classes.length; i++) {
            OntClass c = m.getOntClass( classes[i] );
            for (Iterator<OntProperty> j = c.listDeclaredProperties(); j.hasNext(); j.next() ) {/**/}
        }
    }

    /** Bug report by anon at SourceForge - Bug ID 887409 */
    public void test_anon_0() {
        String ns = "http://example.org/foo#";
        String sourceT =
            "<rdf:RDF "
            + "    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
            + "    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'"
            + "    xmlns:ex='http://example.org/foo#'"
            + "    xmlns:owl='http://www.w3.org/2002/07/owl#'>"
            + "   <owl:ObjectProperty rdf:about='http://example.org/foo#p' />"
            + "   <owl:Class rdf:about='http://example.org/foo#A' />"
            + "   <ex:A rdf:about='http://example.org/foo#x' />"
            + "   <owl:Class rdf:about='http://example.org/foo#B'>"
            + "     <owl:equivalentClass>"
            + "      <owl:Restriction>"
            + "        <owl:onProperty rdf:resource='http://example.org/foo#p' />"
            + "        <owl:hasValue rdf:resource='http://example.org/foo#x' />"
            + "      </owl:Restriction>"
            + "     </owl:equivalentClass>"
            + "   </owl:Class>"
            + "</rdf:RDF>";

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        m.read(new ByteArrayInputStream(sourceT.getBytes()), "http://example.org/foo");

        OntClass B = m.getOntClass( ns + "B");
        Restriction r = B.getEquivalentClass().asRestriction();
        HasValueRestriction hvr = r.asHasValueRestriction();
        RDFNode n = hvr.getHasValue();

        assertTrue( "Should be an individual", n instanceof Individual );
    }

    /** Bug report by Zhao Jun [jeff@seu.edu.cn] - throws no such element exception */
    public void test_zj_0() {
        String ns = "file:/C:/orel/orel0_5.owl#";
        String sourceT =
            "<rdf:RDF " +
            "    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'" +
            "    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'" +
            "    xmlns:ex='http://example.org/foo#'" +
            "    xmlns:owl='http://www.w3.org/2002/07/owl#'" +
            "      xmlns:orel='file:/C:/orel/orel0_5.owl#'" +
            "      xml:base='file:/C:/orel/orel0_5.owl#'" +
            "      xmlns='file:/C:/orel/orel0_5.owl#'>" +
            " <owl:ObjectProperty rdf:ID='hasAgent' />" +
            " <owl:ObjectProperty rdf:ID='hasResource' />" +
            " <owl:Class rdf:ID='MyPlay'>" +
            "    <rdfs:subClassOf>" +
            "      <owl:Restriction>" +
            "        <owl:onProperty rdf:resource='file:/C:/orel/orel0_5.owl#hasResource'/>" +
            "        <owl:hasValue>" +
            "          <orel:Resource rdf:ID='myResource'>" +
            "            <orel:resourceURI>http://mp3.com/newcd/sample.mp3</orel:resourceURI>" +
            "          </orel:Resource>" +
            "        </owl:hasValue>" +
            "      </owl:Restriction>" +
            "    </rdfs:subClassOf>" +
            "    <rdfs:subClassOf rdf:resource='http://www.w3.org/2002/07/owl#Thing'/>" +
            "    <rdfs:subClassOf>" +
            "      <owl:Restriction>" +
            "        <owl:onProperty rdf:resource='file:/C:/orel/orel0_5.owl#hasAgent'/>" +
            "        <owl:hasValue>" +
            "          <orel:Agent rdf:ID='myAgent'>" +
            "            <orel:agentPK>123456789</orel:agentPK>" +
            "          </orel:Agent>" +
            "        </owl:hasValue>" +
            "      </owl:Restriction>" +
            "    </rdfs:subClassOf>" +
            "    <rdfs:subClassOf rdf:resource='file:/C:/orel/orel0_5.owl#Play'/>" +
            "  </owl:Class>" +
            "</rdf:RDF>";

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, null);
        m.read(new ByteArrayInputStream(sourceT.getBytes()), "file:/C:/orel/orel0_5.owl");

        OntClass myPlay = m.getOntClass( ns + "MyPlay");
        for (Iterator<OntProperty> i = myPlay.listDeclaredProperties(); i.hasNext(); ) {
            //System.err.println( "prop " + i.next() );
            i.next();
        }
    }

    /** Bug report by Harry Chen - closed exception when reading many models */
    public void test_hc_01()
        throws Exception
    {
        for (int i = 0; i < 5; i++) {

            OntModel m = ModelFactory.createOntologyModel();

            FileInputStream ifs = new FileInputStream("testing/ontology/relativenames.rdf");

            //System.out.println("Start reading...");
            m.read(ifs, "http://example.org/foo");
            //System.out.println("Done reading...");

            ifs.close();
            //System.out.println("Closed ifs");
            m.close();
            //System.out.println("Closed model");
        }
    }

    /** Bug report by sinclair bain (slbain) SF bugID 912202 - NPE in createOntResource() when 2nd param is null */
    public void test_sb_01() {
        OntModel model= ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF, null);

        Resource result= null;
        Resource nullValueForResourceType= null;

        result= model.createOntResource( OntResource.class, nullValueForResourceType, "http://www.somewhere.com/models#SomeResourceName" );
        assertNotNull( result );
    }

    /* Bug report from Dave Reynolds: listDeclaredProperties not complete */
    public void test_der_02() {
        String SOURCE=
        "<?xml version='1.0'?>" +
        "<!DOCTYPE owl [" +
        "      <!ENTITY rdf  'http://www.w3.org/1999/02/22-rdf-syntax-ns#' >" +
        "      <!ENTITY rdfs 'http://www.w3.org/2000/01/rdf-schema#' >" +
        "      <!ENTITY xsd  'http://www.w3.org/2001/XMLSchema#' >" +
        "      <!ENTITY owl  'http://www.w3.org/2002/07/owl#' >" +
        "      <!ENTITY dc   'http://purl.org/dc/elements/1.1/' >" +
        "      <!ENTITY base  'http://jena.hpl.hp.com/test' >" +
        "    ]>" +
        "<rdf:RDF xmlns:owl ='&owl;' xmlns:rdf='&rdf;' xmlns:rdfs='&rdfs;' xmlns:dc='&dc;' xmlns='&base;#' xml:base='&base;'>" +
        "  <owl:ObjectProperty rdf:ID='hasPublications'>" +
        "    <rdfs:domain>" +
        "      <owl:Class>" +
        "        <owl:unionOf rdf:parseType='Collection'>" +
        "          <owl:Class rdf:about='#Project'/>" +
        "          <owl:Class rdf:about='#Task'/>" +
        "        </owl:unionOf>" +
        "      </owl:Class>" +
        "    </rdfs:domain>" +
        "    <rdfs:domain rdf:resource='#Dummy' />" +
        "    <rdfs:range rdf:resource='#Publications'/>" +
        "  </owl:ObjectProperty>" +
        "  <owl:Class rdf:ID='Dummy'>" +
        "  </owl:Class>" +
        "</rdf:RDF>";
        String ns = "http://jena.hpl.hp.com/test#";
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, null);
        m.read(new ByteArrayInputStream( SOURCE.getBytes()), ns );

        //OntClass dummy = m.getOntClass( NS + "Dummy" );
        // assert commented out - bug not accepted -ijd
        //TestUtil.assertIteratorValues( this, dummy.listDeclaredProperties(),
        //                               new Object[] {m.getObjectProperty( NS+"hasPublications")} );
    }

    /** Bug report from Dave - cycles checking code still not correct */
    public void test_der_03() {
        String ns = "http://jena.hpl.hp.com/test#";
        OntModel om = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        OntClass A = om.createClass(ns+"A");
        OntClass B = om.createClass(ns+"B");
        OntClass C = om.createClass(ns+"C");
        A.addSuperClass(B);
        A.addSuperClass(C);
        B.addSuperClass(C);
        C.addSuperClass(B);

        TestUtil.assertIteratorValues( this, A.listSuperClasses( true ), new Object[] {B,C} );
    }


    /**
     * Bug report by pierluigi.damadio@katamail.com: raises conversion exception
     */
    public void test_pd_01() {
        String SOURCE =
            "<?xml version='1.0'?>" +
            "<rdf:RDF" +
            "    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'" +
            "    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'" +
            "    xmlns:owl='http://www.w3.org/2002/07/owl#'" +
            "    xml:base='http://iasi.cnr.it/leks/localSchema1#'" +
            "    xmlns:test='http://iasi.cnr.it/test/test1#'" +
            "    xmlns='http://iasi.cnr.it/test/test1#'>" +
            "    <owl:Ontology rdf:about=''/>" +
            "    <owl:Class rdf:ID='Hotel'/>" +
            "    <owl:Class rdf:ID='Hotel5Stars'>" +
            "        <rdfs:subClassOf>" +
            "            <owl:Restriction>" +
            "                <owl:onProperty rdf:resource='#hasCategory'/>" +
            "                <owl:hasValue rdf:resource='#Category5'/>" +
            "            </owl:Restriction>" +
            "        </rdfs:subClassOf>" +
            "    </owl:Class>" +
            "    <owl:DatatypeProperty rdf:ID='hasCategory'>" +
            "        <rdfs:range rdf:resource='http://www.w3.org/2001/XMLSchema#string'/>" +
            "        <rdfs:domain rdf:resource='#Hotel'/>" +
            "        <rdf:type rdf:resource='http://www.w3.org/2002/07/owl#FunctionalProperty'/>" +
            "    </owl:DatatypeProperty>" +
            "    <owl:Thing rdf:ID='Category5'/>" +
            "</rdf:RDF>";
        String ns = "http://iasi.cnr.it/leks/localSchema1#";
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, null);
        m.read(new ByteArrayInputStream( SOURCE.getBytes()), ns );

        for (ExtendedIterator<Restriction> j = m.listRestrictions(); j.hasNext(); ) {
              Restriction r = j.next();
              if (r.isHasValueRestriction()) {
                  HasValueRestriction hv = r.asHasValueRestriction();
                  hv.getHasValue().toString();
              }
        }
    }

    /** Bug report from Ole Hjalmar - direct subClassOf not reporting correct result with rule reasoner */
    public void xxtest_oh_01() {
        String ns = "http://www.idi.ntnu.no/~herje/ja/";
        Resource[] expected = new Resource[] {
            ResourceFactory.createResource( ns+"reiseliv.owl#Reiseliv" ),
            ResourceFactory.createResource( ns+"hotell.owl#Hotell" ),
            ResourceFactory.createResource( ns+"restaurant.owl#Restaurant" ),
            ResourceFactory.createResource( ns+"restaurant.owl#UteRestaurant" ),
            ResourceFactory.createResource( ns+"restaurant.owl#UteBadRestaurant" ),
            ResourceFactory.createResource( ns+"restaurant.owl#UteDoRestaurant" ),
            ResourceFactory.createResource( ns+"restaurant.owl#SkogRestaurant" ),
        };

        test_oh_01scan( OntModelSpec.OWL_MEM, "No inf", expected );
        test_oh_01scan( OntModelSpec.OWL_MEM_MINI_RULE_INF, "Mini rule inf", expected );
        test_oh_01scan( OntModelSpec.OWL_MEM_RULE_INF, "Full rule inf", expected );
        test_oh_01scan( OntModelSpec.OWL_MEM_MICRO_RULE_INF, "Micro rule inf", expected );
    }

    private void test_oh_01scan( OntModelSpec s, String prompt, Resource[] expected ) {
        String ns = "http://www.idi.ntnu.no/~herje/ja/reiseliv.owl#";
        OntModel m = ModelFactory.createOntologyModel(s, null);
        m.read( "file:testing/ontology/bugs/test_oh_01.owl");

        System.out.println( prompt );
        OntClass r = m.getOntClass( ns + "Reiseliv" );
        List<OntClass> q = new ArrayList<OntClass>();
        Set<OntClass> seen = new HashSet<OntClass>();
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

            System.out.println( "  Seen class " + c );
        }

        // check we got all classes
        int mask = (1 << expected.length) - 1;

        for (int j = 0;  j < expected.length; j++) {
            if (seen.contains( expected[j] )) {
                mask &= ~(1 << j);
            }
            else {
                System.out.println( "Expected but did not see " + expected[j] );
            }
        }

        for (Iterator<OntClass> k = seen.iterator();  k.hasNext(); ) {
            Resource res = k.next();
            boolean isExpected = false;
            for (int j = 0;  !isExpected && j < expected.length; j++) {
                isExpected = expected[j].equals( res );
            }
            if (!isExpected) {
                System.out.println( "Got unexpected result " + res );
            }
        }

        assertEquals( "Some expected results were not seen", 0, mask );
    }

    /** Test case for SF bug 927641 - list direct subclasses */
    public void test_sf_927641() {
        String ns = "http://example.org/test#";
        OntModel m0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        OntClass c0 = m0.createClass( ns + "C0" );
        OntClass c1 = m0.createClass( ns + "C1" );
        OntClass c2 = m0.createClass( ns + "C2" );
        OntClass c3 = m0.createClass( ns + "C3" );

        c0.addSubClass( c1 );
        c1.addSubClass( c2 );
        c2.addEquivalentClass( c3 );

        // now c1 is the direct super-class of c2, even allowing for the equiv with c3
        assertFalse( "pass 1: c0 should not be a direct super of c2", c2.hasSuperClass( c0, true ) );
        assertFalse( "pass 1: c3 should not be a direct super of c2", c2.hasSuperClass( c3, true ) );
        assertFalse( "pass 1: c2 should not be a direct super of c2", c2.hasSuperClass( c2, true ) );
        assertTrue( "pass 1: c1 should be a direct super of c2", c2.hasSuperClass( c1, true ) );

        // second pass - with inference
        m0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RULE_INF );
        c0 = m0.createClass( ns + "C0" );
        c1 = m0.createClass( ns + "C1" );
        c2 = m0.createClass( ns + "C2" );
        c3 = m0.createClass( ns + "C3" );

        c0.addSubClass( c1 );
        c1.addSubClass( c2 );
        c2.addEquivalentClass( c3 );

        // now c1 is the direct super-class of c2, even allowing for the equiv with c3
        assertFalse( "pass 2: c0 should not be a direct super of c2", c2.hasSuperClass( c0, true ) );
        assertFalse( "pass 2: c3 should not be a direct super of c2", c2.hasSuperClass( c3, true ) );
        assertFalse( "pass 2: c2 should not be a direct super of c2", c2.hasSuperClass( c2, true ) );
        assertTrue( "pass 2: c1 should be a direct super of c2", c2.hasSuperClass( c1, true ) );
    }


    /** Test case for SF bug 934528 - conversion exception with owl:Thing and owl:Nothing when no reasoner */
    public void test_sf_934528() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );

        Resource r = OWL.Thing.inModel( m );
        OntClass thingClass = r.as( OntClass.class );
        assertNotNull( thingClass );

        r = OWL.Nothing.inModel( m );
        OntClass nothingClass = r.as( OntClass.class );
        assertNotNull( nothingClass );

        OntClass c = m.getOntClass( OWL.Thing.getURI() );
        assertNotNull( c );
        assertEquals( c, OWL.Thing );

        c = m.getOntClass( OWL.Nothing.getURI() );
        assertNotNull( c );
        assertEquals( c, OWL.Nothing );
    }

    /** Test case for SF bug 937810 - NPE from ModelSpec.getDescription() */
    /* Test removed 16-Jan-07 following refactoring of ModelSpec code */
//    public void test_sf_937810() throws IllegalAccessException {
//        Field[] specs = OntModelSpec.class.getDeclaredFields();
//
//        for (int i = 0;  i < specs.length;  i++) {
//            if (Modifier.isPublic( specs[i].getModifiers()) &&
//                Modifier.isStatic( specs[i].getModifiers()) &&
//                specs[i].getType().equals( OntModelSpec.class )) {
//                OntModelSpec s = (OntModelSpec) specs[i].get( null );
//                assertNotNull( s.getDescription() );
//            }
//        }
//    }

    /** Test case for SF bug 940570 - listIndividuals not working with RDFS_INF
     */
    public void test_sf_940570() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RDFS_INF );
        OntClass C = m.createClass( NS + "C" );
        Resource a = m.createResource( NS + "a", C );

        TestUtil.assertIteratorValues( this, m.listIndividuals(), new Object[] {a} );

        OntModel dm = ModelFactory.createOntologyModel( OntModelSpec.DAML_MEM_RULE_INF );
        OntClass D = dm.createClass( NS + "D" );
        Resource b = dm.createResource( NS + "b", D );

        TestUtil.assertIteratorValues( this, dm.listIndividuals(), new Object[] {b} );
    }

    /** Test case for SF bug 940570 - listIndividuals not working with RDFS_INF (rdfs case)
     */
    public void test_sf_940570_rdfs() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RDFS_INF );
        OntClass C = m.createClass( NS + "C" );
        Resource a = m.createResource( NS + "a", C );

        TestUtil.assertIteratorValues( this, m.listIndividuals(), new Object[] {a} );
    }

    /** Test case for SF bug 940570 - listIndividuals not working with RDFS_INF (daml case)
     */
    public void test_sf_940570_daml() {
        OntModel dm = ModelFactory.createOntologyModel( OntModelSpec.DAML_MEM_RULE_INF );
        OntClass D = dm.createClass( NS + "D" );
        Resource b = dm.createResource( NS + "b", D );

        TestUtil.assertIteratorValues( this, dm.listIndividuals(), new Object[] {b} );
    }

    /** Test case for SF bug 945436 - a xml:lang='' in the dataset causes sring index exception in getLabel() */
    public void test_sf_945436() {
        String SOURCE=
            "<?xml version='1.0'?>" +
            "<!DOCTYPE owl [" +
            "      <!ENTITY rdf  'http://www.w3.org/1999/02/22-rdf-syntax-ns#' >" +
            "      <!ENTITY rdfs 'http://www.w3.org/2000/01/rdf-schema#' >" +
            "      <!ENTITY xsd  'http://www.w3.org/2001/XMLSchema#' >" +
            "      <!ENTITY owl  'http://www.w3.org/2002/07/owl#' >" +
            "      <!ENTITY dc   'http://purl.org/dc/elements/1.1/' >" +
            "      <!ENTITY base  'http://jena.hpl.hp.com/test' >" +
            "    ]>" +
            "<rdf:RDF xmlns:owl ='&owl;' xmlns:rdf='&rdf;' xmlns:rdfs='&rdfs;' xmlns:dc='&dc;' xmlns='&base;#' xml:base='&base;'>" +
            "  <C rdf:ID='x'>" +
            "    <rdfs:label xml:lang=''>a_label</rdfs:label>" +
            "  </C>" +
            "  <owl:Class rdf:ID='C'>" +
            "  </owl:Class>" +
            "</rdf:RDF>";
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        m.read( new StringReader( SOURCE ), null );
        Individual x = m.getIndividual( "http://jena.hpl.hp.com/test#x" );
        assertEquals( "Label on resource x", "a_label", x.getLabel( null) );
        assertEquals( "Label on resource x", "a_label", x.getLabel( "" ) );
        assertSame( "fr label on resource x", null, x.getLabel( "fr" ) );
    }

    /** Test case for SF bug 948995  - OWL full should allow inverse functional datatype properties */
    public void test_sf_948995() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM );  // OWL dl
        DatatypeProperty dp = m.createDatatypeProperty( NS + "dp" );
        dp.addRDFType( OWL.InverseFunctionalProperty );

        boolean ex = false;
        try {
            dp.as( InverseFunctionalProperty.class );
        }
        catch (ConversionException e) {
            ex = true;
        }
        assertTrue( "Should have been a conversion exception", ex );

        m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );  // OWL full
        dp = m.createDatatypeProperty( NS + "dp" );
        dp.addRDFType( OWL.InverseFunctionalProperty );

        ex = false;
        try {
            dp.as( InverseFunctionalProperty.class );
        }
        catch (ConversionException e) {
            ex = true;
        }
        assertFalse( "Should not have been a conversion exception", ex );
    }

    /** Test case for SF bug 969475 - the return value for getInverse() on an ObjectProperty should be an object property */
    public void test_sf_969475() {
        String SOURCE=
            "<?xml version='1.0'?>" +
            "<!DOCTYPE owl [" +
            "      <!ENTITY rdf  'http://www.w3.org/1999/02/22-rdf-syntax-ns#' >" +
            "      <!ENTITY rdfs 'http://www.w3.org/2000/01/rdf-schema#' >" +
            "      <!ENTITY xsd  'http://www.w3.org/2001/XMLSchema#' >" +
            "      <!ENTITY owl  'http://www.w3.org/2002/07/owl#' >" +
            "      <!ENTITY dc   'http://purl.org/dc/elements/1.1/' >" +
            "      <!ENTITY base  'http://jena.hpl.hp.com/test' >" +
            "    ]>" +
            "<rdf:RDF xmlns:owl ='&owl;' xmlns:rdf='&rdf;' xmlns:rdfs='&rdfs;' xmlns:dc='&dc;' xmlns='&base;#' xml:base='&base;'>" +
            "  <owl:ObjectProperty rdf:ID='p0'>" +
            "    <owl:inverseOf>" +
            "      <owl:ObjectProperty rdf:ID='q0' />" +
            "    </owl:inverseOf>" +
            "  </owl:ObjectProperty>" +
            "  <owl:ObjectProperty rdf:ID='p1'>" +
            "    <owl:inverseOf>" +
            "      <owl:ObjectProperty rdf:ID='q1' />" +
            "    </owl:inverseOf>" +
            "  </owl:ObjectProperty>" +
            "</rdf:RDF>";
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        m.read( new StringReader( SOURCE ), null );

        ObjectProperty p0 = m.getObjectProperty( "http://jena.hpl.hp.com/test#p0");
        Object invP0 = p0.getInverseOf();

        assertEquals( m.getResource( "http://jena.hpl.hp.com/test#q0"), invP0 );
        assertTrue( "Should be an ObjectProperty facet", invP0 instanceof ObjectProperty );

        ObjectProperty q1 = m.getObjectProperty( "http://jena.hpl.hp.com/test#q1");
        Object invQ1 = q1.getInverse();

        assertEquals( m.getResource( "http://jena.hpl.hp.com/test#p1"), invQ1 );
        assertTrue( "Should be an ObjectProperty facet", invQ1 instanceof ObjectProperty );
    }

    /** Test case for SF bug 978259 - missing supports() checks in OWL DL and Lite profiles */
    public void test_sf_978259() {
        OntModel md = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM );
        OntModel ml = ModelFactory.createOntologyModel( OntModelSpec.OWL_LITE_MEM );

        DataRange drd = md.createDataRange( md.createList( new Resource[] {OWL.Thing}) );
        assertNotNull( drd );
        HasValueRestriction hvrd = md.createHasValueRestriction( null, RDFS.seeAlso, OWL.Thing );
        assertNotNull( hvrd );

        boolean ex = false;
        try {
            drd = ml.createDataRange( md.createList( new Resource[] {OWL.Thing}) );
        }
        catch (ProfileException e) {
            ex = true;
        }
        assertTrue( ex );

        ex = false;
        try {
            hvrd = ml.createHasValueRestriction( null, RDFS.seeAlso, OWL.Thing );
        }
        catch (ProfileException e) {
            ex = true;
        }
        assertTrue( ex );
    }

    /**
     * Bug report by Jessica Brown jessicabrown153@yahoo.com: listIndividuals() fails
     * on a composite model in Jena 2.5
     */
    public void test_jb_01() {
        Model schema = ModelFactory.createDefaultModel();
        Model data = ModelFactory.createDefaultModel();
        Resource c = schema.createResource( "http://example.com/foo#AClass" );
        Resource i = data.createResource( "http://example.com/foo#anInd" );
        schema.add( c, RDF.type, OWL.Class );
        data.add( i, RDF.type, c );

        OntModel composite = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, schema );
        composite.addSubModel( data );

        Set<Individual> s = composite.listIndividuals().toSet();
        assertEquals( "should be one individual", 1, s.size() );
        assertTrue( s.contains( i ));
    }

    /**
     * Bug report by David Bigwood - listDeclaredProps(false) fails when props
     * are defined in an imported model
     */
    public void test_dab_01() {
        OntModel m0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );

        // in model M0, p0 has class c0 in the domain
        OntClass c0 = m0.createClass( NS + "c0" );
        ObjectProperty p0 = m0.createObjectProperty( NS + "p0" );
        p0.setDomain( c0 );

        // in model M1, class c1 is a subClass of c0
        OntModel m1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        OntClass c1 = m1.createClass( NS + "c1" );
        c1.addSuperClass( c0 );

        // simulate imports
        m1.addSubModel( m0 );

        // get a c0 reference from m1
        OntClass cc0 = m1.getOntClass( NS + "c0" );
        assertNotNull( cc0 );

        TestUtil.assertIteratorValues( this, c1.listDeclaredProperties(), new Object[] {p0} );
        TestUtil.assertIteratorValues( this, c0.listDeclaredProperties(false), new Object[] {p0} );

        // this is the one that fails per David's bug report
        TestUtil.assertIteratorValues( this, cc0.listDeclaredProperties(false), new Object[] {p0} );
    }

    /**
     * Bug report by David Bigwood - listUnionClasses causes conversion exception
     */
    public void test_dab_02a() {
        String SOURCEA=
            "<rdf:RDF" +
            "    xmlns:rdf          ='http://www.w3.org/1999/02/22-rdf-syntax-ns#'" +
            "    xmlns:owl          ='http://www.w3.org/2002/07/owl#'" +
            "    xml:base           ='http://example.com/a#'" +
            ">" +
            "<rdf:Description>" +
            "  <owl:unionOf " +
            "  rdf:resource='http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'/> " +
            "</rdf:Description>" +
            "</rdf:RDF>";

        OntModel a0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        a0.read( new StringReader( SOURCEA ), null );

        // throws conversion exception ...
        for( Iterator<UnionClass> i = a0.listUnionClasses(); i.hasNext(); ) {
            i.next();
        }

        OntModel a1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM );
        a1.read( new StringReader( SOURCEA ), null );

        // throws conversion exception ...
        for( Iterator<UnionClass> i = a1.listUnionClasses(); i.hasNext(); ) {
            i.next();
        }
    }

    public void test_dab_02b() {
        String SOURCEA=
            "<rdf:RDF" +
            "    xmlns:rdf          ='http://www.w3.org/1999/02/22-rdf-syntax-ns#'" +
            "    xmlns:owl          ='http://www.w3.org/2002/07/owl#'" +
            "    xml:base           ='http://example.com/a#'" +
            ">" +
            "<rdf:Description>" +
            "  <owl:intersectionOf " +
            "  rdf:resource='http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'/> " +
            "</rdf:Description>" +
            "</rdf:RDF>";

        OntModel a0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        a0.read( new StringReader( SOURCEA ), null );

        // throws conversion exception ...
        for( Iterator<IntersectionClass> i = a0.listIntersectionClasses(); i.hasNext(); ) {
            i.next();
        }

        OntModel a1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM );
        a1.read( new StringReader( SOURCEA ), null );

        // throws conversion exception ...
        for( Iterator<IntersectionClass> i = a1.listIntersectionClasses(); i.hasNext(); ) {
            i.next();
        }

        OntModel a2 = ModelFactory.createOntologyModel( OntModelSpec.OWL_LITE_MEM );
        a2.read( new StringReader( SOURCEA ), null );

        // throws conversion exception ...
        for( Iterator<IntersectionClass> i = a2.listIntersectionClasses(); i.hasNext(); ) {
            i.next();
        }
    }

    public void test_dab_02c() {
        String SOURCEA=
            "<rdf:RDF" +
            "    xmlns:rdf          ='http://www.w3.org/1999/02/22-rdf-syntax-ns#'" +
            "    xmlns:owl          ='http://www.w3.org/2002/07/owl#'" +
            "    xml:base           ='http://example.com/a#'" +
            ">" +
            "<rdf:Description>" +
            "  <owl:complementOf " +
            "  rdf:resource='http://www.w3.org/2002/07/owl#Nothing'/> " +
            "</rdf:Description>" +
            "</rdf:RDF>";

        OntModel a0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        a0.read( new StringReader( SOURCEA ), null );

        // throws conversion exception ...
        for( Iterator<ComplementClass> i = a0.listComplementClasses(); i.hasNext(); ) {
            i.next();
        }

        OntModel a1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM );
        a1.read( new StringReader( SOURCEA ), null );

        // throws conversion exception ...
        for( Iterator<ComplementClass> i = a1.listComplementClasses(); i.hasNext(); ) {
            i.next();
        }
    }

    /**
     * Bug report by Othmane Nadjemi - DAML individual whose only type is daml:Thing
     * returns false to isIndividual()
     */
    public void test_on_01() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.DAML_MEM );
        Individual i = m.createIndividual( DAML_OIL.Thing );
        assertTrue( i.isIndividual() );
    }


    /**
     * Bug report by kers - maximal lower elements calculation not correct in models
     * with no reasoner. Manifests as direct sub-class bug.
     */
    public void test_kers_01() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.RDFS_MEM );
        OntClass r = m.createClass( NS + "r" );
        OntClass a = m.createClass( NS + "a" );
        OntClass b = m.createClass( NS + "b" );
        OntClass c = m.createClass( NS + "c" );
        OntClass d = m.createClass( NS + "d" );
        OntClass e = m.createClass( NS + "e" );
        OntClass f = m.createClass( NS + "f" );
        OntClass g = m.createClass( NS + "g" );

        g.addSuperClass( c );
        f.addSuperClass( c );
        e.addSuperClass( b );
        d.addSuperClass( b );
        c.addSuperClass( a );
        b.addSuperClass( a );

        // simulated closure
        r.addSubClass( a );
        r.addSubClass( b );
        r.addSubClass( c );
        r.addSubClass( d );
        r.addSubClass( e );
        r.addSubClass( f );
        r.addSubClass( g );

        TestUtil.assertIteratorValues( this, r.listSubClasses( true ), new Object[] {a} );
    }

    public void test_kers_02() {
        OntModel A = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM  );
        Model B = ModelFactory.createDefaultModel();
        assertTrue( A.getGraph().queryHandler() instanceof SimpleQueryHandler );
        assertTrue( B.getGraph().queryHandler() instanceof GraphMemFasterQueryHandler );
        assertTrue( A.getBaseModel().getGraph().queryHandler() instanceof GraphMemFasterQueryHandler );
    }

    /**
     * Bug report by Andrew Moreton - addSubModel/removeSubmodel not working from
     * Jena 2.1 to Jena 2.2
     * Variant 1: base = no inf, import = no inf
     */
    public void test_am_01() {
        OntModel m0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        OntModel m1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );

        OntClass c = m1.createClass( NS + "c" );

        assertFalse( m0.containsResource( c ) );

        m0.addSubModel( m1 );
        assertTrue( m0.containsResource( c ) );

        m0.removeSubModel( m1 );
        assertFalse( m0.containsResource( c ) );
    }

    /**
     * Bug report by Andrew Moreton - addSubModel/removeSubmodel not working from
     * Jena 2.1 to Jena 2.2
     * Variant 2: base = inf, import = no inf
     */
    public void test_am_02() {
        OntDocumentManager.getInstance().setProcessImports( false );
        OntDocumentManager.getInstance().addAltEntry( "http://www.w3.org/TR/2003/CR-owl-guide-20030818/wine",
                                                      "file:testing/ontology/owl/Wine/wine.owl" );
        OntModel m0 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        OntModel m1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        String namespace = "http://www.w3.org/TR/2003/CR-owl-guide-20030818/wine";
        String classURI = namespace + "#Wine";
        m1.read(namespace);
        OntClass c = m1.getOntClass(classURI);

        assertFalse(m0.containsResource(c));
        m0.addSubModel(m1);
        assertTrue(m0.containsResource(c));
        m0.removeSubModel(m1);
        assertFalse(m0.containsResource(c));
    }

    /**
     * Bug report by Andrew Moreton - addSubModel/removeSubmodel not working from
     * Jena 2.1 to Jena 2.2
     * Variant 3: base = no inf, import = inf
     */
    public void test_am_03() {
        OntModel m0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        OntModel m1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RDFS_INF );

        OntClass c = m1.createClass( NS + "c" );

        assertFalse( m0.containsResource( c ) );

        m0.addSubModel( m1 );
        assertTrue( m0.containsResource( c ) );

        m0.removeSubModel( m1 );
        assertFalse( m0.containsResource( c ) );
    }

    /**
     * Bug report by Andrew Moreton - addSubModel/removeSubmodel not working from
     * Jena 2.1 to Jena 2.2
     * Variant 4: base = inf, import = inf
     */
    public void test_am_04() {
        OntModel m0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RDFS_INF );
        OntModel m1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RDFS_INF );

        OntClass c = m1.createClass( NS + "c" );

        assertFalse( m0.containsResource( c ) );

        m0.addSubModel( m1 );
        assertTrue( m0.containsResource( c ) );

        m0.removeSubModel( m1 );
        assertFalse( m0.containsResource( c ) );
    }

    /**
        The default namespace pefix of a non-base-model should not manifest as the
         default namespace prefix of the base model or the Ont model.
    */
    public void testHolgersPolyadicPrefixMappingBug()
        {
        final String IMPORTED_NAMESPACE = "http://imported#";
        final String LOCAL_NAMESPACE = "http://local#";
        Model importedModel = ModelFactory.createDefaultModel();
         importedModel.setNsPrefix("", IMPORTED_NAMESPACE);
        OntModel ontModel = ModelFactory.createOntologyModel();
        ontModel.setNsPrefix("", LOCAL_NAMESPACE);
        ontModel.addSubModel(importedModel);
        assertNull( ontModel.getNsURIPrefix(IMPORTED_NAMESPACE) );
        }

    /**
     * <p>Bug report by Tina (shilei_back06@yahoo.com.cn) - NPE in listHierarchyRootClasses
     * with generic rule reasoner.</p>
     */
    public void test_tina_01() {
        String rule = "(?x rdf:type rdfs:Class) -> (?x rdf:type owl:Class).";
        Reasoner reasoner = new GenericRuleReasoner( Rule.parseRules(rule ) );
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM);
        spec.setReasoner(reasoner);
        OntModel ml = ModelFactory.createOntologyModel( spec,null );

        Iterator<OntClass> it3= ml.listHierarchyRootClasses();
         while(it3.hasNext()){
            it3.next();
        }
     }

    /**
     * Bug report by mongolito_404 - closed models used in imports raise an exception
     */
    public void test_mongolito_01() {
        String SOURCEA=
            "<rdf:RDF" +
            "    xmlns:rdf          ='http://www.w3.org/1999/02/22-rdf-syntax-ns#'" +
            "    xmlns:owl          ='http://www.w3.org/2002/07/owl#'" +
            "    xml:base           ='http://example.com/a#'" +
            ">" +
            "  <owl:Ontology>" +
            "          <owl:imports rdf:resource='http://example.com/b' />" +
            "  </owl:Ontology>" +
            "</rdf:RDF>";

        OntDocumentManager.getInstance().addAltEntry( "http://example.com/b", "file:testing/ontology/bugs/test_dk_01.xml" );

        OntModel a0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        a0.read( new StringReader( SOURCEA ), null );
        long a0count = a0.size();

        // key step - close a model which is now in the ODM cache
        OntDocumentManager.getInstance().getModel( "http://example.com/b" ).close();

        // this line threw an exception before the bug was fixed
        OntModel a1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        a1.read( new StringReader( SOURCEA ), null );

        // for completeness, check that we have read the same contents
        assertEquals( "Models should be same size", a0count, a1.size() );
    }


    /** IsIndividual reported not to work with default rdfs reasoner
     */
    public void test_isindividual() {
        OntModel defModel = ModelFactory.createOntologyModel();
        OntClass c = defModel.createClass( "http://example.com/test#A" );
        Individual i = c.createIndividual();
        assertTrue( "i should be an individual", i.isIndividual() );
    }

    /** Reported NPE with owl lite profile */
    public void test_getClassOwlLite() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_LITE_MEM );
        // throws NPE
        m.getOntClass( "http://example.com/foo" );
    }

    /** This underpins a problem I'm having with imports processing */
    public void xxtestModelMakerOpen() {
        ModelMaker mm = ModelFactory.createMemModelMaker();
        Model m = mm.openModel( "http://example.com/foo" );
        assertTrue( m.isEmpty() );

        m.close();

        boolean closed = false;
        Model m0 = mm.openModel( "http://example.com/foo" );
        try {
            assertTrue( m0.isEmpty() );
        }
        catch (ClosedException unexpected) {
            closed = true;
        }
        assertFalse( "ModelMaker.openModel returned a closed model", closed );
    }

    /** User requested default prefixes for xsd:, and daml: is now deprecated */
    public void testDefaultPrefixeOWL() {
        OntModel m = ModelFactory.createOntologyModel();
        assertFalse( "owl:".equals( m.expandPrefix( "owl:" ) ) );
    }

    /** User requested default prefixes for xsd:, and daml: is now deprecated */
    public void testDefaultPrefixeRDF() {
        OntModel m = ModelFactory.createOntologyModel();
        assertFalse( "rdf:".equals( m.expandPrefix( "rdf:" ) ) );
    }

    /** User requested default prefixes for xsd:, and daml: is now deprecated */
    public void testDefaultPrefixeRDFS() {
        OntModel m = ModelFactory.createOntologyModel();
        assertFalse( "rdfs:".equals( m.expandPrefix( "rdfs:" ) ) );
    }

    /** User requested default prefixes for xsd:, and daml: is now deprecated */
    public void testDefaultPrefixeXSD() {
        OntModel m = ModelFactory.createOntologyModel();
        assertFalse( "xsd:".equals( m.expandPrefix( "xsd:" ) ) );
    }

    /** User requested default prefixes for xsd:, and daml: is now deprecated */
    public void testDefaultPrefixeDAML() {
        OntModel m = ModelFactory.createOntologyModel();
        assertEquals( "daml:", m.expandPrefix( "daml:" ) );
    }

    /** User requested default prefixes for xsd:, and daml: is now deprecated */
    public void testDefaultPrefixeDC() {
        OntModel m = ModelFactory.createOntologyModel();
        assertEquals( "dc:", m.expandPrefix( "dc:" ) );
    }

    /**
     * OntModel read should do content negotiation if no base URI is given
     * @param ontModel
     * @return
     */
    public void testReadConneg0() {
        final boolean[] acceptHeaderSet = new boolean[] {false};

        // because ModelCom has private fields it references directly, we have to mock
        // a lot more pieces that I would prefer
        OntModel m = new OntModelImpl(OntModelSpec.OWL_MEM) {
            @Override
            protected Model readDelegate( String url ) {
                acceptHeaderSet[0] = true;
                return super.readDelegate( url );
            }

            /** Allow pseudo-conneg even on file: uri's */
            @Override
            public boolean ignoreFileURI( String url ) {
                return false;
            }
        };

        assertFalse( acceptHeaderSet[0] );
        m.read( "file:testing/ontology/bugs/koala.owl" );
        assertTrue( acceptHeaderSet[0] );

    }

    /** No conneg for file: uri's normally */
    public void testReadConneg1() {
        final boolean[] acceptHeaderSet = new boolean[] {false};

        // because ModelCom has private fields it references directly, we have to mock
        // a lot more pieces that I would prefer
        OntModel m = new OntModelImpl(OntModelSpec.OWL_MEM) {
            @Override
            protected Model readDelegate( String url ) {
                acceptHeaderSet[0] = true;
                return super.readDelegate( url );
            }
        };

        assertFalse( acceptHeaderSet[0] );
        m.read( "file:testing/ontology/bugs/koala.owl" );
        assertFalse( acceptHeaderSet[0] );

    }

    /** With RDF/XML syntax specified, conneg */
    public void testReadConneg2() {
        final boolean[] acceptHeaderSet = new boolean[] {false};

        // because ModelCom has private fields it references directly, we have to mock
        // a lot more pieces that I would prefer
        OntModel m = new OntModelImpl(OntModelSpec.OWL_MEM) {
            @Override
            protected Model readDelegate( String url, String lang ) {
                acceptHeaderSet[0] = true;
                return super.readDelegate( url, lang );
            }

            /** Allow pseudo-conneg even on file: uri's */
            @Override
            public boolean ignoreFileURI( String url ) {
                return false;
            }
        };

        assertFalse( acceptHeaderSet[0] );
        m.read( "file:testing/ontology/bugs/koala.owl", "RDF/XML" );
        assertTrue( acceptHeaderSet[0] );

    }

    /** With a base URI, no conneg */
    public void testReadConneg3() {
        final boolean[] acceptHeaderSet = new boolean[] {false};

        // because ModelCom has private fields it references directly, we have to mock
        // a lot more pieces that I would prefer
        OntModel m = new OntModelImpl(OntModelSpec.OWL_MEM) {
            @Override
            protected Model readDelegate( String url, String lang ) {
                acceptHeaderSet[0] = true;
                return super.readDelegate( url, lang );
            }

            /** Allow pseudo-conneg even on file: uri's */
            @Override
            public boolean ignoreFileURI( String url ) {
                return false;
            }
        };

        assertFalse( acceptHeaderSet[0] );
        m.read( "file:testing/ontology/bugs/koala.owl", "http://foo.com", "RDF/XML" );
        assertFalse( acceptHeaderSet[0] );

    }

    /** User report of builtin classes showing up as individuals */
    public void testIsIndividual1() {
        String NS = "http://jena.hpl.hp.com/example#";
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        OntClass c1 = m.createClass(NS + "C1");

        for (Iterator<OntClass> it = m.listClasses(); it.hasNext(); ) {
            OntClass ontClass = it.next();
            assertFalse( ontClass.getLocalName() + "should not be an individual", ontClass.isIndividual() );
        }
    }

    public void testIsIndividual2() {
        String NS = "http://jena.hpl.hp.com/example#";
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF);

        OntClass c1 = m.createClass(NS + "C1");

        for (Iterator<OntClass> it=m.listClasses(); it.hasNext(); ) {
            OntClass ontClass = it.next();
            assertFalse( ontClass.getLocalName() + "should not be an individual", ontClass.isIndividual() );
        }
    }

    /** Edge case - suppose we imagine that user has materialised results of offline inference */
    public void testIsIndividual3() {
        String NS = "http://jena.hpl.hp.com/example#";
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

        OntClass c1 = m.createClass(NS + "C1");
        m.add( OWL.Class, RDF.type, OWL.Class );

        for (Iterator<OntClass> it = m.listClasses(); it.hasNext(); ) {
            OntClass ontClass = it.next();
            assertFalse( ontClass.getLocalName() + " should not be an individual", ontClass.isIndividual() );
        }
    }

    public void testIsIndividual4() {
        String NS = "http://jena.hpl.hp.com/example#";
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

        OntClass c1 = m.createClass(NS + "C1");
        m.add( OWL.Class, RDF.type, RDFS.Class );

        for (Iterator<OntClass> it = m.listClasses(); it.hasNext(); ) {
            OntClass ontClass = it.next();
            assertFalse( ontClass.getLocalName() + " should not be an individual", ontClass.isIndividual() );
        }
    }

    public void testIsIndividual5() {
        String NS = "http://jena.hpl.hp.com/example#";
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);

        OntClass c1 = m.createClass(NS + "C1");
        m.add( RDFS.Class, RDF.type, RDFS.Class );

        for (Iterator<OntClass> it = m.listClasses(); it.hasNext(); ) {
            OntClass ontClass = it.next();
            assertFalse( ontClass.getLocalName() + " should not be an individual", ontClass.isIndividual() );
        }
    }

    /** But we do allow punning */
    public void testIsIndividual6a() {
        String NS = "http://jena.hpl.hp.com/example#";
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        OntClass punned = m.createClass(NS + "C1");
        OntClass c2 = m.createClass(NS + "C2");
        m.add( punned, RDF.type, c2 ); // punned is a class and and instance of c2

        assertFalse( "should not be an individual", c2.isIndividual() );
        assertTrue(  "should be an individual", punned.isIndividual() );
    }

    public void testIsIndividual6b() {
        String NS = "http://jena.hpl.hp.com/example#";
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);

        OntClass punned = m.createClass(NS + "C1");
        OntClass c2 = m.createClass(NS + "C2");
        m.add( punned, RDF.type, c2 ); // punned is a class and and instance of c2

        assertFalse( "should not be an individual", c2.isIndividual() );
        assertTrue(  "should be an individual", punned.isIndividual() );
    }

    /* Bugrep from David Patterson - classes as individuals in OWL Lite */
    public void testDP0() {
        String NS = "http://jena.hpl.hp.com/example#";

        OntModelSpec mySpec = OntModelSpec.OWL_LITE_MEM_TRANS_INF; // classes are also individuals
        OntModel model = ModelFactory.createOntologyModel( mySpec );

        OntClass book = model.createClass( NS + "Book" );
        OntProperty title = model.createOntProperty( NS + "title" );
        ObjectProperty publisher = model.createObjectProperty( NS + "publisher" );
        DatatypeProperty price = model.createDatatypeProperty( NS + "price" );

        Individual sc1 = model.createIndividual( NS + "Ant: The Definitive Guide", book );

        model.setStrictMode( true );

        assertFalse( book + " should not be an individual", book.canAs( Individual.class ));
        assertFalse( title + " should not be an individual", title.canAs( Individual.class ));
        assertFalse( publisher + " should not be an individual", publisher.canAs( Individual.class ));
        assertFalse( price + " should not be an individual", price.canAs( Individual.class ));
    }

    /* Bugrep from Eniz Soztutar: OntProperty.listSuperProperties() should rule out reflexive case, for
     * symmetry with OntClass.listSuperClasses(). */
    public void testES0() {
        OntModel m = ModelFactory.createOntologyModel();
        OntProperty p = m.createOntProperty( NS+"p" );

        List<? extends OntProperty> sp = p.listSuperProperties().toList();
        assertFalse( "super-properties should not include reflexive case", sp.contains( p ) );
    }

    /** Bugrep from Benson Margulies: see
     * <a href="https://issues.apache.org/jira/browse/JENA-21">JENA-21</a>
     */
    public void testBM0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RDFS_INF );
        // should not throw NPE:
        m.listStatements( null, null, (RDFNode) null, null );
    }


    // Internal implementation methods
    //////////////////////////////////

    private int getStatementCount(OntModel ontModel) {
        int count = 0;
        for (Iterator<Statement> it = ontModel.listStatements(); it.hasNext(); it.next()) {
            count++;
        }
        return count;
    }

    //==============================================================================
    // Inner class definitions
    //==============================================================================

    static class MockTransactionHandler extends SimpleTransactionHandler {
        boolean m_inTransaction = false;
        boolean m_aborted = false;
        boolean m_committed = false;

        @Override
        public void begin() {
            m_inTransaction = true;
        }
        @Override
        public void abort() {
            m_inTransaction = false;
            m_aborted = true;
        }
        @Override
        public void commit() {
            m_inTransaction = false;
            m_committed = true;
        }
    }

}

/*
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
