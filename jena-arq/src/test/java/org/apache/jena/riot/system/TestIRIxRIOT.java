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

package org.apache.jena.riot.system;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.irix.SystemIRIx;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.RiotException;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;

/** Test IRIx in parser usage. */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestIRIxRIOT {

    private static final String ProviderName = SystemIRIx.getProvider().getClass().getSimpleName();

    // The cases:
    // _nt        ::  N-triples, default configuration.
    // _nt_check  ::  N-triples, with checking.
    // _ttl       :: Turtle, default configuration (which is checking).

    private static String httpUri01 = "<http://example/>";
    @Test public void irix_http_1_nt()          { testDft (httpUri01, Lang.NT, 0, 0); }
    @Test public void irix_http_1_nt_check()    { testLang(httpUri01, Lang.NT, TRUE, UNSET, 0, 0); }
    @Test public void irix_http_1_ttl()         { testDft (httpUri01, Lang.TTL, 0, 0); }

    private static String httpUri02 = "<HTTP://example/>";
    @Test public void irix_http_2_nt()          { testDft (httpUri02, Lang.NT, 0, 0); }
    @Test public void irix_http_2_nt_check()    { testLang(httpUri02, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_http_2_ttl()         { testDft (httpUri02, Lang.TTL, 0, 1); }

    private static String httpUri03 = "<http://EXAMPLE/>";
    @Test public void irix_http_3_nt()          { testDft (httpUri03, Lang.NT, 0, 0); }
    @Test public void irix_http_3_nt_check()    { testLang(httpUri03, Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_http_3_ttl()         { testDft (httpUri03, Lang.TTL, 0, 0); }

    private static String httpUri04 = "<http://user:pw@host/>";
    @Test public void irix_http_4_nt()          { testDft (httpUri04, Lang.NT, 0, 0); }
    @Test public void irix_http_4_nt_check()    { testLang(httpUri04, Lang.NT, UNSET, TRUE, 0, 2); }
    @Test public void irix_http_4_ttl()         { testDft (httpUri04, Lang.TTL, 0, 2); }

    private static String httpUri05 = "<http://user@host/>";
    @Test public void irix_http_5_nt()          { testDft (httpUri05, Lang.NT, 0, 0); }
    @Test public void irix_http_5_nt_check()    { testLang(httpUri05, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_http_5_ttl()         { testDft (httpUri05, Lang.TTL, 0, 1); }

    private static String urnuuid01 = "<urn:uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79>";
    @Test public void irix_uuid_1_nt()          { testDft (urnuuid01, Lang.NT, 0, 0); }
    @Test public void irix_uuid_1_nt_check()    { testLang(urnuuid01, Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_uuid_1_ttl()         { testDft (urnuuid01, Lang.TTL, 0, 0); }

    // -- uuid: & urn:uuid -- jena-iri answers
    // The warning on bad UUIDs is from IRIProviderjenaIRI, not jena-iri, and so it isn't check/no check sensitive.
    private static String urnuuid02 = "<urn:uuid:bad>";
    @Test public void irix_uuid_2_nt()          { testDft (urnuuid02, Lang.NT, 0, 1); }
    @Test public void irix_uuid_2_nt_check()    { testLang(urnuuid02, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_uuid_2_ttl()         { testDft (urnuuid02, Lang.TTL, 0, 1); }

    private static String uuid03 = "<uuid:bad>";
    @Test public void irix_uuid_3_nt()          { testDft (uuid03, Lang.NT, 0, 1); }
    @Test public void irix_uuid_3_nt_check()    { testLang(uuid03, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_uuid_3_ttl()         { testDft (uuid03, Lang.TTL, 0, 1); }

    private static String urnuuid04 = "<urn:uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79?query>";
    @Test public void irix_uuid_4_nt()          { testDft (urnuuid04, Lang.NT, 0, 0); }
    @Test public void irix_uuid_4_nt_check()    { testLang(urnuuid04, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_uuid_4_ttl()         { testDft (urnuuid04, Lang.TTL, 0, 1); }

    private static String urnuuid05 = "<urn:uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79#fragment>";
    @Test public void irix_uuid_5_nt()          { testDft (urnuuid05, Lang.NT, 0, 0); }
    @Test public void irix_uuid_5_nt_check()    { testLang(urnuuid05, Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_uuid_5_ttl()         { testDft (urnuuid05, Lang.TTL, 0, 0); }

    private static String urnuuid06 = "<urn:uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79?query#fragment>";
    @Test public void irix_uuid_6_nt()          { testDft (urnuuid06, Lang.NT, 0, 0); }
    @Test public void irix_uuid_6_nt_check()    { testLang(urnuuid06, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_uuid_6_ttl()         { testDft (urnuuid06, Lang.TTL, 0, 1); }

    private static String uuid07 = "<uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79?query#fragment>";
    @Test public void irix_uuid_7_nt()          { testDft (uuid07, Lang.NT, 0, 1); }
    @Test public void irix_uuid_7_nt_check()    { testLang(uuid07, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_uuid_7_ttl()         { testDft (uuid07, Lang.TTL, 0, 1); }

    // -- urn:uuid

    private static String uri08 = "<urn:ab:c>";
    @Test public void irix_urn_1_nt()           { testDft (uri08, Lang.NT, 0, 0); }
    @Test public void irix_urn_1_nt_check()     { testLang(uri08, Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_urn_1_ttl()          { testDft (uri08, Lang.TTL, 0, 0); }

    // URNs are required to have 2+ character NID : RFC 8141
    private static String uri09 = "<urn:x:c>";
    @Test public void irix_urn_2_nt()           { testDft (uri09, Lang.NT, 0, 0); }
    @Test public void irix_urn_2_nt_check()     { testLang(uri09, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_urn_2_ttl()          { testDft (uri09, Lang.TTL, 0, 1); }

    private static String uri10 = "<urn:00:c>";
    @Test public void irix_urn_3_nt()           { testDft (uri10, Lang.NT, 0, 0); }
    @Test public void irix_urn_3_nt_check()     { testLang(uri10, Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_urn_3_ttl()          { testDft (uri10, Lang.TTL, 0, 0); }

    // URIs
    private static String uri11 = "<http://host/bad path/>";
    @Test public void irix_err_1_nt()           { testDft (uri11, Lang.NT, 1, 1); }
    @Test public void irix_err_1_nt_check()     { testLang(uri11, Lang.NT, UNSET, TRUE, 1, 1); }
    @Test public void irix_err_1_ttl()          { testDft (uri11, Lang.TTL, 1, 1); }

    // NT: Relative URI
    private static String uriRel = "<relative>";
    @Test public void irix_relative_nt()                { testNT(uriRel, UNSET, UNSET, 0, 0); }
    @Test public void irix_relative_nt_check()          { testNT(uriRel, UNSET, TRUE, 0, 1); }
    @Test public void irix_relative_nt_strict()         { testNT(uriRel, TRUE, UNSET, 1, 0); }
    @Test public void irix_relative_nt_strict_check()   { testNT(uriRel, TRUE, TRUE, 1, 0); }
    @Test public void irix_relative_nt_strict_nocheck() { testNT(uriRel, TRUE, FALSE, 1, 0); }

    // -------- Special cases for Turtle.
    // Turtle - base defaults to system base in normal use.

    @Test
    public void irix_relative_3_ttl() {
        assumeTrue(IRIs.getBaseStr() != null);
        testTTL(uriRel, UNSET, UNSET, 0, 0);
    }

    // Turtle with directly set resolver, non-standard setup. no base, resolve, no relative IRIs
    @Test public void irix_ttl_resolver_0() {
        // Resolver:: default is allowRelative(true)
        IRIxResolver resolver = IRIxResolver.create().noBase().build();
        testTTL(uriRel, resolver, 0, 1);
    }

    @Test public void irix_ttl_resolver_1() {
        // Resolver:: no base, no relative IRIs -> error.
        IRIxResolver resolver = IRIxResolver.create().noBase().allowRelative(false).build();
        testTTL(uriRel, resolver, 1, 0);
    }

    // Turtle with directly set resolver, non-standard setup. No base, no resolve, no relative IRIs.
    @Test public void irix_ttl_resolver_2() {
        // Resolver:: no base, no relative IRIs, no resolving -> error.
        IRIxResolver resolver = IRIxResolver.create().noBase().resolve(false).allowRelative(false).build();
        testTTL(uriRel, resolver, 1, 0);
    }

    @Test public void irix_ttl_resolver_3() {
        // Resolver:: no base, allow relative IRIs -> warning.
        IRIxResolver resolver = IRIxResolver.create().noBase().resolve(true).allowRelative(true).build();
        testTTL(uriRel, resolver, 0, 1);
    }

    @Test public void irix_ttl_resolver_4() {
        // Resolver:: no base, allow relative IRIs, no resolving -> warning.
        IRIxResolver resolver = IRIxResolver.create().noBase().resolve(false).allowRelative(true).build();
        testTTL(uriRel, resolver, 0, 1);
    }

    @Test public void irix_ttl_resolver_5() {
        // Resolver:: no base, allow relative IRIs, no resolving -> warning.
        IRIxResolver resolver = IRIxResolver.create().noBase().resolve(false).allowRelative(true).build();
        testTTL(uriRel, resolver, 0, 1);
    }

    // --------
    // Get the test name.
    private String testMethodName = null;
    @Rule
    public TestRule watcher = new TestWatcher() {
        @Override protected void starting(Description description) {
            testMethodName = description.getMethodName();
        }
        @Override protected void finished(Description description) {
            testMethodName = null;
        }
    };

    private static final Optional<Boolean> TRUE  = Optional.of(true);
    private static final Optional<Boolean> FALSE = Optional.of(false);
    private static final Optional<Boolean> UNSET = Optional.empty();

    // Default behaviour of Lang.
    private void testDft(String iri, Lang lang, int numErrors, int numWarnings) {
        testLang(iri, lang, /*base*/null, UNSET, UNSET, numErrors, numWarnings);
    }

    // Behaviour of Lang, together with settable strict and checking.
    private void testLang(String iri, Lang lang, Optional<Boolean> strict, Optional<Boolean> checking, int numErrors, int numWarnings) {
        testLang(iri, lang, /*base*/null, strict, checking, numErrors, numWarnings);
    }

    // N-triples
    private void testNT(String iri, Optional<Boolean> strict, Optional<Boolean> checking, int numErrors, int numWarnings) {
        testLang(iri, Lang.NT, /*base*/null, strict, checking, numErrors, numWarnings);
    }

    // Turtle, with base.
    private void testTTL(String iri, Optional<Boolean> strict, Optional<Boolean> checking, int numErrors, int numWarnings) {
        testLang(iri, Lang.TTL, "http://base/", strict, checking, numErrors, numWarnings);
    }

    // Turtle, with resolver
    private void testTTL(String iri, IRIxResolver resolver, int numErrors, int numWarnings) {
        InputStream in = generateSource(iri);
        RDFParserBuilder builder = RDFParser.source(in).forceLang(Lang.TTL).resolver(resolver);
        runTest(builder, iri, numErrors, numWarnings);
    }

    private void testLang(String iri, Lang lang, String base, Optional<Boolean> strict, Optional<Boolean> checking, int numErrors, int numWarnings) {
        InputStream in = generateSource(iri);
        RDFParserBuilder builder = RDFParser.source(in).forceLang(lang);
        builder.base(base);
        if ( strict.isPresent() )
            builder.strict(strict.get());
        if ( checking.isPresent() )
            builder.checking(checking.get());
        runTest(builder, iri, numErrors, numWarnings);
    }

    private void runTest(RDFParserBuilder builder, String iri, int numErrors, int numWarnings) {
        StreamRDF dest = new CatchParserOutput();
        ErrorHandlerCollector eh = new ErrorHandlerCollector();
        builder.errorHandler(eh);

        // Do it!
        builder.build().parse(dest);

        int numErrorsActual = eh.errors.size();
        int numWarningsActual = eh.warnings.size();

        String msg = ProviderName+" --"+
                     " Errors=(expected="+numErrors+",got="+numErrorsActual+")"+
                     " Warnings=(expected="+numWarnings+",got="+numWarningsActual+")";
        boolean testPasses = ( numErrors == numErrorsActual && numWarnings == numWarningsActual );

        if ( !testPasses ) {
            System.err.println("== "+testMethodName+" : "+iri);
            System.err.println("-- "+msg);
            if ( numErrorsActual == 0 )
                ; //System.err.println("Errors: None");
            else
                eh.errors.forEach(m->System.err.println("Error: "+m));
            if ( numWarningsActual == 0 && numWarnings >= 0 )
                System.err.println("Warnings: None");
            else
                eh.warnings.forEach(m->System.err.println("Warnings: "+m));
            System.err.println();
        }
        assertTrue(msg, testPasses);
    }

    private static InputStream generateSource(String iri) {
        // N-Triples line with test subject
        String TEXT = iri+" <x:p> <x:o> .";
        InputStream inText = new ByteArrayInputStream(Bytes.string2bytes(TEXT));
        return inText;
    }

    static class ErrorHandlerCollector implements ErrorHandler {
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> fatals = new ArrayList<>();

        @Override
        public void warning(String message, long line, long col) {
            warnings.add(message);
        }

        @Override
        public void error(String message, long line, long col) {
            errors.add(message);
            //throw new RiotException(message);
        }

        @Override
        public void fatal(String message, long line, long col) {
            fatals.add(message);
            throw new RiotException(message);
        }
    }
}
