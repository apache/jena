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

package org.apache.jena.riot.checker;

import java.util.regex.Pattern ;

import org.apache.jena.riot.SysRIOT ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException ;
import org.apache.xerces.impl.dv.ValidatedInfo ;
import org.apache.xerces.impl.dv.ValidationContext ;
import org.apache.xerces.impl.dv.XSSimpleType ;
import org.apache.xerces.impl.validation.ValidationState ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDAbstractDateTimeType ;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDBaseNumericType ;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDDouble ;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDFloat ;
import com.hp.hpl.jena.graph.Node ;

public class CheckerLiterals implements NodeChecker
{
    // A flag to enable the test suite to read bad data.
    public static boolean WarnOnBadLiterals = true ;
    
    private ErrorHandler handler ;
    public CheckerLiterals(ErrorHandler handler)
    {
        this.handler = handler ;
    }
    
    @Override
    public boolean check(Node node, long line, long col)
    { return node.isLiteral() && checkLiteral(node, handler, line, col) ; }
    
    final static private Pattern langPattern = Pattern.compile("[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*") ;

    public static boolean checkLiteral(Node node, ErrorHandler handler, long line, long col)
    {
        if ( ! node.isLiteral() )
        {
            handler.error("Not a literal: "+node, line, col) ;
            return false ;
        }
       
        return checkLiteral(node.getLiteralLexicalForm(), node.getLiteralLanguage(), node.getLiteralDatatype(),  
                            handler, line, col) ;
    }
    
    
    public static boolean checkLiteral(String lexicalForm, RDFDatatype datatype, 
                                       ErrorHandler handler, long line, long col)
    {
        return checkLiteral(lexicalForm, null, datatype, handler, line, col) ;
    }

    public static boolean checkLiteral(String lexicalForm, String lang, 
                                       ErrorHandler handler, long line, long col)
    {
        return checkLiteral(lexicalForm, lang, null, handler, line, col) ;
    }
    
    public static boolean checkLiteral(String lexicalForm, String lang, RDFDatatype datatype, 
                                       ErrorHandler handler, long line, long col)
    {
        if ( ! WarnOnBadLiterals )
            return true ;
        
        boolean hasLang = lang != null && ! lang.equals("") ;
        
        if ( datatype != null && hasLang )
            handler.error("Literal has datatype and language", line, col) ;
        
        // Datatype check (and plain literals are always well formed)
        if ( datatype != null )
            return validateByDatatype(lexicalForm, datatype, handler, line, col) ;
        
        // No datatype.  Language?

        if ( hasLang )
        {
            // Not a perfect test.
            if ( lang.length() > 0 && ! langPattern.matcher(lang).matches() ) 
            {
                handler.warning("Language not valid: "+lang, line, col) ;
                return false; 
            }
        }
        
        return true ;
    }

    protected static boolean validateByDatatype(String lexicalForm, RDFDatatype datatype, ErrorHandler handler, long line, long col)
    {
        // XXX Reconsider.
        if ( SysRIOT.StrictXSDLexicialForms )
        {
            if ( datatype instanceof XSDBaseNumericType || datatype instanceof XSDFloat || datatype instanceof XSDDouble )
                return validateByDatatypeNumeric(lexicalForm, datatype, handler, line, col) ;
            if ( datatype instanceof XSDAbstractDateTimeType )
                return validateByDatatypeDateTime(lexicalForm, datatype, handler, line, col) ;
        }
        return validateByDatatypeJena(lexicalForm, datatype, handler, line, col) ;
    }

    protected static boolean validateByDatatypeJena(String lexicalForm, RDFDatatype datatype, ErrorHandler handler, long line, long col)
    {
        if ( datatype.isValid(lexicalForm) )
            return true ; 
        handler.warning("Lexical form '"+lexicalForm+"' not valid for datatype "+datatype.getURI(), line, col) ;
        return false ;
    }
    
    protected static boolean validateByDatatypeDateTime(String lexicalForm, RDFDatatype datatype, ErrorHandler handler, long line, long col)
    {
        if ( lexicalForm.contains(" ") )  { handler.warning("Whitespace in XSD date or time literal: '"+lexicalForm+"'", line, col) ; return false ; }
        if ( lexicalForm.contains("\n") ) { handler.warning("Newline in XSD date or time literal: '"+lexicalForm+"'", line, col) ; return false ; }
        if ( lexicalForm.contains("\r") ) { handler.warning("Newline in XSD date or time literal: '"+lexicalForm+"'", line, col) ; return false ; }
        //if ( ! StrictXSDLexicialForms )
        // Jena is already strict.
        return validateByDatatypeJena(lexicalForm, datatype, handler, line, col) ;
    }
    
    protected static boolean validateByDatatypeNumeric(String lexicalForm, RDFDatatype datatype, ErrorHandler handler, long line, long col)
    {
        // Do a white space check as well for numerics.
        if ( lexicalForm.contains(" ") )  { handler.warning("Whitespace in numeric XSD literal: '"+lexicalForm+"'", line, col) ; return false ; } 
        if ( lexicalForm.contains("\n") ) { handler.warning("Newline in numeric XSD literal: '"+lexicalForm+"'", line, col) ; return false ; }
        if ( lexicalForm.contains("\r") ) { handler.warning("Carriage return in numeric XSD literal: '"+lexicalForm+"'", line, col) ; return false ; }
        
//        if ( lit.getDatatype() instanceof XSDAbstractDateTimeType )
//        {
//            // Do a white space check as well for numerics.
//            if ( lex.contains(" ") )  { handler.warning("Whitespace in XSD date or time literal: "+node, line, col) ; return false ; } 
//            if ( lex.contains("\n") ) { handler.warning("Newline in XSD date or time literal: "+node, line, col) ; return false ; }
//            if ( lex.contains("\r") ) { handler.warning("Newline in XSD date or time literal: "+node, line, col) ; return false ; }
//        }
//
        if ( ! SysRIOT.StrictXSDLexicialForms )
            return validateByDatatypeJena(lexicalForm, datatype, handler, line, col) ;
        
        // From Jena 2.6.3, XSDDatatype.parse
        XSSimpleType typeDeclaration = (XSSimpleType)datatype.extendedTypeDefinition() ;
        try {
            ValidationContext context = new ValidationState();
            ValidatedInfo resultInfo = new ValidatedInfo();
            Object result = typeDeclaration.validate(lexicalForm, context, resultInfo);
            return true ;
        } catch (InvalidDatatypeValueException e) {
            handler.warning("Lexical form '"+lexicalForm+"' not valid for datatype "+datatype.getURI(), line, col) ;
            return false ;
        }
    }
}
