/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.checker;

import java.util.regex.Pattern ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;

import org.openjena.riot.ErrorHandler ;

public class CheckerLiterals implements NodeChecker
{
    private ErrorHandler handler ;
    private boolean allowBadLexicalForms ;

    public CheckerLiterals(ErrorHandler handler, boolean allowBadLexicalForms)
    {
        this.handler = handler ;
        this.allowBadLexicalForms = allowBadLexicalForms ;
    }
    
    public boolean check(Node node, long line, long col)
    { return node.isLiteral() && checkLiteral(node, line, col) ; }
    
    final private Pattern langPattern = Pattern.compile("[a-zA-Z]{1,8}(-[a-zA-Z]{1,8})*") ;
    
    public boolean checkLiteral(Node node, long line, long col)
    {
        LiteralLabel lit = node.getLiteral() ;

        // Datatype check (and plain literals are always well formed)
        if ( lit.getDatatype() != null )
            return validateByDatatype(lit, node, line, col) ;
        
        // No datatype.
        String lang = lit.language() ;
        if (lang != null && ! lang.equals("") )
        {
            // Not a perfect test.
            if ( lang.length() > 0 && ! langPattern.matcher(lang).matches() ) 
            {
                handler.warning("Language not valid: "+node, line, col) ;
                return false; 
            }
        }
        
        return true ;
    }

    protected boolean validateByDatatype(LiteralLabel lit, Node node, long line, long col)
    {
        if ( allowBadLexicalForms )
            return true ;
        
        String lex = lit.getLexicalForm() ;
        boolean b = lit.getDatatype().isValidLiteral(lit) ;
        if ( !b ) 
            handler.warning("Lexical form not valid for datatype: "+node, line, col) ;
        return b ;
        
        // Not sure about this.  white space for XSD numbers is whitespace facet collapse. 
        //Just: return lit.getDatatype().isValidLiteral(lit) ;

//        if ( ! ( lit.getDatatype() instanceof XSDDatatype ) )
//            return lit.getDatatype().isValidLiteral(lit) ;
//
//        if ( lit.getDatatype() == XSDDatatype.XSDstring || lit.getDatatype() == XSDDatatype.XSDnormalizedString )
//            return true ;
//
//        // Enforce whitespace checking.
//        if ( lit.getDatatype() instanceof XSDBaseNumericType || lit.getDatatype() instanceof XSDFloat || lit.getDatatype() instanceof XSDDouble )
//        {
//            // Do a white space check as well for numerics.
//            if ( lex.contains(" ") )  { handler.warning("Whitespace in numeric XSD literal: "+node, line, col) ; return false ; } 
//            if ( lex.contains("\n") ) { handler.warning("Newline in numeric XSD literal: "+node, line, col) ; return false ; }
//            if ( lex.contains("\r") ) { handler.warning("Newline in numeric XSD literal: "+node, line, col) ; return false ; }
//        }
//
//        if ( lit.getDatatype() instanceof XSDAbstractDateTimeType )
//        {
//            // Do a white space check as well for numerics.
//            if ( lex.contains(" ") )  { handler.warning("Whitespace in XSD date or time literal: "+node, line, col) ; return false ; } 
//            if ( lex.contains("\n") ) { handler.warning("Newline in XSD date or time literal: "+node, line, col) ; return false ; }
//            if ( lex.contains("\r") ) { handler.warning("Newline in XSD date or time literal: "+node, line, col) ; return false ; }
//        }
//
//        // From Jena 2.6.3, XSDDatatype.parse
//        XSSimpleType typeDeclaration = (XSSimpleType)lit.getDatatype().extendedTypeDefinition() ;
//        try {
//            ValidationContext context = new ValidationState();
//            ValidatedInfo resultInfo = new ValidatedInfo();
//            Object result = typeDeclaration.validate(lex, context, resultInfo);
//            return true ;
//        } catch (InvalidDatatypeValueException e) {
//            handler.warning("Lexical form not valid for datatype: "+node, line, col) ;
//            return false ;
//        }
    }

}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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