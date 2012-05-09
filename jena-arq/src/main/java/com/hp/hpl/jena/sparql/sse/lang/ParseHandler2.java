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

/** Splitter for parser handlers.
 *  Calls both, first one first. */

public class ParseHandler2 implements ParseHandler
{
    private ParseHandler handler1 ;
    private ParseHandler handler2 ;
    
    public ParseHandler2(ParseHandler handler1, ParseHandler handler2)
    {
        this.handler1 = handler1 ;
        this.handler2 = handler2 ;
    }

    @Override
    public Item getItem()
    {
        Item item = handler1.getItem() ;
        if ( item == null )
            item = handler2.getItem() ;
        return item ;
    }

    @Override
    public void parseStart()
    {
        handler1.parseStart() ;
        handler2.parseStart() ; 
        
    }

    @Override
    public void parseFinish()
    {
        handler1.parseFinish() ;
        handler2.parseFinish() ; 
    }

    @Override
    public void listStart(int line, int column)
    {
        handler1.listStart(line, column) ;
        handler2.listStart(line, column) ;
        
    }

    @Override
    public void listFinish(int line, int column)
    {
        handler1.listFinish(line, column) ;
        handler2.listFinish(line, column) ;
    }

    @Override
    public void emitBNode(int line, int column, String label)
    { 
        handler1.emitBNode(line, column, label) ;
        handler2.emitBNode(line, column, label) ;
    }

    @Override
    public void emitIRI(int line, int column, String iriStr)
    {
        handler1.emitIRI(line, column, iriStr) ;
        handler2.emitIRI(line, column, iriStr) ;
    }

    @Override
    public void emitLiteral(int line, int column, String lex, String lang, String datatype_iri, String datatype_pn)
    {
        handler1.emitLiteral(line, column, lex, lang, datatype_iri, datatype_pn) ;
        handler2.emitLiteral(line, column, lex, lang, datatype_iri, datatype_pn) ;
    }

    @Override
    public void emitPName(int line, int column, String pname)
    {
        handler1.emitPName(line, column, pname) ;
        handler2.emitPName(line, column, pname) ;
    }

    @Override
    public void emitSymbol(int line, int column, String symbol)
    {
        handler1.emitSymbol(line, column, symbol) ;
        handler2.emitSymbol(line, column, symbol) ;
    }

    @Override
    public void emitVar(int line, int column, String varName)
    {
        handler1.emitVar(line, column, varName) ;
        handler2.emitVar(line, column, varName) ;
    }
    
}
