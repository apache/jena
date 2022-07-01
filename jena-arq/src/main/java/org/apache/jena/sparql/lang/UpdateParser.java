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

import java.io.FileReader;
import java.io.InputStream ;
import java.io.Reader ;
import java.io.StringReader;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.QueryParseException ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.modify.UpdateSink ;
import org.apache.jena.util.FileUtils ;

/** 
 * This class provides the root of lower level access to all the update parsers.
 * Each subclass hides the details of the per-language exception handlers and other
 * javacc details.    
 */

public abstract class UpdateParser
{
    protected UpdateParser() {}
    
    /** Parse a string */ 
    public final void parse(UpdateSink sink, Prologue prologue, String updateString) throws QueryParseException {
        Reader r = new StringReader(updateString);
        executeParse(sink, prologue, r);
    }

    /** Parse an input stream */ 
    public final void parse(UpdateSink sink, Prologue prologue, InputStream input) throws QueryParseException {
        // BOM processing moved to the grammar.
        Reader r = FileUtils.asBufferedUTF8(input);
        executeParse(sink, prologue, r);
    }

    /** Use with care - Reader must be UTF-8 */
    public void parse(UpdateSink sink, Prologue prologue, Reader r) {
        if ( r instanceof FileReader )
            Log.warn(this, "FileReader passed to Update parser - use a FileInputStream");
        executeParse(sink, prologue, r);
    }

    // Subclass action.
    protected abstract void executeParse(UpdateSink sink, Prologue prologue, Reader r);
    
    public static boolean canParse(Syntax syntaxURI) {
        return UpdateParserRegistry.get().containsFactory(syntaxURI);
    }

    public static UpdateParser createParser(Syntax syntaxURI) {
        return UpdateParserRegistry.get().createParser(syntaxURI);
    }
}
