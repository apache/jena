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

package org.apache.jena.shex.runner;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.jena.arq.junit.manifest.Manifest;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.arq.junit.manifest.Prefix;
import org.apache.jena.arq.junit.runners.Label;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.system.stream.Locator;
import org.apache.jena.riot.system.stream.LocatorFile;
import org.apache.jena.riot.system.stream.StreamManager;
import org.apache.jena.shex.expressions.Sx2;
import org.apache.jena.vocabulary.RDF;

public class ShexTests {
    static boolean VERBOSE = false;

    // Validation test filtering.
    static Set<Resource> excludeTraits = new HashSet<>();
    static Set<String> excludes = new HashSet<>();
    static Set<String> includes = new LinkedHashSet<>();
    static boolean dumpTest = false;
    static StreamManager streamMgr = StreamManager.get().clone();


    static {
        setup();
        //Sx.TRACE : true if there are inclusions
        //includes.add("#1val1IRIREFExtra1_pass-iri2");
        //includes.add("#start2RefS1-IstartS2");

        VERBOSE = ! includes.isEmpty();

        // --- Exclusions - development

        // # External
        excludes.add("#shapeExtern_pass");
        excludes.add("#shapeExtern_fail");
        excludes.add("#shapeExternRef_pass");
        excludes.add("#shapeExternRef_fail");

        // ## semantic actions
        // SemanticAction trait supported
        // OrderedSemanticActions trait supported
        excludeTraits.add(ShexT.tExternalSemanticAction); // add external parser

        // ## annotations + semantic actions
        excludes.add("#open3Eachdotclosecard23Annot3Code2-p1p2p3X3");

        // ---- Exclusions
        // BNodes tests we don't support (testing labels, cross file references)
        excludes.addAll(bNodeLabeltests());

        // Syntax exclusions - may be needed here.
        //    //---- Exclusions
        //    // java does to support character classes \N (all numbers)
        //    excludes.add("1literalPattern_with_all_meta.shex");
        //
        //    // Unclear. Breaks when fix for unicode escapes is applied.
        //    // Is this because of the incompatible REGEX language choice?
        //    excludes.add("1literalPattern_with_REGEXP_escapes_escaped.shex");
        //
        //    // Regex has a null (unicode codepoint 0000) which is illegal.
        //    excludes.add("1literalPattern_with_ascii_boundaries.shex");
        //
        //    // Contains \ud800 (ill-formed surrogate pair)
        //    excludes.add("1refbnode_with_spanning_PN_CHARS_BASE1.shex");
        //
        //    // Contains \u0d00 (ill-formed surrogate pair)
        //    excludes.add("_all.shex");
        //---- Exclusions

        if ( ShexTests.VERBOSE ) {
            System.err.println("Validation");
            System.err.println("  inclusions    = "+includes.size());
            System.err.println("  exclusions    = "+(excludes.size()-bNodeLabeltests().size()));
            System.err.println("  bNode labels  = "+bNodeLabeltests().size());
            System.err.println();
            dumpTest = ! includes.isEmpty();
        }

        if ( ! includes.isEmpty() )
            Sx2.TRACE = true;
    }

    private static Collection<String> bNodeLabeltests() {
        // jena-shex operates on post-parsing files.
        //
        // Tests that assume that
        // + blank node labels are preserved after parsing.
        // + the same label in different files is the same blank node.
        // which is not true. In Jena, blank nodes are global.
        // and different between parsing two files. (this is required by RDF).

        Set<String> bNodeLabelTests = new HashSet<>();

        // ### facet (bnodes)
        bNodeLabelTests.add("#1focusLength-dot_fail-bnode-short");
        bNodeLabelTests.add("#1focusLength-dot_pass-bnode-equal");
        bNodeLabelTests.add("#1focusLength-dot_fail-bnode-long");

        bNodeLabelTests.add("#1focusMinLength-dot_pass-bnode-equal");
        bNodeLabelTests.add("#1focusMinLength-dot_pass-bnode-long");
        bNodeLabelTests.add("#1focusMaxLength-dot_pass-bnode-short");
        bNodeLabelTests.add("#1focusMaxLength-dot_pass-bnode-equal");
        bNodeLabelTests.add("#1focusPatternB-dot_pass-bnode-match");
        bNodeLabelTests.add("#1focusPatternB-dot_pass-bnode-long");
        bNodeLabelTests.add("#1focusBNODELength_dot_pass");

        bNodeLabelTests.add("#1bnodeLength_pass-bnode-equal");
        bNodeLabelTests.add("#1bnodeLength_fail-bnode-short");
        bNodeLabelTests.add("#1bnodeLength_pass-bnode-equal");
        bNodeLabelTests.add("#1bnodeLength_fail-bnode-long");
        bNodeLabelTests.add("#1bnodeLength_fail-lit-equal");
        bNodeLabelTests.add("#1bnodeLength_fail-iri-equal");

        bNodeLabelTests.add("#1nonliteralLength_fail-bnode-short");
        bNodeLabelTests.add("#1nonliteralLength_pass-bnode-equal");
        bNodeLabelTests.add("#1nonliteralLength_fail-bnode-long");

        bNodeLabelTests.add("#1bnodeMinlength_pass-bnode-equal");
        bNodeLabelTests.add("#1bnodeMinlength_pass-bnode-long");
        bNodeLabelTests.add("#1nonliteralMinlength_pass-bnode-equal");
        bNodeLabelTests.add("#1nonliteralMinlength_pass-bnode-long");
        bNodeLabelTests.add("#1bnodeMaxlength_pass-bnode-short");
        bNodeLabelTests.add("#1bnodeMaxlength_pass-bnode-equal");
        bNodeLabelTests.add("#1nonliteralMaxlength_pass-bnode-short");
        bNodeLabelTests.add("#1nonliteralMaxlength_pass-bnode-equal");

        bNodeLabelTests.add("#1bnodePattern_pass-bnode-match");
        bNodeLabelTests.add("#1bnodePattern_fail-bnode-short");
        bNodeLabelTests.add("#1bnodePattern_fail-bnode-long");

        bNodeLabelTests.add("#1bnodeMinlength_fail-bnode-short");
        bNodeLabelTests.add("#1bnodeMinlength_pass-bnode-equal");
        bNodeLabelTests.add("#1bnodeMinlength_pass-bnode-long");

        bNodeLabelTests.add("#1bnodePattern_pass-bnode-match");
        bNodeLabelTests.add("#1bnodePattern_fail-bnode-long");

        bNodeLabelTests.add("#1nonliteralPattern_pass-bnode-match");
        bNodeLabelTests.add("#1nonliteralPattern_pass-bnode-long");

        bNodeLabelTests.add("#1nonliteralMinlength_fail-bnode-short");
        bNodeLabelTests.add("#1nonliteralMinlength_pass-bnode-equal");
        bNodeLabelTests.add("#1nonliteralMinlength_pass-bnode-long");

        bNodeLabelTests.add("#1bnodeMaxlength_pass-bnode-short");
        bNodeLabelTests.add("#1bnodeMaxlength_pass-bnode-equal");
        bNodeLabelTests.add("#1bnodeMaxlength_fail-bnode-long");

        bNodeLabelTests.add("#1nonliteralMaxlength_pass-bnode-short");
        bNodeLabelTests.add("#1nonliteralMaxlength_pass-bnode-equal");
        bNodeLabelTests.add("#1nonliteralMaxlength_fail-bnode-long");

        bNodeLabelTests.add("#1valExprRefbnode-IV1_pass-lit-equal");

        bNodeLabelTests.add("#1focusBNODE_dot_fail-iriFocusLabel-equal");
        bNodeLabelTests.add("#1focusBNODE_dot_pass");

        bNodeLabelTests.add("#bnode1dot_fail-missing");
        bNodeLabelTests.add("#bnode1dot_pass-others_lexicallyEarlier");

        return bNodeLabelTests;
    }

    /** Create a Shex test - or return null for "unrecognized" */
    public static Runnable makeShexValidationTest(ManifestEntry entry) {
        if ( ! runTestExclusionsInclusions(entry) )
            return null;

        Resource testType = entry.getTestType();
        if ( testType == null ) {
            //RepresentationTest
            System.out.println("No test type: " + entry.getName());
            return null;
        }

        if ( testType.equals(ShexT.cRepresentationTest) ) {
            return ()->{};
        }

        Resource action = entry.getAction();
        if ( action == null ) {
            System.out.println("Action expected: " + entry.getName());
            return null;
        }

        if ( action.hasProperty(ShexT.semActs) ) {}
        if ( action.hasProperty(ShexT.shapeExterns) ) {}


        if ( testType == null ) { }

        //action.getProperty(ShexT.trait);

        if ( ! testType.equals(ShexT.cValidationTest) && ! testType.equals(ShexT.cValidationFailure) ) {
            System.err.println("Skip unknown test type for: "+entry.getName());
            return ()->{};
        }

        // -- Check
        // map or (shape+focus)
        if ( ! action.hasProperty(ShexT.schema) ) {
            System.err.println("Bad: no schema : "+entry.getName());
            return null;
        }

        if ( action.hasProperty(ShexT.map) ) {
            if ( action.hasProperty(ShexT.shape) || action.hasProperty(ShexT.focus) ) {
                System.err.println("Bad: map + (shape or focus) : "+entry.getName());
            }
            if ( !action.hasProperty(ShexT.data) || ! action.hasProperty(ShexT.schema)) {
                System.err.println("Bad: map + no (data+schema) : "+entry.getName());
            }
        } else {
            // Not map
            if ( ! action.hasProperty(ShexT.schema) || ! action.hasProperty(ShexT.data) || ! action.hasProperty(ShexT.focus) )
                System.err.println("Bad: no map, no (scheme/data/focus) : "+entry.getName());
        }
        // -- Check

        if ( testType.equals(ShexT.cValidationTest) || testType.equals(ShexT.cValidationFailure) ) {
            boolean faiureTest = testType.equals(ShexT.cValidationFailure);

            if ( action.hasProperty(ShexT.shape) ) {
                // Expected: ShexT.schema, ShexT.shape, ShexT.data, ShexT.focus
                try {
                    Resource schema = action.getProperty(ShexT.schema).getResource();
                    Resource shape = action.getProperty(ShexT.shape).getResource();
                    Resource data = action.getProperty(ShexT.data).getResource();
                    // URI or literal.
                    RDFNode focus = action.getProperty(ShexT.focus).getObject();
                    return ShexValidationTest.testShexValidationShapeFocus(entry);
                } catch (Exception ex) {
                    System.err.println(ex.getClass().getName());
                    System.err.println(ex.getMessage());
                    System.err.println(entry.getEntry().getLocalName());
                    return null;
                }
            }

            // Roll into the above?
            if ( action.hasProperty(ShexT.focus) ) {
                // Expected: ShexT.schema (with start), ShexT.data, ShexT.focus
                try {
                    Resource schema = action.getProperty(ShexT.schema).getResource();
                    // No shape.
                    Resource data = action.getProperty(ShexT.data).getResource();
                    // URI or literal.
                    RDFNode focus = action.getProperty(ShexT.focus).getObject();
                    return ShexValidationTest.testShexValidationStartFocus(entry);
                } catch (Exception ex) {
                }
            }

            if ( action.hasProperty(ShexT.map) ) {
                // Expected: ShexT.schema (with start), ShexT.map, ShexT.data
                try {
                    Resource schema = action.getProperty(ShexT.schema).getResource();
                    Resource map = action.getProperty(ShexT.map).getResource();
                    Resource data = action.getProperty(ShexT.data).getResource();
                    return ShexValidationTest.testShexValidationMap(entry);
                } catch (Exception ex) {
                    System.err.println(ex.getClass().getName());
                    System.err.println(ex.getMessage());
                    System.err.println(entry.getEntry().getLocalName());
                    return null;
                }
            }

            // Unknown.
            System.err.println("Unknown: "+entry.getName());
            return null;

        } else {
            Log.warn("ShexTests", "Skip unknown test type for: "+entry.getName());
            return null;
        }
    }

    public static List<Resource> extractTraits(ManifestEntry entry) {
        Resource traitsRsrc = Manifest.getResource(entry.getEntry(), ShexT.trait) ;
        if (traitsRsrc == null)
            return null;
        List<Statement> x = entry.getEntry().listProperties(ShexT.trait).toList();
        return x.stream().map(t -> t.getObject().asResource()).collect(Collectors.toList());
    }

    private static boolean runTestExclusionsInclusions(ManifestEntry entry) {
        String fragment = fragment(entry);
        if ( fragment != null ) {
            // Includes, if present.
            if ( includes.contains(fragment) )
                return true;
            if ( ! includes.isEmpty() )
                return false;

            if ( excludes.contains(fragment) ) {
                // [shex] Convert to "ignored"
                //System.err.println("Skipping:  "+fragment);
                return false;
            }

            List<Resource> traits = extractTraits(entry);
            if (traits != null) {
                List<Resource> excludedBecause = traits.stream().filter(excl -> excludeTraits.contains(excl)).collect(Collectors.toList());
                if (excludedBecause.size() > 0)
                    return false;
            }
        }
        return true;
    }

    public static List<Pair<Resource, String>> extractExtensionResults(ManifestEntry entry) {
        Resource extensionResults = Manifest.getResource(entry.getEntry(), ShexT.extensionResults) ;
        if (extensionResults == null)
            return null;
        List<Pair<Resource, String>> pairs = new ArrayList<>();
        StmtIterator listIter = entry.getEntry().listProperties(ShexT.extensionResults);
        while (listIter.hasNext()) {
            //List head
            Resource listItem = listIter.nextStatement().getResource();
            while (!listItem.equals(RDF.nil)) {
                Resource extensionResult = listItem.getRequiredProperty(RDF.first).getResource(); //TODO Eric, please review. Hopefully this does the trick
                Resource extension = extensionResult.getProperty(ShexT.extension).getResource();
                Literal prints = extensionResult.getProperty(ShexT.prints).getLiteral();
                String printStr = prints.getString() ;
                Pair<Resource, String> pair = new Pair<>(extension, printStr);
                pairs.add(pair);
                // Move to next list item
                listItem = listItem.getRequiredProperty(RDF.rest).getResource();
            }
        }
        listIter.close();
        return pairs;
    }

    // [shex] Migrate
    public static String fragment(ManifestEntry entry) {
        if ( entry.getEntry().isURIResource() )
            return fragment(entry.getEntry().getURI());
        return null;
    }

    // [shex] Migrate
    public static String fragment(String uri) {
        int j = uri.lastIndexOf('#') ;
        String fn = (j >= 0) ? uri.substring(j) : uri ;
        return fn ;
    }

    /*package*/ static String getLabel(Class<? > klass) {
        Label annotation = klass.getAnnotation(Label.class);
        return ( annotation == null ) ? null : annotation.value();
    }

    /*package*/ static String getPrefix(Class<? > klass) {
        Prefix annotation = klass.getAnnotation(Prefix.class);
        return ( annotation == null ) ? null : annotation.value();
    }

    private static void setup() {
        // Setup StreamManager.
        String places[] = { "src/test/files/spec/schemas/", "src/test/files/spec/validation/" };


        for ( String dir : places ) {
            Locator alt = new LocatorShexTest(dir);
            streamMgr.addLocator(alt);
        }
        StreamManager.setGlobal(streamMgr);
    }

    // Hunt files.
    static class LocatorShexTest extends LocatorFile {
        public LocatorShexTest(String dir) {
            super(dir);
        }

        @Override
        public TypedInputStream open(String filenameOrURL) {
            String fn = FileOps.basename(filenameOrURL);
            String url = fn.endsWith(".shex")
                    ? fn
                    : fn+".shex";
            return super.open(url);
        }

        @Override
        public String getName() {
            return "LocatorShexTest";
        }
    }
}
