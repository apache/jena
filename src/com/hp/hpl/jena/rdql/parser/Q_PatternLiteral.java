/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdql.parser;

import com.hp.hpl.jena.rdql.* ;
import java.io.PrintWriter;
import org.apache.oro.text.regex.Perl5Compiler ;


class Q_PatternLiteral extends SimpleNode implements Expr
{
    String patternString = null ;
    String modifiers = "" ;
    int mask ;

  Q_PatternLiteral(int id) {
    super(id);
  }

  Q_PatternLiteral(RDQLParser p, int id) {
    super(p, id);
  }

    public void setPattern(String str)
    {   
        // Unescaping done in parser for the marker - the rest is done in the regex package.
        // Don't do it again here.l
        //patternString = Q_TextLiteral.unescape(str,'\\') ;
        patternString = str ;
    }
    
    public void setModifiers(String str)
    {
        modifiers = modifiers+str ;
        for ( int i = 0 ; i < modifiers.length() ; i++ )
        {
            switch(modifiers.charAt(i))
            {
                case 'i' : mask |= Perl5Compiler.CASE_INSENSITIVE_MASK; break;
                case 'm' : mask |= Perl5Compiler.MULTILINE_MASK; break;
                case 's' : mask |= Perl5Compiler.SINGLELINE_MASK; break;
                case 'x' : mask |= Perl5Compiler.EXTENDED_MASK; break;
                // Parser should catch this.
                //default  :
            }
        }
    }

    public String toString()
    {
        // First see if we can do it without escapes
        if ( patternString.indexOf('/') == -1 )
            return "/"+patternString+"/"+modifiers ;

        if ( patternString.indexOf('!') == -1 )
            return "m!"+patternString+"!"+modifiers ;
        
        if ( patternString.indexOf('%') == -1 )
            return "m%"+patternString+"%"+modifiers ;
        
        // No - need to escape
        char marker = '!' ;
        return "m"+marker+quote(patternString, marker)+marker+modifiers ;
    }
    
    private String quote(String pString, char marker)
    {
        int i = 0 ;
        while(true)
        {
            int j = pString.indexOf(marker,i) ;
            if ( j == -1 )
                   break ;
            pString = pString.substring(0, j)+"\\"+pString.substring(j) ;
            // +2 because pString just got longer.
            i = j+2 ;
        }
        return pString ;
    }
    
    
    public void print(PrintWriter pw, int level)
    {
        pw.print(toString()) ;
    }
    
    public Value eval(Query q, ResultBinding env)
    {
        throw new RDQL_InternalErrorException("Q_PatternLiteral.eval called!") ;
        //return null ;
    }
    
    public String asPrefixString()
    {
        return toString() ;
    }
    
    public String asInfixString()
    {
        return toString() ;
    }
    
}

/*
 *  (c) Copyright Hewlett-Packard Company 2001-2003
 *  All rights reserved.
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
 *
 * This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/).
 *
 */
