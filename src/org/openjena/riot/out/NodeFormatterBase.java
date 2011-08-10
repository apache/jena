/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.out;

import java.io.Writer ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;

public abstract class NodeFormatterBase implements NodeFormatter
{

    //@Override
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
        else
            throw new ARQInternalErrorException("Unknow node type: "+n) ;
    }
    
    //@Override
    public void formatURI(Writer w, Node n)         { formatURI(w, n.getURI()) ; }

    //@Override
    public void formatBNode(Writer w, Node n)       { formatBNode(w, n.getBlankNodeLabel()) ; }

    //@Override
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

    //@Override
    public void formatVar(Writer w, Node n)         { formatVar(w, n.getName()) ; }
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */