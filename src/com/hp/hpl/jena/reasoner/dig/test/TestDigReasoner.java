/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            @package@
 * Web site           @website@
 * Created            21-Nov-2003
 * Filename           $RCSfile: TestDigReasoner.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-12-01 22:40:07 $
 *               by   $Author: ian_dickinson $
 *
 * @copyright@
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig.test;



// Imports
///////////////
import org.w3c.dom.*;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.reasoner.dig.*;

import junit.framework.*;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.parsers.DocumentBuilder;


/**
 * <p>
 * Abstract test harness for DIG reasoners 
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: TestDigReasoner.java,v 1.2 2003-12-01 22:40:07 ian_dickinson Exp $)
 */
public class TestDigReasoner 
    extends TestCase
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    protected Model m_base;
    
    // Constructors
    //////////////////////////////////

    public TestDigReasoner( String name ) {
        super( name );
    }
    
    
    
    // External signature methods
    //////////////////////////////////
    
    public static TestSuite suite() {
        TestSuite s = new TestSuite( "TestDigReasoner" );
        
        //buildConceptLangSuite( "testing/ontology/dig/owl/cl", OntModelSpec.OWL_MEM, s );
        buildBasicQuerySuite( "testing/ontology/dig/owl/basicq", OntModelSpec.OWL_MEM, s );

        return s;
    }

    private static void buildConceptLangSuite( String root, OntModelSpec spec, TestSuite s ) {
        int i = 0;
        while (true) {
            File testSource = new File( root + "/test_" + i + ".source.xml" );
            File testTarget = new File( root + "/test_" + i + ".xml" );
            
            if (!testSource.exists()) {
                break;
            }
            else {
                i++;
            }
            
            s.addTest( new DigTranslationTest( testSource, testTarget, spec ) );
        }
    }
    
    private static void buildBasicQuerySuite( String root, OntModelSpec spec, TestSuite s ) {
        int i = 0;
        while (true) {
            File testSource = new File( root + "/test_" + i + ".source.xml" );
            File testQuery = new File( root + "/test_" + i + ".query.xml" );
            File testTarget = new File( root + "/test_" + i + ".result.xml" );
            
            if (!testSource.exists()) {
                break;
            }
            else {
                i++;
            }
            
            s.addTest( new DigBasicQueryTest( testSource, testTarget, testQuery, spec ) );
        }
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    
    //==============================================================================
    // Inner class definitions
    //==============================================================================

    private static class AbstractDigTest
        extends TestCase
    {
        public AbstractDigTest( String name ) {
            super( name );
        }
        
        /** This is a simple test that test xml structure isomorphism on elements and attributes */
        protected void xmlEqualityTest( Document source, Document target ) {
            // test both ways round to ensure compatability
            assertTrue( "Failed to match source to target documents", xmlEqualityTest( source.getDocumentElement(), target.getDocumentElement() ) );
            assertTrue( "Failed to match target to source documents", xmlEqualityTest( target.getDocumentElement(), source.getDocumentElement() ) );
        }
    
        private boolean xmlEqualityTest( Element source, Element target ) {
            boolean match = source.getNodeName().equals( target.getNodeName() );
            NodeList children = source.getChildNodes();
            
            for (int i = 0;  match && i < children.getLength(); i++) {
                Node child = children.item( i );
                // we're only looking at structural equivalence - elements and attributes
                if (child instanceof Element) {
                    match = findElementMatch( (Element) child, target );
                }
            }
            
            NamedNodeMap attrs = source.getAttributes();
            
            for (int i = 0;  match && i < attrs.getLength(); i++) {
                match = findAttributeMatch( (Attr) attrs.item( i ), target );
            }
            
            return match;
        }
    
    
        private boolean findElementMatch( Element sourceChild, Element target ) {
            boolean found = false;

            NodeList targetChildren = target.getElementsByTagName( sourceChild.getNodeName() );
        
            for (int i = 0;  !found && i < targetChildren.getLength();  i++) {
                Node targetChild = targetChildren.item( i );
                
                if (targetChild instanceof Element && sourceChild.getNodeName().equals( targetChild.getNodeName() )) {
                    // we have found an element with the same name - see if it matches
                    found = xmlEqualityTest( sourceChild, (Element) targetChild );
                }
            }
        
            return found;
        }
    
    
        private boolean findAttributeMatch( Attr child, Element target ) {
            return child.getValue().equals( target.getAttribute( child.getName() ) );
        }
    }
    
    
    private static class DigTranslationTest
        extends AbstractDigTest
    {
        private File m_source;
        private File m_target;
        private OntModelSpec m_spec;
        
        DigTranslationTest( File source, File target, OntModelSpec spec ) {
            super( source.getName() );
            m_source = source;
            m_target = target;
            m_spec = spec;
        }
        
        public void runTest()
            throws Exception 
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            
            Model m = ModelFactory.createDefaultModel();
            m.read( new FileInputStream( m_source ), null );
            DIGAdapter da = new DIGAdapter( m_spec, m.getGraph() );
            
            Document targetD = builder.parse( m_target );
            Document sourceD = da.translateKbToDig();
            
            // debug da.serialiseDocument( sourceD, new PrintWriter( System.out ));
            
            xmlEqualityTest( sourceD, targetD );
        }
    }
    
    
    private static class DigBasicQueryTest
        extends AbstractDigTest
    {
        private File m_source;
        private File m_target;
        private File m_query;
        private OntModelSpec m_spec;
        
        DigBasicQueryTest( File source, File target, File query, OntModelSpec spec ) {
            super( source.getName() );
            m_source = source;
            m_target = target;
            m_query = query;
            m_spec = spec;
        }
        
        public void runTest()
            throws Exception 
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            
            Model m = ModelFactory.createDefaultModel();
            m.read( new FileInputStream( m_source ), null );
            DIGAdapter da = new DIGAdapter( m_spec, m.getGraph() );
            
            // upload 
            da.resetKB();
            boolean warn  = !da.uploadKB();
            if (warn) {
                System.err.println( "00 Warning!" );
                for (Iterator i = da.getConnection().getWarnings(); i.hasNext(); ) {
                    System.err.println( i.next() );
                }
                assertFalse( "Should not be upload warnings", warn );
            }
                        
            Document queryD = builder.parse( m_query );
            Document targetD = builder.parse( m_target );
            Document resultD = da.getConnection().sendDigVerb( queryD, da.getProfile() );
            
            da.getConnection().errorCheck( resultD );
            assertFalse( "Should not be warnings", da.getConnection().warningCheck( resultD ) );
            
            System.out.println( m_source.getPath() );
            da.getConnection().serialiseDocument( resultD, new PrintWriter( System.out ));
            System.out.println();
            
            da.close();
            xmlEqualityTest( resultD, targetD );
        }
    }
}


/*
@footer@
*/
