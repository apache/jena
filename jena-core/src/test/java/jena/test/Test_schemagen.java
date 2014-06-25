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

// Package
///////////////
package jena.test;


// Imports
///////////////
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

import jena.schemagen;
import jena.schemagen.SchemagenOptionsImpl;
import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileUtils;


/**
 * <p>
 * Unit tests for schemagen
 * </p>
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

    private static Logger log = LoggerFactory.getLogger( Test_schemagen.class );

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /** This test used to fail with an abort, but we now guess the NS based on prevalence */
    public void testNoBaseURI0() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {},
                             new String[] {".*public static final Resource A =.*"},
                             new String[] {} );
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

    /** Bug report by Brian: instance is in the namespace, but the class itself is not */
    public void testInstance4() throws Exception {
        String SOURCE = PREFIX + "@prefix ex2: <http://example.org/otherNS#>. ex2:A a rdfs:Class . ex:i a ex2:A .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--rdfs"},
                             new String[] {".*public static final Resource i.*"},
                             new String[] {} );
    }

    /** Bug report by Brian: instances not being recognised */
    public void testInstance5() throws Exception {
        String SOURCE = "@prefix :        <http://ontology.earthster.org/eco/impact#> .\n" +
                "@prefix core:    <http://ontology.earthster.org/eco/core#> .\n" +
                "@prefix ecoinvent:  <http://ontology.earthster.org/eco/ecoinvent#> .\n" +
                "@prefix owl:     <http://www.w3.org/2002/07/owl#> .\n" +
                "@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
                "@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                "@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .\n" +
                "\n" +
                "<http://ontology.earthster.org/eco/impact>\n" +
                "      rdf:type owl:Ontology ;\n" +
                "      owl:imports <http://ontology.earthster.org/eco/ecoinvent> , <http://ontology.earthster.org/eco/core> ;\n" +
                "      owl:versionInfo \"Created with TopBraid Composer\"^^xsd:string .\n" +
                "\n" +
                ":CD-CML2001-AbioticDepletion\n" +
                "      rdf:type core:ImpactAssessmentMethodCategoryDescription ;\n" +
                "      rdfs:label \"abiotic resource depletion\"^^xsd:string ;\n" +
                "      core:hasImpactCategory\n" +
                "              :abioticDepletion .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"--owl", "--inference"},
                             new String[] {".*public static final Resource CD_CML2001_AbioticDepletion.*"},
                             new String[] {".*valtype.*"} );
    }

    public void testDatatype0() throws Exception {
        String SOURCE = PREFIX + "ex:d a rdfs:Datatype . ex:d rdfs:comment \"custom datatype\" .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl"},
                             new String[] {".*public static final Resource d.*"},
                             new String[] {} );
    }
    
    public void testDatatype1() throws Exception {
        String SOURCE = PREFIX + "ex:d a rdfs:Datatype . ex:d rdfs:comment \"custom datatype\" .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl", "--nodatatypes"},
                             new String[] {},
                             new String[] {".*public static final Resource d.*"} );
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

    public void testComment2() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class ; rdfs:comment \"commentcomment\" .";

        // we don't want the input fixed to be http://example.com/sg
        SchemaGenAux sga = new SchemaGenAux() {
            @Override
            protected void go( String[] args ) {
                go( new SchemagenOptionsImpl( args ) );
            }
        };
        testSchemagenOutput( SOURCE, sga,
                             new String[] {"-a", "http://example.com/sg#", "--owl", "-i", "file:\\\\C:\\Users\\fubar/vocabs/test.ttl"},
                             new String[] {".*Vocabulary definitions from file:\\\\\\\\C:\\\\Users\\\\fubar/vocabs/test.ttl.*"},
                             new String[] {} );
    }

    public void testComment3() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class ; rdfs:comment \"commentcomment\" .";

        // we don't want the input fixed to be http://example.com/sg
        SchemaGenAux sga = new SchemaGenAux() {
            @Override
            protected void go( String[] args ) {
                go( new SchemagenOptionsImpl( args ) );
            }
        };
        testSchemagenOutput( SOURCE, sga,
                             new String[] {"-a", "http://example.com/sg#", "--owl", "-i", "C:\\Users\\fubar/vocabs/test.ttl"},
                             new String[] {".*Vocabulary definitions from C:\\\\Users\\\\fubar/vocabs/test.ttl.*"},
                             new String[] {} );
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
            @Override
            protected void go( String[] args ) {
                SchemagenOptionsFixture sgf = new SchemagenOptionsFixture( args ) {
                    @Override
                    public Resource getInputOption() {
                        return ResourceFactory.createResource( "http://example.org/soggy" );
                    }
                };
                go( sgf );
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

    public void testIncludeSource0() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class . ex:i a ex:A . ex:p a owl:ObjectProperty .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl", "--includeSource"},
                             new String[] {".*private static final String SOURCE.*",
                                            ".*ex:A *(a|rdf:type) *owl:Class.*"} ,
                             new String[] {} );
    }

    public void testIncludeSource1() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class ; rdfs:comment \"comment\".";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-a", "http://example.com/sg#", "--owl", "--includeSource"},
                             new String[] {".*\\\\\"comment\\\\\".*\""},
                             new String[] {} );
    }


    public void testIncludeSource2() throws Exception {
        // had a report of the following not compiling ....
        String SOURCE = PREFIX + "@prefix skos: <http://www.w3.org/2004/02/skos/core#>.\n" +
                       " <http://purl.org/dc/elements/1.1/relation> skos:note \"\"\"A second property with the same name as this property has been declared in the dcterms: namespace (http://purl.org/dc/terms/).  See the Introduction to the document \"DCMI Metadata Terms\" (http://dublincore.org/documents/dcmi-terms/) for an explanation.\"\"\".";

        testSchemagenOutput( SOURCE, null,
                new String[] {"-a", "http://example.com/sg#", "--owl", "--includeSource"},
                new String[] {},
                new String[] {} );

    }

    public void testIncludeSource3() throws Exception {
        // multiple literals on one line can cause double-quote issues
        String SOURCE = PREFIX +
                       " ex:foo a ex:Foo; rdfs:label \"thing called foo\"@en, \"le foo\"@fr, \"das foo\"@de. ";

        testSchemagenOutput( SOURCE, null,
                new String[] {"-a", "http://example.com/sg#", "--rdfs", "--includeSource"},
                new String[] {},
                new String[] {} );

    }

    public void testConfigFile() throws Exception {
        String SOURCE = PREFIX + "ex:A a owl:Class .";
        testSchemagenOutput( SOURCE, null,
                             new String[] {"-c", "testing/cmd/sg-test-config.rdf"},
                             new String[] {".*OntClass.*"}, // if config is not processed, we will not get --ontology output
                             new String[] {} );

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
        Model m = ModelFactory.createDefaultModel();
        m.read(  new StringReader( source ), "http://example.com/sg#", "N3" );
        return testSchemagenOutput( m, sg, args, posPatterns, negPatterns );
    }

    /**
     * Test the output from schemagen by saving the output to a string,
     * then ensuring that every positive regex matches at least one line, and
     * every negative regex matches at most no lines. Also checks that
     * compiling the file does not cause any errors.
     *
     * @param m Source model to read from
     * @param sg The schemagen object to test, or null for a default
     * @param args list of args to pass to SG
     * @param posPatterns array of regexps that must match at least once in the output
     * @param negPatterns arrays of regexps that must not match the output
     * @return The string defining the java class
     */
    protected String testSchemagenOutput( Model m, SchemaGenAux sg, String[] args,
                                          String[] posPatterns, String[] negPatterns )
        throws Exception
    {
        sg = (sg == null) ? new SchemaGenAux() : sg;
        sg.setSource( m );

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        sg.setOutput( new PrintStream( buf ) );

        // run schemagen
        sg.testGo( args );

        // now run the test pattern over the lines in the file
        String result = buf.toString();
//        if (log.isDebugEnabled()) {
//            log.debug(  result );
//        }
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
            for ( String negPattern : negPatterns )
            {
                Pattern pat = Pattern.compile( negPattern );
                assertFalse( "negative match pattern ||" + negPattern + "|| matched on line: " + line,
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
    protected void testCompile( String source, String defaultClassName )
        throws Exception
    {
        String className = defaultClassName;

        // ensure we use the right class name for the temp file
        // should do this with a regex, but java Pattern & Matcher is borked
        String key = "public class ";
        int i = source.indexOf( key );
        if (i > 0) {
            i += key.length();
            className = source.substring( i, source.indexOf( " ", i ) );
        }

        // first write the source file to a temp dir
        File tmpDir = FileUtils.getScratchDirectory( "schemagen" );
        File srcFile = new File( tmpDir, className + ".java" );
        try ( FileWriter out = new FileWriter( srcFile ) ) {
            out.write( source );
        }

        // now get ready to invoke javac using the new javax.tools package
        try {
            Class<?> tp = Class.forName(  "javax.tools.ToolProvider" );

            // static method to get the Java compiler tool
            Method gsjc = tp.getMethod( "getSystemJavaCompiler" );
            Object sjc = gsjc.invoke( null );

            // get the run method for the Java compiler tool
            Class<?> jc = Class.forName( "javax.tools.JavaCompiler" );
            Method jcRun = jc.getMethod( "run", new Class[] {InputStream.class, OutputStream.class, OutputStream.class, String[].class} );

            if (sjc != null && jcRun != null) {
                // build the args list for javac
                String[] args = new String[] {"-classpath", getClassPath( tmpDir ), "-d", tmpDir.getPath(), srcFile.getPath()};

                int success = (Integer) jcRun.invoke( sjc, null, null, null, args );
                assertEquals( "Errors reported from compilation of schemagen output", 0, success );
            }
            else {
                log.debug( "Could not resolve javax.tools.JavaCompiler.run() method. Is the CLASSPATH defined correctly?" );
            }
        }
        catch (ClassNotFoundException nf) {
            log.debug( "javax.tools not found (no tools.jar on classpath?). schemagen compilation test skipped." );
        }
        catch (Exception e) {
            log.debug( e.getMessage(), e );
            fail( e.getMessage() );
        }

        // clean up
        List<File> toClean = new ArrayList<>();
        toClean.add( tmpDir );

        while (!toClean.isEmpty()) {
            File f = toClean.remove( 0 );
            f.deleteOnExit();

            if (f.isDirectory()) {
                for (File g: f.listFiles()) {toClean.add( g );}
            }
        }
    }

    /**
     * Return the classpath we can use to compile the sg output files
     * @param tmpDir
     * @return
     */
    protected String getClassPath( File tmpDir ) {
        Properties pp = System.getProperties();
        // if we're running under maven, use Special Secret Knowledge to identify the class path
        // otherwise, default to the CP that Java thinks it's using
        return pp.getProperty( "surefire.test.class.path", pp.getProperty( "java.class.path" ) );
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
        @Override
        protected void selectInput() {
            m_source.add( m_auxSource );
            m_source.setNsPrefixes( m_auxSource );
        }
        @Override
        protected void selectOutput() {
            // call super to allow option processing
            super.selectOutput();
            // then override the result
            m_output = m_auxOutput;
        }

        public void testGo( String[] args ) {
            go( args );
        }

        @Override
        protected void go( String[] args ) {
            go( new SchemagenOptionsFixture( args ) );
        }

        @Override
        protected void abort( String msg, Exception e ) {
            throw new RuntimeException( msg, e );
        }
    }

    static class SchemagenOptionsFixture
        extends SchemagenOptionsImpl
    {

        public SchemagenOptionsFixture( String[] args ) {
            super( args );
        }

        @Override
        public Resource getInputOption() {
            return ResourceFactory.createResource( "http://example.org/sg" );
        }
    }
}
