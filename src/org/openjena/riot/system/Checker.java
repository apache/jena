/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Limited
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.system;

import static org.openjena.riot.ErrorHandlerFactory.errorHandlerStd ;
import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.checker.CheckerBlankNodes ;
import org.openjena.riot.checker.CheckerIRI ;
import org.openjena.riot.checker.CheckerLiterals ;
import org.openjena.riot.checker.CheckerVar ;
import org.openjena.riot.checker.NodeChecker ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.iri.IRI ;

/** A checker validates RDF terms. */
public final class Checker
{
    private boolean allowRelativeIRIs = false ;
    private boolean warningsAreErrors = false ;
    private ErrorHandler handler ;
    
    private NodeChecker checkLiterals ;
    private NodeChecker checkURIs ;
    private NodeChecker checkBlankNodes ;
    private NodeChecker checkVars ;

    public Checker()
    {
        this(null) ;
    }
    
    public Checker(ErrorHandler handler)
    {
        if ( handler == null )
            handler = errorHandlerStd ;
        this.handler = handler ;
        
        checkLiterals = new CheckerLiterals(handler) ;
       
        checkURIs = new CheckerIRI(handler, IRIResolver.iriFactory) ;
        checkBlankNodes = new CheckerBlankNodes(handler) ;
        checkVars = new CheckerVar(handler) ;        
    }

    public ErrorHandler getHandler()                { return handler ; } 
    public void setHandler(ErrorHandler handler)    { this.handler = handler ; }
    
    public boolean check(Node node, long line, long col)
    {
        // NodeVisitor?
        if      ( node.isURI() )        return checkIRI(node, line, col) ;
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
    { return checkVars.check(node, line, col) ; }

    final public boolean checkLiteral(Node node, long line, long col)
    { return checkLiterals.check(node, line, col) ; }
    
    final public boolean checkBlank(Node node, long line, long col)
    { return checkBlankNodes.check(node, line, col) ; }

    final public boolean checkIRI(Node node, long line, long col)
    { return checkURIs.check(node, line, col) ; }

    final public boolean checkIRI(IRI iri, long line, long col)
    { 
        if ( ! ( checkURIs instanceof CheckerIRI ) )
            return true ;
        
        return ((CheckerIRI)checkURIs).checkIRI(iri, line, col) ;
    }

    
    // Getters and setters
    
    public final NodeChecker getCheckLiterals()                       { return checkLiterals ; }
    public final void setCheckLiterals(NodeChecker checkLiterals)     { this.checkLiterals = checkLiterals ; }

    public final NodeChecker getCheckURIs()                           { return checkURIs ; }
    public final void setCheckURIs(NodeChecker checkURIs)             { this.checkURIs = checkURIs ; }

    public final NodeChecker getCheckBlankNodes()                     { return checkBlankNodes ; }
    public final void setCheckBlankNodes(NodeChecker checkBlankNodes) { this.checkBlankNodes = checkBlankNodes ; }

    public final NodeChecker getCheckVars()                           { return checkVars ; }
    public final void setCheckVars(NodeChecker checkVars)             { this.checkVars = checkVars ; }
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