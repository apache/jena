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

package org.apache.jena.riot.system.irix;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.iri3986.provider.InitIRI3986;
import org.apache.jena.irix.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.CatchParserOutput;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sys.JenaSystem;

/** Test IRIx in parser usage. */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class AbstractTestIRIxRIOT_system {

    static { JenaSystem.init(); }

    protected static final Optional<Boolean> TRUE  = Optional.of(true);
    protected static final Optional<Boolean> FALSE = Optional.of(false);
    protected static final Optional<Boolean> UNSET = Optional.empty();

    private static final IRIProvider systemProvider = SystemIRIx.getProvider();

    protected abstract IRIProvider getProviderForTest();
    protected IRIxResolver.Builder resolverBuilder() {
        IRIProvider provider = getProviderForTest();
        String baseIRI = IRIs.getBaseStr();
        IRIx base = provider.create(baseIRI);
        return IRIxResolver.create(baseIRI);
    }

    protected AbstractTestIRIxRIOT_system(String name) {
        // Ensure IRI3986 initialized even if not the system provider.
        InitIRI3986.init();
        //System.out.println("AbstractTestIRIxRIOT_system [ "+name+" ] ");
    }

    // The cases:
    // _nt        ::  N-triples, default configuration.
    // _nt_check  ::  N-triples, with checking.
    // _ttl       ::  Turtle, default configuration (which is checking).

    protected static final String httpUri01 = "<http://example/>";
    @Test public void irix_http_1_nt()          { testDft (httpUri01, Lang.NT, 0, 0); }
    @Test public void irix_http_1_nt_check()    { testLang(httpUri01, Lang.NT, TRUE, UNSET, 0, 0); }
    @Test public void irix_http_1_ttl()         { testDft (httpUri01, Lang.TTL, 0, 0); }

    protected static final String httpUri02 = "<HTTP://example/>";
    @Test public void irix_http_2_nt()          { testDft (httpUri02, Lang.NT, 0, 0); }
    @Test public void irix_http_2_nt_check()    { testLang(httpUri02, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_http_2_ttl()         { testDft (httpUri02, Lang.TTL, 0, 1); }

    protected static final String httpUri03 = "<http://EXAMPLE/>";
    @Test public void irix_http_3_nt()          { testDft (httpUri03, Lang.NT, 0, 0); }
    @Test public void irix_http_3_nt_check()    { testLang(httpUri03, Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_http_3_ttl()         { testDft (httpUri03, Lang.TTL, 0, 0); }

    protected static final String httpUri04 = "<http://user:pw@host/>";
    @Test public void irix_http_4_nt()          { testDft (httpUri04, Lang.NT, 0, 0); }
    @Test public void irix_http_4_nt_check()    { testLang(httpUri04, Lang.NT, UNSET, TRUE, 0, 2); }
    @Test public void irix_http_4_ttl()         { testDft (httpUri04, Lang.TTL, 0, 2); }

    protected static final String httpUri05 = "<http://user@host/>";
    @Test public void irix_http_5_nt()          { testDft (httpUri05, Lang.NT, 0, 0); }
    @Test public void irix_http_5_nt_check()    { testLang(httpUri05, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_http_5_ttl()         { testDft (httpUri05, Lang.TTL, 0, 1); }

    // -- urn:uuid
    // The warning on bad UUIDs is from IRIProviderJenaIRI, not jena-iri, and so it isn't check/no check sensitive.

    protected static final String urnuuid01 = "<urn:uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79>";
    @Test public void irix_urn_uuid_1_nt()        { testDft (urnuuid01, Lang.NT, 0, 0); }
    @Test public void irix_urn_uuid_1_nt_check()  { testLang(urnuuid01, Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_urn_uuid_1_ttl()       { testDft (urnuuid01, Lang.TTL, 0, 0); }

    protected static final String urnuuid02 = "<urn:uuid:>";
    @Test public void irix_urn_uuid_2_nt()        { testDft (urnuuid02, Lang.NT, 0, 0); }
    @Test public void irix_urn_uuid_2_nt_check()  { testLang(urnuuid02, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_urn_uuid_2_ttl()       { testDft (urnuuid02, Lang.TTL, 0, 1); }

    protected static final String urnuuid03 = "<urn:uuid:bad>";
    @Test public void irix_urn_uuid_3_nt()        { testDft (urnuuid03, Lang.NT, 0, 0); }
    @Test public void irix_urn_uuid_3_nt_check()  { testLang(urnuuid03, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_urn_uuid_3_ttl()       { testDft (urnuuid03, Lang.TTL, 0, 1); }

    // bad URN component.
    protected static final String urnuuid04 = "<urn:uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79?query>";
    @Test public void irix_urn_uuid_4_nt()        { testDft (urnuuid04, Lang.NT, 0, 0); }
    @Test public void irix_urn_uuid_4_nt_check()  { testLang(urnuuid04, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_urn_uuid_4_ttl()       { testDft (urnuuid04, Lang.TTL, 0, 1); }

    // Allow URN components in urn:uuid.
    protected static final String urnuuid05 = "<urn:uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79#fragment>";
    @Test public void irix_urn_uuid_5_nt()        { testDft (urnuuid05, Lang.NT, 0, 0); }
    @Test public void irix_urn_uuid_5_nt_check()  { testLang(urnuuid05, Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_urn_uuid_5_ttl()       { testDft (urnuuid05, Lang.TTL, 0, 0); }

    protected static final String urnuuid06 = "<urn:uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79?query#fragment>";
    @Test public void irix_urn_uuid_6_nt()        { testDft (urnuuid06, Lang.NT, 0, 0); }
    @Test public void irix_urn_uuid_6_nt_check()  { testLang(urnuuid06, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_urn_uuid_6_ttl()       { testDft (urnuuid06, Lang.TTL, 0, 1); }

    // Allow URN components in urn:uuid.
    protected static final String urnuuid07 = "<urn:uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79?+R?=Q#fragment>";
    @Test public void irix_urn_uuid_7_nt()        { testDft (urnuuid07, Lang.NT, 0, 0); }
    @Test public void irix_urn_uuid_7_nt_check()  { testLang(urnuuid07, Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_urn_uuid_7_ttl()       { testDft (urnuuid07, Lang.TTL, 0, 0); }

    // Allow URN components in urn:uuid.
    protected static final String urnuuid08 = "<urn:uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79?+R?=Q>";
    @Test public void irix_urn_uuid_8_nt()        { testDft (urnuuid08, Lang.NT, 0, 0); }
    @Test public void irix_urn_uuid_8_nt_check()  { testLang(urnuuid08, Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_urn_uuid_8_ttl()       { testDft (urnuuid08, Lang.TTL, 0, 0); }

    // -- uuid:

    protected static final String uuid01 = "<uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79>";
    @Test public void irix_uuid_1_nt()          { testDft (urnuuid01, Lang.NT, 0, 0); }
    @Test public void irix_uuid_1_nt_check()    { testLang(urnuuid01, Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_uuid_1_ttl()         { testDft (urnuuid01, Lang.TTL, 0, 0); }

    protected static final String uuid02 = "<uuid:>";
    @Test public void irix_uuid_2_nt()          { testDft (urnuuid02, Lang.NT, 0, 0); }
    @Test public void irix_uuid_2_nt_check()    { testLang(urnuuid02, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_uuid_2_ttl()         { testDft (urnuuid02, Lang.TTL, 0, 1); }

    protected static final String uuid03 = "<uuid:bad>";
    @Test public void irix_uuid_3_nt()          { testDft (uuid03, Lang.NT, 0, 0); }
    @Test public void irix_uuid_3_nt_check()    { testLang(uuid03, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_uuid_3_ttl()         { testDft (uuid03, Lang.TTL, 0, 1); }

    protected static final String uuid04 = "<uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79?query>";
    @Test public void irix_uuid_4_nt()          { testDft (urnuuid04, Lang.NT, 0, 0); }
    @Test public void irix_uuid_4_nt_check()    { testLang(urnuuid04, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_uuid_4_ttl()         { testDft (urnuuid04, Lang.TTL, 0, 1); }

    protected static final String uuid05 = "<urn:uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79#fragment>";
    @Test public void irix_uuid_5_nt()          { testDft (urnuuid05, Lang.NT, 0, 0); }
    @Test public void irix_uuid_5_nt_check()    { testLang(urnuuid05, Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_uuid_5_ttl()         { testDft (urnuuid05, Lang.TTL, 0, 0); }

    protected static final String uuid06 = "<uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79?query#fragment>";
    @Test public void irix_uuid_6_nt()          { testDft (uuid06, Lang.NT, 0, 0); }
    @Test public void irix_uuid_6_nt_check()    { testLang(uuid06, Lang.NT, UNSET, TRUE, 0, 2); }
    @Test public void irix_uuid_6_ttl()         { testDft (uuid06, Lang.TTL, 0, 2); }

    // Do not allow URN components in uuid:.
    protected static final String uuid07 = "<uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79?+R?=Q#fragment>";
    @Test public void irix_uuid_7_nt()          { testDft (uuid07, Lang.NT, 0, 0); }
    @Test public void irix_uuid_7_nt_check()    { testLang(uuid07, Lang.NT, UNSET, TRUE, 0, 2); }
    @Test public void irix_uuid_7_ttl()         { testDft (uuid07, Lang.TTL, 0, 2); }

    // Do not allow URN components in uuid:.
    protected static final String uuid08 = "<uuid:6cd401dc-a8d2-11eb-9192-1f162b53dc79?+R?=Q>";
    @Test public void irix_uuid_8_nt()          { testDft (uuid08, Lang.NT, 0, 0); }
    @Test public void irix_uuid_8_nt_check()    { testLang(uuid08, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_uuid_8_ttl()         { testDft (uuid08, Lang.TTL, 0, 1); }

    // -- urn:

    protected static final String urn01 = "<urn:ab:c>";
    @Test public void irix_urn_1_nt()           { testDft (urn01, Lang.NT, 0, 0); }
    @Test public void irix_urn_1_nt_check()     { testLang(urn01, Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_urn_1_ttl()          { testDft (urn01, Lang.TTL, 0, 0); }

    // URNs are required to have 2+ character NID : RFC 8141
    protected static final String urn02 = "<urn:x:c>";
    @Test public void irix_urn_2_nt()           { testDft (urn02, Lang.NT, 0, 0); }
    @Test public void irix_urn_2_nt_check()     { testLang(urn02, Lang.NT, UNSET, TRUE, 0, 1); }
    @Test public void irix_urn_2_ttl()          { testDft (urn02, Lang.TTL, 0, 1); }

    protected static final String urn03 = "<urn:00:c>";
    @Test public void irix_urn_3_nt()           { testDft (urn03, Lang.NT, 0, 0); }
    @Test public void irix_urn_3_nt_check()     { testLang(urn03, Lang.NT, UNSET, TRUE, 0, 0); }
    @Test public void irix_urn_3_ttl()          { testDft (urn03, Lang.TTL, 0, 0); }

    // URIs
    protected static final String uri11 = "<http://host/bad path/>";
    @Test public void irix_err_1_nt()           { testDft (uri11, Lang.NT, 1, 1); }
    @Test public void irix_err_1_nt_check()     { testLang(uri11, Lang.NT, UNSET, TRUE, 1, 1); }
    @Test public void irix_err_1_ttl()          { testDft (uri11, Lang.TTL, 1, 1); }

    // NT: Relative URI
    protected static final String uriRel = "<relative>";
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
        IRIxResolver resolver = resolverBuilder().noBase().build();
        testTTL(uriRel, resolver, 0, 1);
    }

    @Test public void irix_ttl_resolver_1() {
        // Resolver:: no base, no relative IRIs -> error.
        IRIxResolver resolver = resolverBuilder().noBase().allowRelative(false).build();
        testTTL(uriRel, resolver, 1, 0);
    }

    // Turtle with directly set resolver, non-standard setup. No base, no resolve, no relative IRIs.
    @Test public void irix_ttl_resolver_2() {
        // Resolver:: no base, no relative IRIs, no resolving -> error.
        IRIxResolver resolver = resolverBuilder().noBase().resolve(false).allowRelative(false).build();
        testTTL(uriRel, resolver, 1, 0);
    }

    @Test public void irix_ttl_resolver_3() {
        // Resolver:: no base, allow relative IRIs -> warning.
        IRIxResolver resolver = resolverBuilder().noBase().resolve(true).allowRelative(true).build();
        testTTL(uriRel, resolver, 0, 1);
    }

    @Test public void irix_ttl_resolver_4() {
        // Resolver:: no base, allow relative IRIs, no resolving -> warning.
        IRIxResolver resolver = resolverBuilder().noBase().resolve(false).allowRelative(true).build();
        testTTL(uriRel, resolver, 0, 1);
    }

    @Test public void irix_ttl_resolver_5() {
        // Resolver:: no base, allow relative IRIs, no resolving -> warning.
        IRIxResolver resolver = resolverBuilder().noBase().resolve(false).allowRelative(true).build();
        testTTL(uriRel, resolver, 0, 1);
    }

    // Default behaviour of Lang.
    protected void testDft(String iri, Lang lang, int numErrors, int numWarnings) {
        testLang(iri, lang, /*base*/null, UNSET, UNSET, numErrors, numWarnings);
    }

    // Behaviour of Lang, together with settable strict and checking.
    protected void testLang(String iri, Lang lang, Optional<Boolean> strict, Optional<Boolean> checking, int numErrors, int numWarnings) {
        testLang(iri, lang, /*base*/null, strict, checking, numErrors, numWarnings);
    }

    // N-triples
    protected void testNT(String iri, Optional<Boolean> strict, Optional<Boolean> checking, int numErrors, int numWarnings) {
        testLang(iri, Lang.NT, /*base*/null, strict, checking, numErrors, numWarnings);
    }

    // Turtle, with base.
    protected void testTTL(String iri, Optional<Boolean> strict, Optional<Boolean> checking, int numErrors, int numWarnings) {
        testLang(iri, Lang.TTL, "http://base/", strict, checking, numErrors, numWarnings);
    }

    // Turtle, with resolver
    protected void testTTL(String iri, IRIxResolver resolver, int numErrors, int numWarnings) {
        InputStream in = generateSource(iri);
        RDFParserBuilder builder = RDFParser.source(in).forceLang(Lang.TTL).resolver(resolver);
        runTest(builder, iri, numErrors, numWarnings);
    }

    private void testLang(String iri, Lang lang, String base, Optional<Boolean> strict, Optional<Boolean> checking, int numErrors, int numWarnings) {
        InputStream in = generateSource(iri);
        RDFParserBuilder builder = RDFParser.source(in).forceLang(lang);
        builder.base(base);
        strict.ifPresent(builder::strict);
        checking.ifPresent(builder::checking);
        runTest(builder, iri, numErrors, numWarnings);
    }

    private void runTest(RDFParserBuilder builder, String iri, int numErrors, int numWarnings) {
        IRIProvider systemProvider = SystemIRIx.getProvider();
        IRIProvider testProvider = getProviderForTest();
        SystemIRIx.setProvider(testProvider);
        try {
            runTestInner(builder, iri, numErrors, numWarnings);
        } finally {
            SystemIRIx.setProvider(systemProvider);
        }
    }

    private void runTestInner(RDFParserBuilder builder, String iri, int numErrors, int numWarnings) {
        StreamRDF dest = new CatchParserOutput();
        ErrorHandlerCollector eh = new ErrorHandlerCollector();
        builder.errorHandler(eh);

        // Do it!
        builder.build().parse(dest);

        int numErrorsActual = eh.errors.size();
        int numWarningsActual = eh.warnings.size();

        boolean testPasses = ( numErrors == numErrorsActual && numWarnings == numWarningsActual );
        String msg = iri;

        if ( !testPasses ) {
            String ProviderName = getProviderForTest().getClass().getSimpleName();
            msg = ProviderName+" -- "+iri+" "+
                    " Errors=(expected="+numErrors+",got="+numErrorsActual+")"+
                    " Warnings=(expected="+numWarnings+",got="+numWarningsActual+")";
            PrintStream out = System.out;
            out.println("-- "+msg);
            eh.errors.forEach(m->System.err.println("Error: "+m));
            if ( numWarningsActual == 0 && numWarnings >= 0 )
                out.println("Warnings: None");
            else
                eh.warnings.forEach(m->out.println("Warnings: "+m));
            out.println();
        }
        assertTrue(testPasses, msg);
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
