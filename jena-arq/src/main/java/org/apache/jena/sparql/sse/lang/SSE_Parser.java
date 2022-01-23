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

package org.apache.jena.sparql.sse.lang;

import java.io.Reader;

import org.apache.jena.sparql.sse.SSE_ParseException;
import org.apache.jena.sparql.sse.lang.parser.ParseException;
import org.apache.jena.sparql.sse.lang.parser.SSE_ParserCore;
import org.apache.jena.sparql.sse.lang.parser.TokenMgrError;

/** Public interface to the SSE parser */
public class SSE_Parser
{
    @FunctionalInterface
    private interface ParserEntry { void entry(SSE_ParserCore parser) throws ParseException; }

    public static void parse(Reader reader, ParseHandler handler) {
        parse$(reader, handler, SSE_ParserCore::parse);
    }

    private static void parse$(Reader reader, ParseHandler handler, ParserEntry parserStep) {
        SSE_ParserCore p = new SSE_ParserCore(reader);
        p.setHandler(handler);
        try {
            parserStep.entry(p);
            // EOF checking done within the parser.
        }
        catch (ParseException ex)
        { throw new SSE_ParseException(ex.getMessage(), ex.currentToken.beginLine, ex.currentToken.beginColumn); }
        catch (TokenMgrError tErr)
        {
            // Last valid token : not the same as token error message - but this should not happen
            int col = p.token.endColumn;
            int line = p.token.endLine;
            throw new SSE_ParseException(tErr.getMessage(), line, col);
        }
    }

}
