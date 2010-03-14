/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;

import static com.hp.hpl.jena.riot.ErrorHandlerLib.errorHandlerStd ;

import java.util.Iterator ;

import org.openjena.atlas.lib.Cache ;
import org.openjena.atlas.lib.CacheFactory ;


import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIComponents ;
import com.hp.hpl.jena.iri.IRIFactory ;
import com.hp.hpl.jena.iri.Violation ;
import com.hp.hpl.jena.iri.ViolationCodes ;

/** A checker validates RDF terms. */
public final class Checker
{
    //static IRIFactory iriFactory = IRIFactory.jenaImplementation() ;
    //static IRIFactory iriFactory = IRIFactory.iriImplementation();
    
    /** The IRI checker setup - more than usual Jena but not full IRI. */
    static IRIFactory iriFactory = new IRIFactory();
    static {
        // IRIFactory.iriImplementation() ...
        iriFactory.useSpecificationIRI(true);
        iriFactory.useSchemeSpecificRules("*",true);

        //iriFactory.shouldViolation(false,true);

        // Moderate it -- allow unwise chars and any scheme name.
        iriFactory.setIsError(ViolationCodes.UNWISE_CHARACTER,false);
        iriFactory.setIsWarning(ViolationCodes.UNWISE_CHARACTER,false);

        iriFactory.setIsError(ViolationCodes.UNREGISTERED_IANA_SCHEME,false);
        iriFactory.setIsWarning(ViolationCodes.UNREGISTERED_IANA_SCHEME,false);
    }
    
    private boolean allowRelativeIRIs = false ;
    private boolean warningsAreErrors = true ;
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

    final public boolean checkLiteral(Node node, long line, long col)
    {
        LiteralLabel lit = node.getLiteral() ;

        // Datatype check (and plain literals are always well formed)
        if ( lit.getDatatype() != null && ! lit.isWellFormed() )
        {
            handler.warning("Lexical not valid for datatype: "+node, line, col) ;
            return false; 
        }

        if (lit.language() != null )
        {
            // Not a perfect test.
            String lang = lit.language() ;
            if ( lang.length() > 0 && ! lang.matches("[a-zA-Z]{1,8}(-[a-zA-Z]{1,8})*") )
            {
                handler.warning("Language not valid: "+node, line, col) ;
                return false; 
            }
        }
        
        if ( lit.getDatatype() != null  && (lit.language() != null && ! lit.language().equals("")) )
        {
            handler.error("Illegal: Both language and datatype: "+node, line, col) ;
            return false; 
        }
        return true ;
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
        
        IRI iri = iriFactory.create(node.getURI()); // always works - no exceptions.
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
            Violation vSub = null ;
            
            for ( ; iter.hasNext() ; )
            {
                Violation v = iter.next();
                int code = v.getViolationCode() ;
                
                // Treat these with low priority.
                if ( code == Violation.LOWERCASE_PREFERRED ||
                    code == Violation.PERCENT_ENCODING_SHOULD_BE_UPPERCASE )
                {
                    if ( vSub == null )
                        vSub = v ;
                    continue ;
                }
                
                // Remember first.
                if ( v.isError() )
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
    
                // Put out warnings for all IRI issues - later, exception for errors.
                if (v.getViolationCode() == ViolationCodes.REQUIRED_COMPONENT_MISSING &&
                    v.getComponent() == IRIComponents.SCHEME)
                {
                    if (! allowRelativeIRIs )
                        handler.warning("Relative URIs are not permitted in RDF: specifically <"+iriStr+">", line, col);
                } 
                else
                    handler.warning("Bad IRI: "+msg, line, col);
            }
            
            // and report our choosen error.
            if ( errorSeen || (warningsAreErrors && warningSeen) )
            {
                String msg = null ;
                if ( vError != null ) msg = vError.getShortMessage() ;
                if ( msg == null && vWarning != null ) msg = vWarning.getShortMessage() ;
                if ( msg == null )
                    handler.error("Bad IRI: "+iri, line, col) ;
                else
                    handler.error("Bad IRI: "+iri+" : "+msg, line, col) ;
            }
        }
    
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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