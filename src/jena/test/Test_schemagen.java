/*****************************************************************************
 * File               Test_schemagen.java
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            8 Sep 2006
 * Filename           $RCSfile: Test_schemagen.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2006-09-10 23:48:33 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package jena.test;


// Imports
///////////////
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import jena.schemagen;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;


/**
 * <p>
 * Unit tests for schemagen
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: Test_schemagen.java,v 1.1 2006-09-10 23:48:33 ian_dickinson Exp $
 */
public class Test_schemagen
    extends TestCase
{
    // Constants
    //////////////////////////////////

    String PREFIX = "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
            "@prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
            "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
            "@prefix ex: <http://example.com/sg#> .\n";

    // Static variables
    //////////////////////////////////

    private static Log log = LogFactory.getLog( Test_schemagen.class );

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    public void testNoBaseURI0() {
        String SOURCE = PREFIX + "ex:A a owl:Class .";
        boolean ex = false;
        try {
            testSchemagenOutput( SOURCE, null,
                                 new String[] {},
                                 new String[] {},
                                 new String[] {} );
        }
        catch (RuntimeException e) {
            assertEquals( "Could not determine the base URI for the input vocabulary", e.getMessage() );
            ex = true;
        }

        assertTrue( "Expected abort", ex );
    }

    public void testClass0() {
        String SOURCE = PREFIX + "ex:A a owl:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             new String[] {".*public static final Resource A.*"},
                             new String[] {} );
    }

    public void testClass1() {
        String SOURCE = PREFIX + "ex:A a rdfs:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             new String[] {},
                             new String[] {".*public static final Resource A.*"} );
    }

    public void testClass2() {
        String SOURCE = PREFIX + "ex:A a owl:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs"},
                             new String[] {},
                             new String[] {".*public static final Resource A.*"} );
    }

    public void testClass3() {
        String SOURCE = PREFIX + "ex:A a rdfs:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs"},
                             new String[] {".*public static final Resource A.*"},
                             new String[] {} );
    }

    public void testProperty0() {
        String SOURCE = PREFIX + "ex:p a owl:ObjectProperty .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             new String[] {".*public static final Property p.*"},
                             new String[] {} );
    }

    public void testProperty1() {
        String SOURCE = PREFIX + "ex:p a rdf:Property .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             // in OWL mode we permit rdf:properties
                             new String[] {".*public static final Property p.*"},
                             new String[] {} );
    }

    public void testProperty2() {
        String SOURCE = PREFIX + "ex:p a owl:ObjectProperty .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs"},
                             new String[] {},
                             new String[] {".*public static final Property p.*"} );
    }

    public void testProperty3() {
        String SOURCE = PREFIX + "ex:p a rdf:Property .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs"},
                             new String[] {".*public static final Property p.*"},
                             new String[] {} );
    }

    public void testInstance0() {
        String SOURCE = PREFIX + "ex:A a owl:Class . ex:i a ex:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             new String[] {".*public static final Resource i.*"},
                             new String[] {} );
    }

    public void testInstance1() {
        String SOURCE = PREFIX + "ex:A a rdfs:Class . ex:i a ex:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             new String[] {".*public static final Resource i.*"},
                             new String[] {} );
    }

    /* TODO this test fails, because the isInstance check in schemagen is quite weak.
     * Consider whether to fix the test or the code... *
    public void testInstance2() {
        String SOURCE = PREFIX + "ex:A a owl:Class . ex:i a ex:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs"},
                             new String[] {},
                             new String[] {".*public static final Resource i.*"} );
    }
    */

    public void testInstance3() {
        String SOURCE = PREFIX + "ex:A a rdfs:Class . ex:i a ex:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs"},
                             new String[] {".*public static final Resource i.*"},
                             new String[] {} );
    }

    /** Bug report by Richard Cyganiak */
    public void testRC0() {
        String SOURCE = PREFIX + "ex:class a owl:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             new String[] {},
                             new String[] {".*public static final Resource class .*"} );
    }


    // Internal implementation methods
    //////////////////////////////////

    /**
     *
     * @param source String defining the model, using N3
     * @param sg The schemagen object to test, or null for a default
     * @param args list of args to pass to SG
     * @param posPatterns array of regexps that must match at least once in the output
     * @param negPatterns arrays of regexps that must not match the output
     * @return The string defining the java class
     */
    protected String testSchemagenOutput( String source, SchemaGenAux sg, String[] args, String[] posPatterns, String[] negPatterns ) {
        sg = (sg == null) ? new SchemaGenAux() : sg;

        Model m = ModelFactory.createDefaultModel();
        m.read(  new StringReader( source ), "http://example.com/sg#", "N3" );
        sg.setSource( m );

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        sg.setOutput( new PrintStream( buf ) );

        // run schemagen
        sg.testGo( args );

        // now run the test pattern over the lines in the file
        String result = buf.toString();
        if (log.isDebugEnabled()) {
            log.debug(  result );
        }
        StringTokenizer tokens = new StringTokenizer( result, "\n" );

        boolean[] foundPos = new boolean[posPatterns.length];

        // look for any line that matches the patterns
        while (tokens.hasMoreTokens()) {
            String line = tokens.nextToken();

            // try each positive pattern
            for (int i = 0; i < posPatterns.length; i++) {
                Pattern pat = Pattern.compile( posPatterns[i] );
                foundPos[i] |= pat.matcher( line ).matches();
            }

            // try each negative pattern
            for (int i = 0; i < negPatterns.length; i++) {
                Pattern pat = Pattern.compile( negPatterns[i] );
                assertFalse( "negative match pattern ||" + negPatterns[i] + "|| matched on line: " + line,
                             pat.matcher( line ).matches() );
            }
        }

        for (int i = 0; i < posPatterns.length; i++) {
            String msg = "Expecting a positive match to pattern: ||" + posPatterns[i] + "||";
            assertTrue( msg, foundPos[i] );
        }

        return result;
    }


    //==============================================================================
    // Inner class definitions
    //==============================================================================

    static class SchemaGenAux
        extends schemagen
    {
        protected PrintStream m_auxOutput;
        protected Model m_auxSource;
        public void setOutput( PrintStream out ) {
            m_auxOutput = out;
        }
        public void setSource( Model m ) {
            m_auxSource = m;
        }

        // override the behaviours from schemagen
        protected void selectInput() {
            m_source.add( m_auxSource );
        }
        protected void selectOutput() {
            m_output = m_auxOutput;
        }

        public void testGo( String[] args ) {
            go( args );
        }

        // option faking
        protected String getValue( Object option ) {
            if (option.equals( OPT_INPUT )) {
                return "http://example.org/sg";
            }
            else {
                return super.getValue( option );
            }
        }

        protected Resource getResource( Object option ) {
            if (option.equals( OPT_INPUT )) {
                return ResourceFactory.createResource( "http://example.org/sg" );
            }
            else {
                return super.getResource( option );
            }
        }

        protected void abort( String msg, Exception e ) {
            throw new RuntimeException( msg, e );
        }
    }
}



/*
 * (c) Copyright 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
