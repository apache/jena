/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Limited
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot;

import static org.openjena.riot.ErrorHandlerLib.errorHandlerStd ;

import java.util.Iterator ;
import java.util.regex.Pattern ;

import org.openjena.atlas.lib.Cache ;
import org.openjena.atlas.lib.CacheFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIComponents ;
import com.hp.hpl.jena.iri.Violation ;
import com.hp.hpl.jena.iri.ViolationCodes ;
import com.hp.hpl.jena.sparql.lib.IRILib ;

/** A checker validates RDF terms. */
public final class Checker
{
    private boolean allowRelativeIRIs = false ;
    private boolean warningsAreErrors = false ;
    private ErrorHandler handler ;

    public Checker()
    {
        this(null) ;
    }
    
    public Checker(ErrorHandler handler)
    {
        if ( handler == null )
            handler = errorHandlerStd ;
        this.handler = handler ;
    }
    
    public ErrorHandler getHandler()                { return handler ; } 
    public void setHandler(ErrorHandler handler)    { this.handler = handler ; }
    
    public boolean check(Node node, long line, long col)
    {
        if ( node.isURI() )             return checkURI(node, line, col) ;
        else if ( node.isBlank() )      return checkBlank(node, line, col) ;
        else if ( node.isLiteral() )    return checkLiteral(node, line, col) ;
        else if ( node.isVariable() )   return checkVar(node, line, col) ;
        handler.warning("Not a recognized node: ", line, col) ;
        return false ;
    }

    /** Check a triple - assumes individual nodes are legal */
    public boolean check(Triple triple, long line, long col) 
    {
        return checkTriple(triple.getSubject(), triple.getPredicate(), triple.getObject(), line, col) ; 
    }
    
    /** Check a triple against the RDF rules for a triple : subject is a IRI or bnode, predicate is a IRI and object is an bnode, literal or IRI */
    public boolean checkTriple(Node subject, Node predicate, Node object, long line, long col) 
    {
        boolean rc = true ;
    
        if ( subject == null || ( ! subject.isURI() && ! subject.isBlank() ) )
        {
            handler.error("Subject is not a URI or blank node", line, col) ;
            rc = false ;
        }
        if ( predicate == null || ( ! predicate.isURI() ) )
        {
            handler.error("Predicate not a URI", line, col) ;
            rc = false ;
        }
        if ( object == null || ( ! object.isURI() && ! object.isBlank() && ! object.isLiteral() ) )
        {
            handler.error("Object is not a URI, blank node or literal", line, col) ;
            rc = false ;
        }
        return rc ;
    }
    
    public static boolean validate(String msg, Triple triple)
    {
        return validate(msg, triple.getSubject() , triple.getPredicate() , triple.getObject() ) ;
    }
    
    public static boolean validate(String msg, Node subject, Node predicate, Node object)
    {
        if ( msg == null )
            msg = "Validation" ;
        if ( subject == null || ( ! subject.isURI() && ! subject.isBlank() ) )
        {
            errorHandlerStd.error(msg+": Subject is not a URI or blank node", -1, -1) ;
            return false ;
        }
            
        if ( predicate == null || ( ! predicate.isURI() ) )
        {
            errorHandlerStd.error(msg+": Predicate not a URI", -1, -1) ;
            return false ;
        }
        if ( object == null || ( ! object.isURI() && ! object.isBlank() && ! object.isLiteral() ) )
        {
            errorHandlerStd.error(msg+": Object is not a URI, blank node or literal", -1 ,-1) ;
            return false ;
        }
        return true ;
    }

    
    final public boolean checkVar(Node node, long line, long col)
    { return true ; }

    final private Pattern langPattern = Pattern.compile("[a-zA-Z]{1,8}(-[a-zA-Z]{1,8})*") ;
    
    final public boolean checkLiteral(Node node, long line, long col)
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

    private final boolean validateByDatatype(LiteralLabel lit, Node node, long line, long col)
    {
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
    
    final public boolean checkBlank(Node node, long line, long col)
    {
        String x =  node.getBlankNodeLabel() ;
        if ( x.indexOf(' ') >= 0 )
        {
            handler.error("Illegal blank node label (contains a space): "+node, line, col) ;
            return false ; 
        }
        return true ;
    }

    final public boolean checkIRI(IRI iri, long line, long col)
    {
        violations(iri, handler, allowRelativeIRIs, warningsAreErrors, line, col) ;
        return ! iri.hasViolation(true) ;
    }

    // An LRU cache is slower.
    // An unbounded cache is fastest but does not scale.
    private final Cache<Node, IRI> cache = CacheFactory.createSimpleCache(5000) ;
    
    public final boolean checkURI(Node node, long line, long col)
    {
        if ( cache != null && cache.containsKey(node) )
            return true ;
        
        IRI iri = IRILib.iriFactory.create(node.getURI()); // always works - no exceptions.
        boolean b = checkIRI(iri, line, col) ;
        // If OK, put in cache.
        if ( cache != null && b )
            cache.put(node, iri) ;
        return b ;
    }

    public static void violationsIRI(IRI iri, ErrorHandler handler, boolean allowRelativeIRIs, boolean warningsAreErrors)
    {
        violations(iri, handler, allowRelativeIRIs, warningsAreErrors, -1, -1) ;
    }

    /** Process violations on an IRI
     *  Calls the errorhandler on all errors and warnings (as warning) then
     *  calls the errorHandler for an error.   
     */
    public static void violations(IRI iri, ErrorHandler handler, 
                                  boolean allowRelativeIRIs, boolean warningsAreErrors, 
                                  long line, long col)
    {
        if ( iri.hasViolation(true) )
        {
            Iterator<Violation> iter = iri.violations(true) ; 
            
            boolean errorSeen = false ;
            boolean warningSeen = false ;
            
            // What to finally report.
            Violation vError = null ;
            Violation vWarning = null ;
            Violation xvSub = null ;
            
            for ( ; iter.hasNext() ; )
            {
                Violation v = iter.next();
                int code = v.getViolationCode() ;
                boolean isError = v.isError() ;
                
                // Ignore these.
                if ( code == Violation.LOWERCASE_PREFERRED ||
                    code == Violation.PERCENT_ENCODING_SHOULD_BE_UPPERCASE )
                    continue ;

                // Anything we want to reprioritise?
                // [nothing at present]
                
                // Remember first error and first warning.
                if ( isError )
                {
                    errorSeen = true ;
                    if ( vError == null )
                        // Remember first error
                        vError = v ;
                }
                else
                {
                    warningSeen = true ;
                    if ( vWarning == null )
                        vWarning = v ;
                }
                
                String msg = v.getShortMessage();
                String iriStr = iri.toString();

                // Ideally, we might want to output all messages relating to this IRI
                // then cause the error or continue.
                // But that's tricky given the current errorhandler architecture.
                
                // Put out warnings for all IRI issues - later, exception for errors.
                if (v.getViolationCode() == ViolationCodes.REQUIRED_COMPONENT_MISSING &&
                    v.getComponent() == IRIComponents.SCHEME)
                {
                    if (! allowRelativeIRIs )
                        handler.error("Relative URIs are not permitted in RDF: <"+iriStr+">", line, col);
                } 
                else
                {
                    if ( isError || warningsAreErrors )
                        // IRI errors are warning at the level of parsing - they got through syntax checks.  
                        handler.warning("Bad IRI: "+msg, line, col);
                    else
                        handler.warning("Not advised IRI: "+msg, line, col);
                }
            }
            
//            // and report our choosen error.
//            if ( errorSeen || (warningsAreErrors && warningSeen) )
//            {
//                String msg = null ;
//                if ( vError != null ) msg = vError.getShortMessage() ;
//                if ( msg == null && vWarning != null ) msg = vWarning.getShortMessage() ;
//                if ( msg == null )
//                    handler.error("Bad IRI: <"+iri+">", line, col) ;
//                else
//                    handler.error("Bad IRI: "+msg, line, col) ;
//            }
        }
    
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Limited
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