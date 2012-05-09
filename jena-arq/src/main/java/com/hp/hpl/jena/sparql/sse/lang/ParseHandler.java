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

public interface ParseHandler
{
    /** The result of a parse */
    public Item getItem() ;
    
    public void parseStart() ;
    public void parseFinish() ;
    
    public void emitIRI(int line, int column, String iriStr) ;
    public void emitBNode(int line, int column, String label) ;
    public void emitLiteral(int line, int column, String lex, String lang, String datatype_iri, String datatype_pn) ;
    public void emitVar(int line, int column, String varName) ;
    
    public void emitPName(int line, int column, String pname) ;
    public void emitSymbol(int line, int column, String symbol) ;
    
    public void listStart(int line, int column) ;
    public void listFinish(int line, int column) ;
}
