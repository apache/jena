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

import java.util.Map ;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.PrefixMapping2 ;

public class PrologueSerializer
{
    public static void output(IndentedWriter out, Prologue prologue)
    {
        printBase(prologue, out) ;
        printPrefixes(prologue, out) ;
    }
    
    private static void printBase(Prologue prologue, IndentedWriter out)
    {
        if ( prologue.getBaseURI() != null && prologue.explicitlySetBaseURI() )
        {
            out.print("BASE    ") ;
            out.print("<"+prologue.getBaseURI()+">") ;
            out.newline() ;
        }
    }

    public static void printPrefixes(Prologue prologue, IndentedWriter out)
    {
        if ( prologue.getPrefixMapping() == null )
            return ;
        
        Map<String, String> pmap = null ;

        if ( prologue.getPrefixMapping() instanceof PrefixMapping2 )
        {
            PrefixMapping2 pm2 = (PrefixMapping2)prologue.getPrefixMapping() ;
            pmap = pm2.getNsPrefixMap(false) ;
        }
        else
        {
            Map<String, String> _pmap = prologue.getPrefixMapping().getNsPrefixMap() ;
            pmap = _pmap ;
        }

        if ( pmap.size() > 0 )
        {
            //boolean first = true ;
            for (String k : pmap.keySet())
            {
                String v = pmap.get(k) ;
                out.print("PREFIX  ") ;
                out.print(k) ;
                out.print(':') ;
                out.print(' ', 4-k.length()) ;
                // Include at least one space 
                out.print(' ') ;
                out.print(FmtUtils.stringForURI(v)) ;
                out.newline() ;
            }
        }
    }
}
