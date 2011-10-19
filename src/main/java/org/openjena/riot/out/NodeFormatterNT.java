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

package org.openjena.riot.out;

import java.io.IOException ;
import java.io.Writer ;

import org.openjena.atlas.io.IO ;

public class NodeFormatterNT extends NodeFormatterBase
{
    // Formatting for NTriples 
    // Turtles extends this class to intercept forms it can do better.

    private final EscapeStr escapeProc ; 
    
    public NodeFormatterNT() { this(true) ; }
    
    protected NodeFormatterNT(boolean asciiOnly) { escapeProc = new EscapeStr(asciiOnly) ;}
    
    @Override
    public void formatURI(Writer w, String uriStr)
    {
        try {
            w.write('<') ;
            w.write(uriStr) ;
            w.write('>') ;
        } catch (IOException ex) { IO.exception(ex) ; } 
    }

    @Override
    public void formatVar(Writer w, String name)
    {
        try {
            w.write('?') ;
            w.write(name) ;
        } catch (IOException ex) { IO.exception(ex) ; }
    }

    @Override
    public void formatBNode(Writer w, String label)
    {
        try {
            w.write("_:") ;
            String lab = NodeFmtLib.encodeBNodeLabel(label) ;
            w.write(lab) ;
        } catch (IOException ex) { IO.exception(ex) ; }
    }

    @Override
    public void formatLitString(Writer w, String lex)
    {
        try {
            writeEscaped(w, lex) ;
        } catch (IOException ex) { IO.exception(ex) ; }
    }

    private void writeEscaped(Writer w, String lex) throws IOException
    {
        w.write('"') ;
        escapeProc.writeStr(w, lex) ;
        w.write('"') ;
    }

    @Override
    public void formatLitLang(Writer w, String lex, String langTag)
    {
        try {
            writeEscaped(w, lex) ;
            w.write('@') ;
            w.write(langTag) ;
        } catch (IOException ex) { IO.exception(ex) ; }
    }

    @Override
    public void formatLitDT(Writer w, String lex, String datatypeURI)
    {
        try {
            writeEscaped(w, lex) ;
            w.write("^^") ;
            formatURI(w, datatypeURI) ;
        } catch (IOException ex) { IO.exception(ex) ; }
    }
}
