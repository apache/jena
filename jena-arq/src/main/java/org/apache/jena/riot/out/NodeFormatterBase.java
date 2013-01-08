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

import java.io.IOException ;
import java.io.Writer ;

import org.apache.jena.atlas.io.IO ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;

/** Provide implementations of the operations of NodeFormatter in terms
 * of core operations for each node type.
 */
public abstract class NodeFormatterBase implements NodeFormatter
{
    @Override
    public void format(Writer w, Node n)
    {
        if ( n.isBlank() )
            formatBNode(w, n) ;
        else if ( n.isURI() )
            formatURI(w, n) ;
        else if ( n.isLiteral() )
            formatLiteral(w, n) ;
        else if ( n.isVariable() )
            formatVar(w, n) ;
        else if ( Node.ANY.equals(n) )
            try { w.write("ANY") ; }
            catch (IOException ex) { IO.exception(ex) ; }
        else
            throw new ARQInternalErrorException("Unknow node type: "+n) ;
    }
    
    @Override
    public void formatURI(Writer w, Node n)         { formatURI(w, n.getURI()) ; }

    @Override
    public void formatBNode(Writer w, Node n)       { formatBNode(w, n.getBlankNodeLabel()) ; }

    @Override
    public void formatLiteral(Writer w, Node n)
    {
        String dt = n.getLiteralDatatypeURI() ;
        String lang = n.getLiteralLanguage() ;
        String lex = n.getLiteralLexicalForm() ;
        
        if ( dt == null )
        {
            if ( lang == null || lang.equals("") )
                formatLitString(w, lex) ;
            else
                formatLitLang(w, lex,lang) ;
        }
        else
            formatLitDT(w, lex, dt) ;
    }

    @Override
    public void formatVar(Writer w, Node n)         { formatVar(w, n.getName()) ; }
}
