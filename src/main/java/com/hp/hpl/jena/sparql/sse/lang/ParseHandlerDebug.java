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


/** Tracing parser handler - logs what the core parse sees */ 

public class ParseHandlerDebug implements ParseHandler
{
    int count = 0 ;
    
    private void indent()
    {
        for ( int i = 0 ; i < count ; i++ ) System.out.print("  ") ;
    }

    private void start(int line, int column)
    { 
        System.out.print("["+line+", "+column+"]  ") ; 
        indent() ;
    }

    @Override
    public Item getItem()       { return null ; }
    
    @Override
    public void parseStart()
    { System.out.println("<<<<") ; }

    @Override
    public void parseFinish()
    { System.out.println(">>>>") ; }

    @Override
    public void listStart(int line, int column)
    { 
        start(line, column) ;
        count++ ;
        System.out.println("(") ;
    }

    @Override
    public void listFinish(int line, int column)
    {
        count-- ;         
        start(line, column) ;
        System.out.println(")") ;
    }



    @Override
    public void emitBNode(int line, int column, String label)
    { 
        start(line, column) ;
        System.out.println("BNode: "+label) ;
    }


    @Override
    public void emitIRI(int line, int column, String iriStr)
    { 
        start(line, column) ;
        System.out.println("IRI: "+iriStr) ;
    }

    @Override
    public void emitLiteral(int line, int column, String lex, String lang, String datatype_iri, String datatype_pn)
    { 
        start(line, column) ;
        if ( lang != null )
            System.out.println("Literal: "+lex+" @"+lang) ;
        else if ( datatype_iri != null )
            System.out.println("Literal: "+lex+" ^^"+datatype_iri) ;
        else if ( datatype_pn != null )
            System.out.println("Literal: "+lex+" ^^"+datatype_pn) ;
    }

    @Override
    public void emitPName(int line, int column, String pname)
    { 
        start(line, column) ;
        System.out.println("PName: "+pname) ;
    }

    @Override
    public void emitSymbol(int line, int column, String symbol)
    { 
        start(line, column) ;
        System.out.println("Symbol: "+symbol) ;
    }

    @Override
    public void emitVar(int line, int column, String varName)
    { 
        start(line, column) ;
        System.out.println("Var: "+varName) ;
    }

}
