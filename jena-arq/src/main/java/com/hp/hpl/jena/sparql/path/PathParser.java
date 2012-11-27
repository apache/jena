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

package com.hp.hpl.jena.sparql.path;

import java.io.Reader ;
import java.io.StringReader ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryException ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.lang.arq.ARQParser ;

import org.apache.jena.atlas.logging.Log ;

public class PathParser
{
    public static Path parse(String str, PrefixMapping pmap)
    { return parse(str, new Prologue(pmap)) ; }
    
    public static Path parse(String str, Prologue prologue)
    {
        Query query = new Query(prologue) ;
        Reader in = new StringReader(str) ;
        ARQParser parser = new ARQParser(in) ;

        try {
            query.setStrict(true) ;
            parser.setQuery(query) ;
            return parser.PathUnit() ;
        } catch (com.hp.hpl.jena.sparql.lang.arq.ParseException ex)
        {
            throw new QueryParseException(ex.getMessage(),
                                          ex.currentToken.beginLine,
                                          ex.currentToken.beginColumn
                                          ) ; }
        catch (com.hp.hpl.jena.sparql.lang.arq.TokenMgrError tErr)
        {
            // Last valid token : not the same as token error message - but this should not happen
            int col = parser.token.endColumn ;
            int line = parser.token.endLine ;
            throw new QueryParseException(tErr.getMessage(), line, col) ;
        }
        catch (QueryException ex) { throw ex ; }
        catch (JenaException ex)  { throw new QueryException(ex.getMessage(), ex) ; }
        catch (Error err)
        {
            // The token stream can throw errors.
            throw new QueryParseException(err.getMessage(), err, -1, -1) ;
        }
        catch (Throwable th)
        {
            Log.warn(PathParser.class, "Unexpected throwable: ",th) ;
            throw new QueryException(th.getMessage(), th) ;
        }
    }
}
