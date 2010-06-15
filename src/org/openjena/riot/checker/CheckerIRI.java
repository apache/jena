/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev.checker;

import java.util.Iterator ;

import org.openjena.atlas.lib.Cache ;
import org.openjena.atlas.lib.CacheFactory ;
import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.ErrorHandlerLib ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIComponents ;
import com.hp.hpl.jena.iri.IRIFactory ;
import com.hp.hpl.jena.iri.Violation ;
import com.hp.hpl.jena.iri.ViolationCodes ;
import com.hp.hpl.jena.sparql.lib.IRILib ;

public class CheckerIRI implements Check
{
    private boolean allowRelativeIRIs = false ;
    private boolean warningsAreErrors = false ;

    private ErrorHandler handler ;
    private IRIFactory iriFactory ;

    public CheckerIRI()
    {
        this(ErrorHandlerLib.errorHandlerStd, IRILib.iriFactory) ;
    }
    
    public CheckerIRI(ErrorHandler handler, IRIFactory iriFactory)
    {
        this.handler = handler ;
        this.iriFactory = iriFactory ;
    }
    
    public boolean check(Node node, long line, long col)
    { return node.isURI() && checkURI(node, line, col) ; }
    


    // An LRU cache is slower.
    // An unbounded cache is fastest but does not scale.
    private final Cache<Node, IRI> cache = CacheFactory.createSimpleCache(5000) ;

    // abstract
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

    // Abstract
    final public boolean checkIRI(IRI iri, long line, long col)
    {
        violations(iri, handler, allowRelativeIRIs, warningsAreErrors, line, col) ;
        return ! iri.hasViolation(true) ;
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