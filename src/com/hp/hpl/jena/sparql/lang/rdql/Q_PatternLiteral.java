/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lang.rdql;
import java.util.regex.Pattern;

import com.hp.hpl.jena.graph.query.IndexValues ;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.util.IndentedWriter;



public class Q_PatternLiteral extends ExprNodeRDQL implements ExprRDQL
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
                case 'i' :
                    mask |= Pattern.CASE_INSENSITIVE;
                    mask |= Pattern.UNICODE_CASE ;
                    break ;
                case 'm' : mask |= Pattern.MULTILINE ;           break ;
                case 's' : mask |= Pattern.DOTALL ;              break ;
                //case 'x' : mask |= Pattern.;  break ;
                
                // Parser should catch this.
                //default  :
            }
        }
    }

    @Override
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
    
    public String getPatternString()  { return patternString ; }
    public String getModifiers()      { return modifiers ; }
    
    // -----------
    // graph.query.Expression

    @Override
    public boolean isConstant()      { return true; }
    @Override
    public Object getValue()         { return toString(); } // For constants

    // -----------
    
    @Override
    public void format(IndentedWriter w)
    {
        w.print(toString()) ;
    }
    
    public RDQL_NodeValue evalRDQL(Query q, IndexValues env)
    {
        throw new ARQInternalErrorException("Q_PatternLiteral.eval called!") ;
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
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
 */
