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

package com.hp.hpl.jena.sparql.sse.lang;

import com.hp.hpl.jena.sparql.sse.Item ;

/** Warpper parser handler with pass-through for all operations.
 *  Calls both, first one first. */

public class ParseHandlerWrapper implements ParseHandler
{
    private ParseHandler handler ;
    
    public ParseHandlerWrapper(ParseHandler handler)
    {
        this.handler = handler ;
    }

    @Override
    public Item getItem()
    {
        return handler.getItem() ;
    }

    @Override
    public void parseStart()
    {
        handler.parseStart() ;
    }

    @Override
    public void parseFinish()
    {
        handler.parseFinish() ;
    }

    @Override
    public void listStart(int line, int column)
    {
        handler.listStart(line, column) ;
    }

    @Override
    public void listFinish(int line, int column)
    {
        handler.listFinish(line, column) ;
    }

    @Override
    public void emitBNode(int line, int column, String label)
    { 
        handler.emitBNode(line, column, label) ;
    }

    @Override
    public void emitIRI(int line, int column, String iriStr)
    {
        handler.emitIRI(line, column, iriStr) ;
    }

    @Override
    public void emitLiteral(int line, int column, String lex, String lang, String datatype_iri, String datatype_pn)
    {
        handler.emitLiteral(line, column, lex, lang, datatype_iri, datatype_pn) ;
    }

    @Override
    public void emitPName(int line, int column, String pname)
    {
        handler.emitPName(line, column, pname) ;
    }

    @Override
    public void emitSymbol(int line, int column, String symbol)
    {
        handler.emitSymbol(line, column, symbol) ;
    }

    @Override
    public void emitVar(int line, int column, String varName)
    {
        handler.emitVar(line, column, varName) ;
    }
    
}
