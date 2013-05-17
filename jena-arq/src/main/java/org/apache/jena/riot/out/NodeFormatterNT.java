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

package org.apache.jena.riot.out;


import org.apache.jena.atlas.io.AWriter ;

public class NodeFormatterNT extends NodeFormatterBase
{
    // Formatting for NTriples 
    // Turtles extends this class to intercept forms it can do better.

    private final EscapeStr escapeProc ; 

    public NodeFormatterNT() { this(CharSpace.UTF8) ; }

    public NodeFormatterNT(CharSpace charSpace) { escapeProc = new EscapeStr(charSpace) ;}

    @Override
    public void formatURI(AWriter w, String uriStr)
    {
        w.print('<') ;
        escapeProc.writeURI(w, uriStr) ;
        w.print('>') ;
    }

    @Override
    public void formatVar(AWriter w, String name)
    {
        w.print('?') ;
        escapeProc.writeStr(w, name) ;
    }

    @Override
    public void formatBNode(AWriter w, String label)
    {
        w.print("_:") ;
        String lab = NodeFmtLib.encodeBNodeLabel(label) ;
        w.print(lab) ;
    }

    @Override
    public void formatLitString(AWriter w, String lex)
    {
        writeEscaped(w, lex) ;
    }

    private void writeEscaped(AWriter w, String lex)
    {
        w.print('"') ;
        escapeProc.writeStr(w, lex) ;
        w.print('"') ;
    }

    @Override
    public void formatLitLang(AWriter w, String lex, String langTag)
    {
        writeEscaped(w, lex) ;
        w.print('@') ;
        w.print(langTag) ;
    }

    @Override
    public void formatLitDT(AWriter w, String lex, String datatypeURI)
    {
        writeEscaped(w, lex) ;
        w.print("^^") ;
        formatURI(w, datatypeURI) ;
    }
}
