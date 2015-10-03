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

import java.io.InputStream ;
import java.io.Reader ;

import org.apache.jena.query.QueryParseException ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.sparql.modify.UpdateSink ;
import org.apache.jena.util.FileUtils ;

/** This class provides the root of lower level access to all the parsers.
 *  Each subclass hides the details of the per-language exception handlers and other
 *  javacc details to provide a methods that deal with setting up Query objects
 *  and using QueryException exceptions for problems.    
 */

public abstract class UpdateParser
{
    public final void parse(UpdateSink sink, String updateString) throws QueryParseException
    {
        parse$(sink, updateString) ;
    }

    protected abstract void parse$(UpdateSink sink, String updateString) throws QueryParseException ;

    public final void parse(UpdateSink sink, InputStream input) throws QueryParseException
    {
        // BOM processing moved to the grammar.
        Reader r = FileUtils.asBufferedUTF8(input) ;
        parse$(sink, r) ;
    }
    
    protected abstract void parse$(UpdateSink sink, Reader r) throws QueryParseException ;

    public static boolean canParse(Syntax syntaxURI)
    {
        return UpdateParserRegistry.get().containsFactory(syntaxURI) ;
    }
    
    public static UpdateParser createParser(Syntax syntaxURI)
    {
        return UpdateParserRegistry.get().createParser(syntaxURI) ;
    }
}
