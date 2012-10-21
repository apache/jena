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

package com.hp.hpl.jena.n3;

//import org.apache.commons.logging.*;
import com.hp.hpl.jena.rdf.model.*;

/** A simple N3 writer - writes N3 out as triples with prefixes done.
 *  "N3 triples" - triples with N3 abbreviations and prefixes.
 *  Very simple.  
 */

public class N3JenaWriterTriples extends N3JenaWriterCommon
{
    static public final int colWidth = 8 ; 
    
    @Override
    protected void writeModel(Model model)
    {
        alwaysAllocateBNodeLabel = true ;
        StmtIterator sIter = model.listStatements() ;
        for ( ; sIter.hasNext() ; )
        {
            Statement stmt = sIter.nextStatement() ;
            String subjStr = formatResource(stmt.getSubject()) ;
            
            out.print(subjStr) ;
            padCol(subjStr) ; 
            out.print(minGapStr) ;
            
            
            String predStr = formatProperty(stmt.getPredicate()) ;
            out.print(predStr) ;
            padCol(predStr) ;
            out.print(minGapStr) ;
            
            out.print( formatNode(stmt.getObject()) ) ;
            out.println(" .") ; 
        }
        sIter.close() ;
    }
    
    private void padCol(String tmp)
    {
        if ( tmp.length() < (colWidth) )
            out.print(pad( colWidth - tmp.length())) ;
    }
}
