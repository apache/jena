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

package com.hp.hpl.jena.sparql.resultset;

import java.io.OutputStream ;
import java.io.PrintWriter ;

import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.util.FileUtils ;

public class PlainFormat implements ResultSetProcessor
{
    PrintWriter out ;
    int count = 0 ;
    boolean lineNumbers = true ;
    boolean first = true ;
    SerializationContext context ;

    public PlainFormat(OutputStream outStream, SerializationContext context)
    {
        this.out = FileUtils.asPrintWriterUTF8(outStream) ;
        this.context = context ;
    }
    
    public PlainFormat(OutputStream outStream, Prologue prologue)
    {
        this(outStream, new SerializationContext(prologue)) ;
    }
    
    @Override
    public void start(ResultSet rs) {}
    @Override
    public void finish(ResultSet rs) { out.flush() ; } 
    @Override
    public void start(QuerySolution qs)
    {
        count++ ;
        //insertLineNumber() ;
        first = true ;
    }
    
    @Override
    public void finish(QuerySolution qs) { out.println() ; }
    @Override
    public void binding(String varName, RDFNode value)
    {
        if ( value == null )
            return ; // Unbound
        if ( ! first )
            out.print(" ") ;
        // Would like to share code Binding here.
        String s = FmtUtils.stringForRDFNode(value, context) ;
        out.print("( ?"+varName+" = "+s+" )") ;
        first = false ;
    }
    
    void insertLineNumber()
    {
        if ( ! lineNumbers )
            return ;
        String s = Integer.toString(count) ;
        for ( int i = 0 ; i < 3-s.length() ; i++ )
            out.print(' ') ;
        out.print(s) ;
        out.print(' ') ;
    }

}
