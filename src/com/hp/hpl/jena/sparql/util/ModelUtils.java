/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.LiteralImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;


public class ModelUtils
{
    public static RDFNode convertGraphNodeToRDFNode(Node n, Model model)
    {
        if ( n.isVariable() )
            throw new QueryException("Variable: "+n) ;

        // Best way.
        if ( model != null )
             return model.asRDFNode(n) ;
        
        if ( n.isLiteral() )
            return new LiteralImpl(n, null) ;
                
        if ( n.isURI() || n.isBlank() )
            return new ResourceImpl(n, null) ;
        
        throw new ARQInternalErrorException("Unknown node type for node: "+n) ;
    }
    
 
    public static Statement tripleToStatement(Model model, Triple t)
    {
        if ( model == null )
            throw new ARQInternalErrorException("Attempt to create statement with null model") ;
        
        Node sNode = t.getSubject() ;
        Node pNode = t.getPredicate() ;
        Node oNode = t.getObject() ;
        
        if ( sNode.isLiteral() || sNode.isVariable() )
            return null ;
        
        if ( ! pNode.isURI() )  // Not variable, literal or blank.
            return null ;

        if ( oNode.isVariable() )
            return null ;
        
        return model.asStatement(t) ; 
//        RDFNode s = convertGraphNodeToRDFNode(sNode, model) ;
//        RDFNode p = convertGraphNodeToRDFNode(pNode, model) ;
//        if ( p instanceof Resource )
//            p = model.createProperty(((Resource)p).getURI()) ;
//        RDFNode o = convertGraphNodeToRDFNode(oNode, model) ;
//        
//        Statement stmt = model.createStatement((Resource)s, (Property)p, o) ;
//        return stmt ;
    }
 
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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