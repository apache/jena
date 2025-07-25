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

package org.apache.jena.riot.lang;

import static org.apache.jena.riot.system.ErrorHandlerFactory.errorHandlerNoLogging;
import static org.apache.jena.riot.system.ErrorHandlerFactory.getDefaultErrorHandler;
import static org.apache.jena.riot.system.ErrorHandlerFactory.setDefaultErrorHandler;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.CharSpace;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.irix.IRIs;
import org.apache.jena.riot.ErrorHandlerTestLib.ErrorHandlerEx;
import org.apache.jena.riot.ErrorHandlerTestLib.ExError;
import org.apache.jena.riot.ErrorHandlerTestLib.ExFatal;
import org.apache.jena.riot.ErrorHandlerTestLib.ExWarning;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.system.*;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerText;
import org.apache.jena.riot.tokens.TokenizerTextBuilder;

/** Test of syntax by a tuples parser (does not include node validity checking) */
abstract public class AbstractTestLangNTuples
{
    // Test streaming interface.

    private static ErrorHandler errorhandler = null;

    @BeforeAll
    public static void beforeClass() {
        errorhandler = getDefaultErrorHandler();
        setDefaultErrorHandler(errorHandlerNoLogging);
    }

    @AfterAll
    public static void afterClass() {
        setDefaultErrorHandler(errorhandler);
    }

    @Test
    public void tuple_0() {
        long count = parseCount("");
        assertEquals(0, count);
    }

    @Test
    public void tuple_1() {
        long count = parseCount("<http://example/x> <http://example/y> <http://example/z>.");
        assertEquals(1, count);
    }

    @Test
    public void tuple_2() {
        long count = parseCount("<http://example/x> <http://example/y> \"z\".");
        assertEquals(1, count);
    }

    @Test
    public void tuple_3() {
        long count = parseCount("<http://example/x> <http://example/y> <http://example/z>. <http://example/x> <http://example/y> <http://example/z>.");
        assertEquals(2, count);
    }

    @Test
    public void tuple_4() {
        long count = parseCount("<http://example/x> <http://example/y> \"123\"^^<int>.");
        assertEquals(1, count);
    }

    @Test
    public void tuple_5() {
        long count = parseCount("<http://example/x> <http://example/y> \"123\"@lang.");
        assertEquals(1, count);
    }

    // Test parse errors interface.
    @Test
    public void tuple_bad_01() {
        parseException(ExFatal.class, "<http://example/x> <http://example/p> <http://example/z>");          // No DOT
    }

    @Test
    public void tuple_bad_02() {
        parseException(ExFatal.class, "<http://example/x> _:a <http://example/z> .");        // Bad predicate
    }

    @Test
    public void tuple_bad_03() {
        parseException(ExFatal.class, "<http://example/x> \"p\" <http://example/z> .");      // Bad predicate
    }

    @Test
    public void tuple_bad_04() {
        parseException(ExFatal.class, "\"x\" <http://example/p> <http://example/z> .");      // Bad subject -- fatal -- syntax error.
    }

    @Test
    public void tuple_bad_05() {
        parseException(ExFatal.class, "<http://example/x> <http://example/p> ?var .");        // No variables
    }

    @Test
    public void tuple_bad_6() {
        parseException(ExFatal.class, "<http://example/x> <http://example/p> 123 .");        // No abbreviations.
    }

    @Test
    public void tuple_bad_7() {
        parseException(ExFatal.class, "<http://example/x> <http://example/p> x:y .");        // No prefixed names
    }

    @Test
    public void tuple_bad_10() {
        parseException(ExFatal.class, "<http://example/x> <http://example/p> <http://example/bad uri> .");
    }

    // Bad terms (value range) - but legal syntax
    @Test
    public void tuple_bad_11() {
        parseCount("<http://example/x> <http://example/p> \"9000\"^^<http://www.w3.org/2001/XMLSchema#byte> .");
    }

    // Bad - relative URI.
    @Test
    public void tuple_bad_21() {
        parseException(ExError.class, "<http://example/x> <p> <http://example/z> .");
    }

    // Bad terms
    @Test
    public void tuple_bad_22() {
        parseException(ExFatal.class, "<http://example/x> <http://example/p> \"abc\"^^<http://example/bad uri> .");
    }

    @Test
    public void tuple_bad_23() {
        parseException(ExWarning.class, "<http://example/x> <http://example/p> \"9000\"^^<http://www.w3.org/2001/XMLSchema#byte> .");
    }

    // ASCII vs UTF-8
    @Test
    public void tuple_charset_1() {
        // E9 is e-acute
        parseCheck("<http://example/x\\u00E9> <http://example/p> <http://example/s> .");
    }

    @Test
    public void tuple_charset_2() {
        parseCheck("<http://example/é> <http://example/p> \"é\" .");
    }

    static protected Tokenizer tokenizer(CharSpace charSpace, String string) {
        byte b[] = StrUtils.asUTF8bytes(string);
        ByteArrayInputStream in = new ByteArrayInputStream(b);

        TokenizerTextBuilder builder = TokenizerText.create()
                .source(in)
                .errorHandler(ErrorHandlerFactory.errorHandlerExceptions());
        if ( charSpace == CharSpace.ASCII )
            builder.asciiOnly(true);
        Tokenizer tokenizer = builder.build();
        return tokenizer;
    }

    static protected Tokenizer tokenizer(String string) {
        // UTF-8
        byte b[] = StrUtils.asUTF8bytes(string);
        ByteArrayInputStream in = new ByteArrayInputStream(b);
        Tokenizer tokenizer = TokenizerText.create()
                .source(in)
                .errorHandler(ErrorHandlerFactory.errorHandlerExceptionOnError())
                .build();
        return tokenizer;
    }

    protected <T extends Throwable> T parseException(Class<T> exClass, String string) {
        return assertThrows(exClass, ()->parseCheck(string));
    }

    protected abstract LangRIOT createLangRIOT(Tokenizer tokenizer, StreamRDF sink, ParserProfile profile);

    final protected void parseCheck(String... strings) {
        String string = String.join("\n", strings);
        Tokenizer tokenizer = tokenizer(string);
        StreamRDFCounting sink = StreamRDFLib.count();
        LangRIOT x = createLangRIOT(tokenizer, sink, parserProfile(new ErrorHandlerEx()));
        x.parse();
    }

    final protected long parseCount(CharSpace charSpace, String... strings) {
        String string = String.join("\n", strings);
        Tokenizer tokenizer = tokenizer(charSpace, string);
        StreamRDFCounting sink = StreamRDFLib.count();
        LangRIOT x = createLangRIOT(tokenizer, sink, parserProfile(new ErrorHandlerEx()));
        x.parse();
        return sink.count();
    }

    /** Create a {@link ParserProfile}, no resolving, no prefix map. */
    static ParserProfile parserProfile(ErrorHandler errorHandler) {
        return new ParserProfileStd(RiotLib.factoryRDF(),
                                    errorHandler,
                                    IRIs.absoluteResolver(),
                                    PrefixMapFactory.emptyPrefixMap(),
                                    RIOT.getContext().copy(),
                                    true, false);
    }

    protected abstract Lang getLang();

    protected long parseCount(String string) {
        return ParserTests.parseCount(getLang(), string);
    }
}
