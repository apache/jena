/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;

import java.util.Iterator;

import atlas.lib.Cache;
import atlas.lib.CacheFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIComponents;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;
import com.hp.hpl.jena.iri.ViolationCodes;
import com.hp.hpl.jena.shared.JenaException;

public class Checker
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

        iriFactory.create("");
    }
    
    private boolean allowRelativeIRIs = false ;
    private boolean warningsAreErrors = true ;
    private ErrorHandler handler ;

    public Checker(ErrorHandler handler)
    {
        this.handler = handler ;
        if ( this.handler == null )
            this.handler = new ErrorHandlerStd() ;
    }
    
    public ErrorHandler getHandler() { return handler ; } 
    
    public void check(Node node)
    {
        if ( node.isURI() )             checkURI(node) ;
        else if ( node.isBlank() )      checkBlank(node) ;
        else if ( node.isLiteral() )    checkLiteral(node) ;
        else if ( node.isVariable() )   checkVar(node) ;
    }

    /** Check a triple - assumes individual nodes are legal */
    public void check(Triple triple) 
    {
        validate(null, triple) ;
    }
    
    public static void validate(String msg, Triple triple)
    {
        validate(msg, triple.getSubject() , triple.getPredicate() , triple.getObject() ) ;
    }
    
    public static void validate(String msg, Node subject, Node predicate, Node object)
    {
        if ( msg == null )
            msg = "Validation" ;
        if ( subject == null || ( ! subject.isURI() && ! subject.isBlank() ) )
            throw new RiotException(msg+": Subject is not a URI or blank node") ;
        if ( predicate == null || ( ! predicate.isURI() ) )
            throw new RiotException(msg+": Predicate not a URI") ;
        if ( object == null || ( ! object.isURI() && ! object.isBlank() && ! object.isLiteral() ) )
            throw new RiotException(msg+": Object is not a URI, blank node or literal") ;
    }

    
    final private void checkVar(Node node)
    {}

    final private void checkLiteral(Node node)
    {
        LiteralLabel lit = node.getLiteral() ;

        // Datatype check (and plain literals are always well formed)
        if ( lit.getDatatype() != null && ! lit.isWellFormed() )
            throw new JenaException("Lexical not valid for datatype: "+node) ;

        //        // Not well formed.
        //        if ( lit.getDatatype() != null )
        //        {
        //            if ( ! lit.getDatatype().isValid(lit.getLexicalForm()) )
        //                throw new JenaException("Lexical not valid for datatype: "+node) ;
        //        }

        if (lit.language() != null )
        {
            // Not a pefect test.
            String lang = lit.language() ;
            if ( lang.length() > 0 && ! lang.matches("[a-z]{1,8}(-[a-z]{1,8})*") )
                throw new JenaException("Language not valid: "+node) ;
        }
    }

    final private void checkBlank(Node node)
    {
        String x =  node.getBlankNodeLabel() ;
        if ( x.indexOf(' ') >= 0 )
            throw new JenaException("Illegal blank node label (contains a space): "+node) ;
    }

    public void checkIRI(IRI iri)
    {
        violations(iri, handler, allowRelativeIRIs, warningsAreErrors) ;
    }

    private Cache<Node, IRI> cache = CacheFactory.createSimpleCache(1000) ;
    
    final private void checkURI(Node node)
    {
        if ( cache != null && cache.containsKey(node) )
            return ;
        
        IRI iri = iriFactory.create(node.getURI()); // always works - no exceptions.
        checkIRI(iri) ;
        // If OK, put in cache.
        if ( cache != null && ! iri.hasViolation(true) )
            cache.put(node, iri) ;
    }

    /** Process violations on an IRI
     *  Calls the errorhandler on all errors and warnings (as warning) then
     *  calls the errorHandler for an error.   
     */
    public static void violations(IRI iri, ErrorHandler handler, boolean allowRelativeIRIs, boolean warningsAreErrors)
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
                        handler.warning("Relative URIs are not permitted in RDF: specifically <"+iriStr+">");
                } 
                else
                    handler.warning("Bad IRI: "+msg);
            }
            
            // and report our choosen error.
            if ( errorSeen || (warningsAreErrors && warningSeen) )
            {
                String msg = null ;
                if ( vError != null ) msg = vError.getShortMessage() ;
                if ( msg == null && vWarning != null ) msg = vWarning.getShortMessage() ;
                if ( msg == null )
                    handler.error("Bad IRI: "+iri) ;
                else
                    handler.error("Bad IRI: "+iri+" : "+msg) ;
                
                
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