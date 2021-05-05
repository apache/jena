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

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.RiotException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/** Test IRIx in parser usage. */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestIRIxRIOT {

    // The cases:
    // _nt        ::  N-triples, default configuration.
    // _nt_check  ::  N-triples, with checking.
    // _ttl       :: Turtle, default configuration (which is checking).

    @Test public void irix_http_1_nt()          { testDft("<http://example/>", Lang.NT, 0, 0); }
    @Test public void irix_http_1_nt_check()    { testNT("<http://example/>", TRUE, UNSET, 0, 0); }
    @Test public void irix_http_1_ttl()         { testDft("<http://example/>", Lang.TTL, 0, 0); }

    @Test public void irix_http_2_nt()          { testDft("<HTTP://example/>", Lang.NT, 0, 0); }
    @Test public void irix_http_2_nt_check()    { testLang("<HTTP://example/>", Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_http_2_ttl()         { testDft("<HTTP://example/>", Lang.TTL, 0, 1); }

    @Test public void irix_http_3_nt()          { testDft("<http://EXAMPLE/>", Lang.NT, 0, 0); }
    @Test public void irix_http_3_nt_check()    { testLang("<http://EXAMPLE/>", Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_http_3_ttl()         { testDft("<http://EXAMPLE/>", Lang.TTL, 0, 0); }

    @Test public void irix_http_4_nt()          { testDft("<http://user:pw@host/>", Lang.NT, 0, 0); }
    @Test public void irix_http_4_nt_check()    { testLang("<http://user:pw@host/>", Lang.NT, UNSET, TRUE, 0, 2); }
    @Test public void irix_http_4_ttl()         { testDft("<http://user:pw@host/>", Lang.TTL, 0, 2); }

    @Test public void irix_uuid_1_nt()          { testDft("<urn:uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79>", Lang.NT, 0, 0); }
    @Test public void irix_uuid_1_nt_check()    { testLang("<urn:uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79>", Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_uuid_1_ttl()         { testDft("<urn:uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79>", Lang.TTL, 0, 0); }

    @Test public void irix_uuid_2_nt()          { testDft("<urn:uuid:bad>", Lang.NT, 0, 1); }
    @Test public void irix_uuid_2_nt_check()    { testLang("<urn:uuid:bad>", Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_uuid_2_ttl()         { testDft("<urn:uuid:bad>", Lang.TTL, 0, 1); }

    @Test public void irix_uuid_3_nt()          { testDft("<urn:uuid:bad>", Lang.NT, 0, 1); }
    @Test public void irix_uuid_3_nt_check()    { testLang("<urn:uuid:bad>", Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_uuid_3_ttl()         { testDft("<urn:uuid:bad>", Lang.TTL, 0, 1); }

    @Test public void irix_uuid_4_nt()          { testDft("<uuid:bad>", Lang.NT, 0, 1); }
    @Test public void irix_uuid_4_nt_check()    { testLang("<uuid:bad>", Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_uuid_4_ttl()         { testDft("<uuid:bad>", Lang.TTL, 0, 1); }

    @Test public void irix_urn_1_nt()           { testDft("<urn:ab:c>", Lang.NT, 0, 0); }
    @Test public void irix_urn_1_nt_check()     { testLang("<urn:ab:c>", Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_urn_1_ttl()          { testDft("<urn:ab:c>", Lang.TTL, 0, 0); }

    // URNs are required to have 2+ character NID : RFC 8141
    @Test public void irix_urn_2_nt()           { testDft("<urn:x:c>", Lang.NT, 0, 0); }
    @Test public void irix_urn_2_nt_check()     { testLang("<urn:x:c>", Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_urn_2_ttl()          { testDft("<urn:x:c>", Lang.TTL, 0, 1); }

    @Test public void irix_urn_3_nt()           { testDft("<urn:00:c>", Lang.NT, 0, 0); }
    @Test public void irix_urn_3_nt_check()     { testLang("<urn:00:c>", Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_urn_3_ttl()          { testDft("<urn:00:c>", Lang.TTL, 0, 0); }

    // URIs
    @Test public void irix_err_1_nt()           { testDft("<http://host/bad path/>", Lang.NT, 1, 1); }
    @Test public void irix_err_1_nt_check()     { testLang("<http://host/bad path/>", Lang.NT, UNSET, TRUE, 1, 1); }
    @Test public void irix_err_1_ttl()          { testDft("<http://host/bad path/>", Lang.TTL, 1, 1); }

    // NT: Relative URI
    @Test public void irix_relative_nt()           { testNT("<relative>", UNSET, UNSET, 0, 0); }
    @Test public void irix_relative_nt_check()     { testNT("<relative>", UNSET, TRUE, 0, 1); }
    @Test public void irix_relative_nt_strict()    { testNT("<relative>", TRUE, UNSET, 1, 0); }
    @Test public void irix_relative_nt_strict_check()    { testNT("<relative>", TRUE, TRUE, 1, 0); }
    @Test public void irix_relative_nt_strict_nocheck()   { testNT("<relative>", TRUE, FALSE, 1, 0); }

    // -------- Special cases for Turtle.
    // Turtle - base defaults to system base in normal use.

    @Test
    public void irix_relative_3_ttl() {
        assumeTrue(IRIs.getBaseStr() != null);
        testTTL("<relative>", UNSET, UNSET, 0, 0);
    }

    // Turtle with directly set resolver, non-standard setup. no base, resolve, no relative IRIs
    @Test public void irix_ttl_resolver_0() {
        // Resolver:: default is allowRelative(true)
        IRIxResolver resolver = IRIxResolver.create().noBase().build();
        testTTL("<relative>", resolver, 0, 1);
    }

    @Test public void irix_ttl_resolver_1() {
        // Resolver:: no base, no relative IRIs -> error.
        IRIxResolver resolver = IRIxResolver.create().noBase().allowRelative(false).build();
        testTTL("<relative>", resolver, 1, 0);
    }

    // Turtle with directly set resolver, non-standard setup. No base, no resolve, no relative IRIs.
    @Test public void irix_ttl_resolver_2() {
        // Resolver:: no base, no relative IRIs, no resolving -> error.
        IRIxResolver resolver = IRIxResolver.create().noBase().resolve(false).allowRelative(false).build();
        testTTL("<relative>", resolver, 1, 0);
    }

    @Test public void irix_ttl_resolver_3() {
        // Resolver:: no base, allow relative IRIs -> warning.
        IRIxResolver resolver = IRIxResolver.create().noBase().resolve(true).allowRelative(true).build();
        testTTL("<relative>", resolver, 0, 1);
    }

    @Test public void irix_ttl_resolver_4() {
        // Resolver:: no base, allow relative IRIs, no resolving -> warning.
        IRIxResolver resolver = IRIxResolver.create().noBase().resolve(false).allowRelative(true).build();
        testTTL("<relative>", resolver, 0, 1);
    }

    @Test public void irix_ttl_resolver_5() {
        // Resolver:: no base, allow relative IRIs, no resolving -> warning.
        IRIxResolver resolver = IRIxResolver.create().noBase().resolve(false).allowRelative(true).build();
        testTTL("<relative>", resolver, 0, 1);
    }

    // --------

    private static final Optional<Boolean> TRUE  = Optional.of(true);
    private static final Optional<Boolean> FALSE = Optional.of(false);
    private static final Optional<Boolean> UNSET = Optional.empty();

    // Default behaviour of Lang.
    private static void testDft(String iri, Lang lang, int numErrors, int numWarnings) {
        testLang(iri, lang, /*base*/null, UNSET, UNSET, numErrors, numWarnings);
    }

    // Behaviour of Lang, toegther with settable strict and checking.
    private static void testLang(String iri, Lang lang, Optional<Boolean> strict, Optional<Boolean> checking, int numErrors, int numWarnings) {
        testLang(iri, lang, /*base*/null, strict, checking, numErrors, numWarnings);
    }

    // N-triples
    private static void testNT(String iri, Optional<Boolean> strict, Optional<Boolean> checking, int numErrors, int numWarnings) {
        testLang(iri, Lang.NT, /*base*/null, strict, checking, numErrors, numWarnings);
    }

    // Turtle, with base.
    private static void testTTL(String iri, Optional<Boolean> strict, Optional<Boolean> checking, int numErrors, int numWarnings) {
        testLang(iri, Lang.TTL, "http://base/", strict, checking, numErrors, numWarnings);
    }

    // Turtle, with resolver
    private static void testTTL(String iri, IRIxResolver resolver, int numErrors, int numWarnings) {
        InputStream in = generateSource(iri);
        RDFParserBuilder builder = RDFParser.source(in).forceLang(Lang.TTL).resolver(resolver);
        runTest(builder, iri, numErrors, numWarnings);
    }

    private static void testLang(String iri, Lang lang, String base, Optional<Boolean> strict, Optional<Boolean> checking, int numErrors, int numWarnings) {
        InputStream in = generateSource(iri);
        RDFParserBuilder builder = RDFParser.source(in).forceLang(lang);
        builder.base(base);
        if ( strict.isPresent() )
            builder.strict(strict.get());
        if ( checking.isPresent() )
            builder.checking(checking.get());
        runTest(builder, iri, numErrors, numWarnings);
    }

    private static void runTest(RDFParserBuilder builder, String iri, int numErrors, int numWarnings) {
        StreamRDF dest = new CatchParserOutput();
        ErrorHandlerCollector eh = new ErrorHandlerCollector();
        builder.errorHandler(eh);

        // Do it!
        builder.build().parse(dest);

        int numErrorsActual = eh.errors.size();
        int numWarningsActual = eh.warnings.size();

        String msg = "Errors=("+numErrors+",got="+numErrorsActual+") Warnings=("+numWarnings+",got="+numWarningsActual+")";

        if ( numErrors != numErrorsActual || numWarnings != numWarningsActual ) {
            System.err.println("== "+iri);
            System.err.println("-- "+msg);
            if ( numErrorsActual == 0 )
                System.err.println("Errors: None");
            else
                eh.errors.forEach(m->System.err.println("Error: "+m));
            if ( numWarningsActual == 0 && numWarnings >= 0 )
                System.err.println("Warnings: None");
            else
                eh.warnings.forEach(m->System.err.println("Warnings: "+m));
        }

        assertEquals("Errors ("+msg+")", numErrors, numErrorsActual);
        // Only tested if errors passes.
        // -1 => ignore
        if ( numWarnings >= 0 )
            assertEquals("Warnings ("+msg+")", numWarnings, numWarningsActual);
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
