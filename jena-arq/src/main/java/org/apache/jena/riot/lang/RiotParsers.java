/**
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


import java.io.InputStream;
import java.io.Reader;

import org.apache.jena.atlas.io.PeekReader;
import org.apache.jena.atlas.json.io.parser.TokenizerJSON;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.riot.*;
import org.apache.jena.riot.protobuf.ProtobufRDF;
import org.apache.jena.riot.protobuf.RiotProtobufException;
import org.apache.jena.riot.system.*;
import org.apache.jena.riot.thrift.RiotThriftException;
import org.apache.jena.riot.thrift.ThriftRDF;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerText;
import org.apache.jena.sparql.util.Context;

/** Use {@link RDFParser} via:
 * <ul>
 * <li> {@link RDFParser#create()}
 * <li> {@link RDFParser#source(InputStream) RDFParser.source(...)} --various forms.
 * <li> {@link RDFParser#fromString}</li>
 * </ul>
 * <b>This class is internal to RIOT.</b>
 */
public class RiotParsers {

    private RiotParsers() {}

    public static ReaderRIOTFactory factoryTTL =
            (lang, parserProfile) -> new ReaderRIOTLangTTL(parserProfile);

    public static ReaderRIOTFactory factoryTRIG =
            (lang, parserProfile) -> new ReaderRIOTLangTriG(parserProfile);

    public static ReaderRIOTFactory factoryNT =
            (lang, parserProfile) -> new ReaderRIOTLangNTriples(parserProfile);

    public static ReaderRIOTFactory factoryNQ =
            (lang, parserProfile) -> new ReaderRIOTLangNQuads(parserProfile);

    public static ReaderRIOTFactory factoryRDFThrift =
            (Lang language, ParserProfile profile) -> new ReaderRDFThrift(profile);

    public static ReaderRIOTFactory factoryRDFProtobuf =
            (Lang language, ParserProfile profile) -> new ReaderRDFProtobuf(profile);

    public static ReaderRIOTFactory factoryJSONLD =
            (Lang language, ParserProfile profile) -> new LangJSONLD11(language, profile, profile.getErrorHandler());

    public static ReaderRIOTFactory factoryRDFJSON =
            (Lang language, ParserProfile profile) -> new ReaderRIOT_RDFJSON(profile);


    // RIOT TokenizerText based parsers - Turtle, TriG, N-Triple, N-Quads

    private static abstract class AbstractReaderRIOTLang implements ReaderRIOT {
        protected final Lang lang;
        protected final ParserProfile parserProfile;

        AbstractReaderRIOTLang(Lang lang, ParserProfile parserProfile) {
            this.lang = lang;
            this.parserProfile = parserProfile;
        }

        @Override
        public void read(InputStream input, String baseURI, ContentType ct, StreamRDF output, Context context) {
            // Parser profile gets the base.
            Tokenizer tokenizer = TokenizerText.create().source(input).errorHandler(parserProfile.getErrorHandler()).build();
            read(tokenizer,  output, context);
        }

        /**
         * The parser profile has a base URI when created; the read operation has a
         * baseURI. What if they are different?
         * <p>
         * For a non-null base. this does not happen from RDFParser. It creates a
         * parser profile with base URI, creates a ReaderRIOT then calls that
         * ReaderRIOT with the base URI.
         * <p>
         * For a null base, the parer profile (TTL, TriG) has the system base and NT and NQ have null base.
         * <p>
         * In case a ReaderRIOT is created directly by the app at one point, and used
         * later, the app may have caused a mis-alignment; the call to "read" does
         * not agree with the ParserProfile.
         * <p>
         * In this case, if the parser profile base is null - treated as "unset" - use the value in the read call.
         */
        private void checkParserProfile(ParserProfile parserProfile, String baseURI) {
            if ( Lang.NTRIPLES.equals(lang) || Lang.NQUADS.equals(lang) ) {
                // These two languages ignore the base. Relative URIs are either passed through (normal mode)
                // or illegal (strict mode) or the resolver has been specifically set (app choice).
            } else {
                // Turtle, TriG
                String parserProfileBase = parserProfile.getBaseURI();
                if ( parserProfileBase == null && baseURI != null ) {
                    parserProfile.setBaseIRI(baseURI);
                }
            }
        }

        @Override
        public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
            // Parser profile gets the base.
            checkParserProfile(parserProfile, baseURI);
            Tokenizer tokenizer = TokenizerText.create().source(reader).errorHandler(parserProfile.getErrorHandler()).build();
            read(tokenizer, output, context);
        }

        protected void read(Tokenizer tokenizer, StreamRDF output, Context context) {
            LangRIOT parser = create(tokenizer, output, context);
            parser.parse();
        }

        abstract protected LangRIOT create(Tokenizer tokenizer, StreamRDF output, Context context);
    }

    private static class ReaderRIOTLangTTL extends AbstractReaderRIOTLang {
        ReaderRIOTLangTTL(ParserProfile parserProfile) {
            super(Lang.TURTLE, parserProfile);
        }

        @Override
        protected LangRIOT create(Tokenizer tokenizer, StreamRDF output, Context context) {
            return new LangTurtle(tokenizer, super.parserProfile, output);
        }
    }

    private static class ReaderRIOTLangTriG extends AbstractReaderRIOTLang {
        ReaderRIOTLangTriG(ParserProfile parserProfile) {
            super(Lang.TRIG, parserProfile);
        }

        @Override
        protected LangRIOT create(Tokenizer tokenizer, StreamRDF output, Context context) {
            return new LangTriG(tokenizer, super.parserProfile, output);
        }
    }

    private static class ReaderRIOTLangNTriples extends AbstractReaderRIOTLang {
        ReaderRIOTLangNTriples(ParserProfile parserProfile) {
            super(Lang.NTRIPLES, parserProfile);
        }

        @Override
        protected LangRIOT create(Tokenizer tokenizer, StreamRDF output, Context context) {
            return new LangNTriples(tokenizer, super.parserProfile, output);
        }
    }

    private static class ReaderRIOTLangNQuads extends AbstractReaderRIOTLang {
        ReaderRIOTLangNQuads(ParserProfile parserProfile) {
            super(Lang.NQUADS, parserProfile);
        }

        @Override
        protected LangRIOT create(Tokenizer tokenizer, StreamRDF output, Context context) {
            return new LangNQuads(tokenizer, super.parserProfile, output);
        }
    }


    private static class ReaderRIOT_RDFJSON implements ReaderRIOT {

        private final ParserProfile parserProfile;

        ReaderRIOT_RDFJSON(ParserProfile parserProfile) {
            this.parserProfile = parserProfile;
        }

        @Override
        public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
            Tokenizer tokenizer = new TokenizerJSON(PeekReader.makeUTF8(in));
            LangRDFJSON parser = new LangRDFJSON(tokenizer, parserProfile, output);
            parser.parse();
        }

        @Override
        public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
            Tokenizer tokenizer = new TokenizerJSON(PeekReader.make(reader));
            LangRDFJSON parser = new LangRDFJSON(tokenizer, parserProfile, output);
            parser.parse();
        }
    }

    private static class ReaderRIOTFactoryJSONLD11 implements ReaderRIOTFactory {
        @Override
        public ReaderRIOT create(Lang language, ParserProfile profile) {
            if ( !Lang.JSONLD.equals(language) && !Lang.JSONLD11.equals(language) )
                throw new InternalErrorException("Attempt to parse " + language + " as JSON-LD 1.1");
            // Titanium json-ld for JSON-LD 1.1
            return new LangJSONLD11(language, profile, profile.getErrorHandler());
        }
    }

    private static class ReaderRDFProtobuf implements ReaderRIOT {
        private final ParserProfile profile;

        public ReaderRDFProtobuf(ParserProfile profile) {
            this.profile = profile;
        }

        @Override
        public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
            try {
                ProtobufRDF.inputStreamToStreamRDF(in, output);
            } catch (RiotProtobufException ex) {
                if ( profile != null && profile.getErrorHandler() != null )
                    profile.getErrorHandler().error(ex.getMessage(), -1, -1);
                else
                    ErrorHandlerFactory.errorHandlerStd.error(ex.getMessage(), -1, -1);
                throw ex;
            }
        }

        @Override
        public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
            throw new RiotException("RDF Protobuf : Reading binary data from a java.io.reader is not supported. Please use an InputStream");
        }
    }

    private static class ReaderRDFThrift implements ReaderRIOT {
        private final ParserProfile profile;
        public ReaderRDFThrift(ParserProfile profile) { this.profile = profile; }

        @Override
        public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
            try {
                ThriftRDF.inputStreamToStream(in, output);
            } catch (RiotThriftException ex) {
                if ( profile != null && profile.getErrorHandler() != null )
                    profile.getErrorHandler().error(ex.getMessage(), -1, -1);
                else
                    ErrorHandlerFactory.errorHandlerStd.error(ex.getMessage(), -1 , -1);
                throw ex;
            }
        }

        @Override
        public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
            throw new RiotException("RDF Thrift : Reading binary data from a java.io.reader is not supported. Please use an InputStream");
        }
    }
}

