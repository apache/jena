/*****************************************************************************
 * File               Test_schemagen.java
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            8 Sep 2006
 * Filename           $RCSfile: Test_schemagen.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2007-06-11 13:34:44 $
 *               by   $Author: chris-dollin $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package jena.test;


// Imports
///////////////
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import jena.schemagen;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileUtils;


/**
 * <p>
 * Unit tests for schemagen
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: Test_schemagen.java,v 1.5 2007-06-11 13:34:44 chris-dollin Exp $
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

    public void testNoBaseURI0() throws Exception {
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

    public void testClass0() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             new String[] {".*public static final Resource A.*"},
                             new String[] {} );
    }

    public void testClass1() throws Exception {
        String SOURCE = PREFIX + "ex:A a rdfs:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             new String[] {},
                             new String[] {".*public static final Resource A.*"} );
    }

    public void testClass2() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs"},
                             new String[] {},
                             new String[] {".*public static final Resource A.*"} );
    }

    public void testClass3() throws Exception {
        String SOURCE = PREFIX + "ex:A a rdfs:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs"},
                             new String[] {".*public static final Resource A.*"},
                             new String[] {} );
    }

    public void testProperty0() throws Exception {
        String SOURCE = PREFIX + "ex:p a owl:ObjectProperty .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             new String[] {".*public static final Property p.*"},
                             new String[] {} );
    }

    public void testProperty1() throws Exception {
        String SOURCE = PREFIX + "ex:p a rdf:Property .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             // in OWL mode we permit rdf:properties
                             new String[] {".*public static final Property p.*"},
                             new String[] {} );
    }

    public void testProperty2() throws Exception {
        String SOURCE = PREFIX + "ex:p a owl:ObjectProperty .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs"},
                             new String[] {},
                             new String[] {".*public static final Property p.*"} );
    }

    public void testProperty3() throws Exception {
        String SOURCE = PREFIX + "ex:p a rdf:Property .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs"},
                             new String[] {".*public static final Property p.*"},
                             new String[] {} );
    }

    public void testInstance0() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class . ex:i a ex:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             new String[] {".*public static final Resource i.*"},
                             new String[] {} );
    }

    public void testInstance1() throws Exception {
        String SOURCE = PREFIX + "ex:A a rdfs:Class . ex:i a ex:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             new String[] {".*public static final Resource i.*"},
                             new String[] {} );
    }

    /* TODO this test fails, because the isInstance check in schemagen is quite weak.
     * Consider whether to fix the test or the code... *
    public void testInstance2() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class . ex:i a ex:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs"},
                             new String[] {},
                             new String[] {".*public static final Resource i.*"} );
    }
    */

    public void testInstance3() throws Exception {
        String SOURCE = PREFIX + "ex:A a rdfs:Class . ex:i a ex:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs"},
                             new String[] {".*public static final Resource i.*"},
                             new String[] {} );
    }

    /** Bug report by Richard Cyganiak */
    public void testRC0() throws Exception {
        String SOURCE = PREFIX + "ex:class a owl:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             new String[] {},
                             new String[] {".*public static final Resource class .*"} );
    }


    public void testComment0() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class ; rdfs:comment \"commentcomment\" .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             new String[] {" */\\*\\* <p>commentcomment</p> \\*/ *"},
                             new String[] {} );
    }

    public void testComment1() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class ; rdfs:comment \"commentcomment\" .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl", "--nocomments"},
                             new String[] {},
                             new String[] {" */\\*\\* <p>commentcomment</p> \\*/ *"} );
    }

    public void testOntClass0() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl", "--ontology"},
                             new String[] {".*public static final OntClass A.*"},
                             new String[] {} );
    }

    public void testOntClass1() throws Exception {
        String SOURCE = PREFIX + "ex:A a rdfs:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl", "--ontology"},
                             new String[] {},
                             new String[] {".*public static final OntClass A.*"} );
    }

    public void testOntClass2() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs", "--ontology"},
                             new String[] {},
                             new String[] {".*public static final OntClass A.*"} );
    }

    public void testOntClass3() throws Exception {
        String SOURCE = PREFIX + "ex:A a rdfs:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs", "--ontology"},
                             new String[] {".*public static final OntClass A.*"},
                             new String[] {} );
    }

    public void testOntProperty0() throws Exception {
        String SOURCE = PREFIX + "ex:p a owl:ObjectProperty .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl", "--ontology"},
                             new String[] {".*public static final ObjectProperty p.*"},
                             new String[] {} );
    }

    public void testOntProperty1() throws Exception {
        String SOURCE = PREFIX + "ex:p a rdf:Property .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl", "--ontology"},
                             // in OWL mode we permit rdf:properties
                             new String[] {".*public static final OntProperty p.*"},
                             new String[] {} );
    }

    public void testOntProperty2() throws Exception {
        String SOURCE = PREFIX + "ex:p a owl:ObjectProperty .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs", "--ontology"},
                             new String[] {},
                             new String[] {".*public static final ObjectProperty p.*"} );
    }

    public void testOntProperty3() throws Exception {
        String SOURCE = PREFIX + "ex:p a rdf:Property .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs", "--ontology"},
                             new String[] {".*public static final OntProperty p.*"},
                             new String[] {} );
    }

    public void testHeader() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--ontology", "--header", "/* header */\n%package%\n%imports%\n"},
                             new String[] {"/\\* header \\*/"},
                             new String[] {} );
    }

    public void testFooter() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--ontology", "--footer", "/* footer */"},
                             new String[] {"/\\* footer \\*/"},
                             new String[] {} );
    }

    public void testPackage() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--ontology", "--package", "test.test"},
                             new String[] {"package test.test;\\s*"},
                             new String[] {} );
    }

    public void testClassname() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class .";
        SchemaGenAux fixture = new SchemaGenAux() {
            protected String getValue( Object option ) {
                if (option.equals( OPT_INPUT )) {
                    // without the -n option, this will force the classname to be Soggy
                    return "http://example.org/soggy";
                }
                else {
                    return super.getValue( option );
                }
            }
        };

        testSchemagenOutput( SOURCE, fixture,
                             new String[] {"-a", "http://example.com/soggy#", "--ontology", "--package", "test.test", "-n", "Sg"},
                             new String[] {},
                             new String[] {} );
    }

    public void testClassdec() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--ontology", "--classdec", "\n    implements java.lang.Cloneable\n"},
                             new String[] {"\\s*implements java.lang.Cloneable\\s*"},
                             new String[] {} );
    }

    public void testDeclarations() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--ontology", "--declarations", "protected String m_gnole = \"Fungle\";;\n"},
                             new String[] {".*Fungle.*"},
                             new String[] {} );
    }

    public void testNoClasses() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--ontology", "--noclasses"},
                             new String[] {},
                             new String[] {".*OntClass A.*"} );
    }

    public void testNoProperties() throws Exception {
        String SOURCE = PREFIX + "ex:p a owl:ObjectProperty .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl", "--ontology", "--noproperties"},
                             new String[] {},
                             new String[] {".*Property p.*"} );
    }

    public void testNoIndividuals() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class . ex:i a ex:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl", "--noindividuals"},
                             new String[] {".*Resource A.*"},
                             new String[] {".*Resource i.*"} );
    }

    public void testNoHeader() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class . ex:i a ex:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl", "--noheader"},
                             new String[] {},
                             new String[] {"/\\*\\*.*"} );
    }

    public void testUCNames() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class . ex:i a ex:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl", "--uppercase"},
                             new String[] {".*Resource A.*",".*Resource I.*"},
                             new String[] {} );
    }

    public void testInference0() throws Exception {
        String SOURCE = PREFIX + "ex:p rdfs:domain ex:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             new String[] {},
                             new String[] {".*Resource A.*",".*Property p.*"} );
    }

    public void testInference1() throws Exception {
        String SOURCE = PREFIX + "ex:p rdfs:domain ex:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl", "--inference"},
                             new String[] {".*Resource A.*",".*Property p.*"},
                             new String[] {} );
    }

    public void testInference2() throws Exception {
        String SOURCE = PREFIX + "ex:p rdfs:domain ex:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs", "--inference"},
                             new String[] {".*Resource A.*",".*Property p.*"},
                             new String[] {} );
    }

    public void testStrictIndividuals0() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class . ex:i a ex:A . <http://example.com/different#j> a ex:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs"},
                             new String[] {".*Resource i.*",".*Resource j.*"},
                             new String[] {} );
    }

    public void testStrictIndividuals1() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class . ex:i a ex:A . <http://example.com/different#j> a ex:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs", "--strictIndividuals"},
                             new String[] {".*Resource i.*"},
                             new String[] {".*Resource j.*"} );
    }

    public void testLineEnd0() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class . ex:i a ex:A . ex:p a rdf:Property .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs", "--strictIndividuals"},
                             new String[] {},
                             new String[] {".*\r.*"} );
    }

    public void testLineEnd1() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class . ex:i a ex:A . ex:p a rdf:Property .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs", "--dos"},
                             new String[] {".*\\r"},
                             new String[] {".*[^\r]"} );
    }


    // Internal implementation methods
    //////////////////////////////////

    /**
     * Test the output from schemagen by saving the output to a string,
     * then ensuring that every positive regex matches at least one line, and
     * every negative regex matches at most no lines. Also checks that
     * compiling the file does not cause any errors.
     *
     * @param source String defining the model, using N3
     * @param sg The schemagen object to test, or null for a default
     * @param args list of args to pass to SG
     * @param posPatterns array of regexps that must match at least once in the output
     * @param negPatterns arrays of regexps that must not match the output
     * @return The string defining the java class
     */
    protected String testSchemagenOutput( String source, SchemaGenAux sg, String[] args,
                                          String[] posPatterns, String[] negPatterns )
        throws Exception
    {
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
            assertTrue( msg + " in:\n" + result, foundPos[i] );
        }

        // check that the file compiles with javac
        testCompile( result, "Sg" );

        return result;
    }

    /**
     * Test the compilability of the generated output string by saving it to a
     * class file, and invoking javac on that file.
     * @param source
     * @param className
     * @throws Exception
     */
    protected void testCompile( String source, String className )
        throws Exception
    {
        // first write the source file to a temp dir
        File tmpDir = FileUtils.getScratchDirectory( "schemagen" );
        File srcFile = new File( tmpDir, className + ".java" );
        FileWriter out = new FileWriter( srcFile );
        out.write(  source );
        out.close();

        // now get ready to invoke javac
        try {
            Class jcMain = Class.forName(  "sun.tools.javac.Main" );

            // constructor
            Constructor jcConstruct = jcMain.getConstructor( new Class[] {OutputStream.class, String.class} );
            Method jcCompile = jcMain.getMethod( "compile", new Class[] {String[].class} );
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            Object jc = jcConstruct.newInstance( new Object[] {byteOut, "javac"} );

            // build the args list for javac
            String[] args = new String[] {"-classpath", getClassPath( tmpDir ), "-d", tmpDir.getPath(), srcFile.getPath() };

            Boolean success = (Boolean) jcCompile.invoke( jc, new Object[] {args} );
            log.debug( "compiled - success = " + success );
            log.debug( "message = " + byteOut.toString() );
            assertTrue( "Errors reported from compilation of schemagen output", success.booleanValue() );
        }
        catch (ClassNotFoundException nf) {
            log.debug( "sun.tools.java.Main not found (no tools.jar on classpath?). schemagen compilation test skipped." );
        }

        // clean up
        srcFile.deleteOnExit();
        new File( tmpDir, className + ".class" ).deleteOnExit();
        tmpDir.deleteOnExit();
    }

    /**
     * answer the classpath we can use to compile the sg output files
     * @param tmpDir
     * @return
     */
    protected String getClassPath( File tmpDir ) {
        return System.getProperty ("java.class.path") +
               System.getProperty ("path.separator") +
               tmpDir.getPath();
    }

    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /**
     * An extension to standard schemagen to create a test fixture; we override the
     * input and output methods.
     */
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
            // call super to allow option processing
            super.selectOutput();
            // then override the result
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
 * (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
