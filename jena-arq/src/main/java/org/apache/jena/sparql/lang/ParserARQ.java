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

package org.apache.jena.sparql.lang;

import java.io.Reader ;
import java.io.StringReader ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryException ;
import org.apache.jena.query.QueryParseException ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.sparql.lang.arq.ARQParser ;
import org.apache.jena.sparql.syntax.Element ;
import org.apache.jena.sparql.syntax.Template ;


public class ParserARQ extends SPARQLParser
{
    private interface Action { void exec(ARQParser parser) throws Exception ; }

    @Override
    protected Query parse$(final Query query, String queryString)
    {
        query.setSyntax(Syntax.syntaxARQ) ;

        Action action = new Action() {
            @Override
            public void exec(ARQParser parser) throws Exception
            {
                parser.QueryUnit() ;
            }
        } ;

        perform(query, queryString, action) ;
        return query ;
    }

    public static Element parseElement(String string)
    {
        final Query query = new Query () ;
        Action action = new Action() {
            @Override
            public void exec(ARQParser parser) throws Exception
            {
                Element el = parser.GroupGraphPattern() ;
                query.setQueryPattern(el) ;
            }
        } ;
        perform(query, string, action) ;
        return query.getQueryPattern() ;
    }

    public static Template parseTemplate(String string)
    {
        final Query query = new Query () ;
        Action action = new Action() {
            @Override
            public void exec(ARQParser parser) throws Exception
            {
                Template t = parser.ConstructTemplate() ;
                query.setConstructTemplate(t) ;
            }
        } ;
        perform(query, string, action) ;
        return query.getConstructTemplate() ;
    }

    // All throwable handling.
    private static void perform(Query query, String string, Action action)
    {
        Reader in = new StringReader(string) ;
        ARQParser parser = new ARQParser(in) ;

        try {
            query.setStrict(true) ;
            parser.setQuery(query) ;
            action.exec(parser) ;
        }
        catch (org.apache.jena.sparql.lang.arq.ParseException ex)
        {
            throw new QueryParseException(ex.getMessage(),
                                          ex.currentToken.beginLine,
                                          ex.currentToken.beginColumn
                                          ) ; }
        catch (org.apache.jena.sparql.lang.arq.TokenMgrError tErr)
        {
            // Last valid token : not the same as token error message - but this should not happen
            int col = parser.token.endColumn ;
            int line = parser.token.endLine ;
            throw new QueryParseException(tErr.getMessage(), line, col) ; }

        catch (QueryException ex) { throw ex ; }
        catch (JenaException ex)  { throw new QueryException(ex.getMessage(), ex) ; }
        catch (Error err)
        {
            // The token stream can throw errors.
            throw new QueryParseException(err.getMessage(), err, -1, -1) ;
        }
        catch (Throwable th)
        {
            Log.warn(ParserSPARQL11.class, "Unexpected throwable: ",th) ;
            throw new QueryException(th.getMessage(), th) ;
        }
    }
}
