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

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.QueryException ;
import org.apache.jena.query.QueryParseException ;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.lang.sparql_11.SPARQLParser11 ;
import org.apache.jena.sparql.modify.UpdateSink ;
import org.apache.jena.update.UpdateException ;

public class ParserSPARQL11Update extends UpdateParser
{
    public ParserSPARQL11Update() {}
    
    @Override
    protected void executeParse(UpdateSink sink, Prologue prologue, Reader r) {
        SPARQLParser11 parser = null ;
        try {
            parser = new SPARQLParser11(r) ;
            parser.setUpdate(prologue, sink) ;
            parser.UpdateUnit() ;
        }
        catch (org.apache.jena.sparql.lang.sparql_11.ParseException ex)
        { 
            throw new QueryParseException(ex.getMessage(),
                                          ex.currentToken.beginLine,
                                          ex.currentToken.beginColumn) ;
        }
        catch (org.apache.jena.sparql.lang.sparql_11.TokenMgrError tErr)
        {
            // Last valid token : not the same as token error message - but this should not happen
            int col = parser.token.endColumn ;
            int line = parser.token.endLine ;
            throw new QueryParseException(tErr.getMessage(), line, col) ; }

        catch (UpdateException ex) { throw ex ; }
        catch (JenaException ex)  { throw new QueryException(ex.getMessage(), ex) ; }
        catch (Error err)
        {
            // The token stream can throw errors.
            throw new QueryParseException(err.getMessage(), err, -1, -1) ;
        }
        catch (Throwable th)
        {
            Log.error(this, "Unexpected throwable: ",th) ;
            throw new QueryException(th.getMessage(), th) ;
        }
    }
}
