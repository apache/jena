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

package com.hp.hpl.jena.sparql.serializer;
import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

public abstract class FormatterBase
{
    protected IndentedWriter out ;
    protected SerializationContext context ;
    protected FormatterBase(IndentedWriter _out, SerializationContext _context)
    {
        out = _out ;
        context = _context ; 
    }
    
    public void startVisit()  {}
    public void finishVisit() { out.flush() ; }
    
    // Utilities
    
    protected void formatTriples(BasicPattern pattern)
    {
        FmtUtils.formatPattern(out, pattern, context) ;
    }
    
    protected void formatTriple(Triple tp)
    {
        out.print(slotToString(tp.getSubject())) ;
        out.print(" ") ;
        out.print(slotToString(tp.getPredicate())) ;
        out.print(" ") ;
        out.print(slotToString(tp.getObject())) ;
    }
    
    protected String slotToString(Node n)
    {
        return FmtUtils.stringForNode(n, context) ;
    }


}
