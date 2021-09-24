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

package org.apache.jena.riot.lang.extra;

import java.io.InputStream;
import java.io.Reader;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.riot.ReaderRIOT;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.lang.LangTurtle;
import org.apache.jena.riot.lang.extra.javacc.ParseException;
import org.apache.jena.riot.lang.extra.javacc.TokenMgrError;
import org.apache.jena.riot.lang.extra.javacc.TurtleJavacc;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.util.Context;

/**
 * Turtle parser, written using JavaCC.
 * This is not used normally.
 * It is slower than the RIOT {@link LangTurtle}.
 * It may not be up-to-date but at least in the codebase means it should be java-compatible.
 * It exists so that there is a JavaCC grammar that can be used as a basis for other languages.
 */
public class TurtleJavaccReaderRIOT implements ReaderRIOT {
    private final ParserProfile profile;

    public TurtleJavaccReaderRIOT(ParserProfile profile) { this.profile = profile; }

    @Override
    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
        // Do bytes -> chars in big units.
        Reader r = IO.asBufferedUTF8(in);
        TurtleJavacc parser = new TurtleJavacc(r);
        read(parser, baseURI, ct, output, context);
    }

    @Override
    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
        // Causes some double buffering but the JJavaCC buffer is only 4k.
        //reader = IO.ensureBuffered(reader);
        TurtleJavacc parser = new TurtleJavacc(reader);
        read(parser, baseURI, ct, output, context);
    }

    private void read(TurtleJavacc parser, String baseURI, ContentType ct, StreamRDF output, Context context) {
        parser.setDest(output);
        parser.setProfile(profile);
        // profile should be setup correctly for the base
        try {
            output.start();
            parser.parse();
            output.finish();
        }
        catch (ParseException ex) {
            profile.getErrorHandler().error(ex.getMessage(), ex.currentToken.beginLine, ex.currentToken.beginColumn);
            throw new RiotParseException(ex.getMessage(), ex.currentToken.beginLine, ex.currentToken.beginColumn);
        }
        catch (TokenMgrError ex) {
            profile.getErrorHandler().error(ex.getMessage(), -1, -1);
            throw new RiotParseException(ex.getMessage(), -1 , -1);
        }
    }
}
