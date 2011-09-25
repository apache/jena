/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import java.security.MessageDigest ;
import java.security.NoSuchAlgorithmException ;

import org.openjena.atlas.lib.Bytes ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;

public abstract class ExprDigest extends ExprFunction1
{
    private final String digestName ;
    private MessageDigest digestCache ;

    public ExprDigest(Expr expr, String symbol, String digestName)
    {
        super(expr, symbol) ;
        this.digestName = digestName ; 
        try
        {
            digestCache = MessageDigest.getInstance(digestName) ;
        } catch (NoSuchAlgorithmException e)
        {
            throw new ARQInternalErrorException("Digest not provided in this Java system: "+digestName) ;
        }
    }

    // Each digest expr function is unique 
    // Pool needs to be static and per-digest-type

//    private Pool<MessageDigest> pool = PoolSync.create(new PoolBase<MessageDigest>()) ;
//    private MessageDigest getDigest()
//    {
//        MessageDigest md = pool.get() ;
//        if ( md == null )
//        {
//            synchronized (pool)
//            {
//                md = pool.get() ;
//                if ( md == null )
//                {
//                    md = createDigest() ;
//                    pool.put(md) ;
//                }
//            }
//        }
//        return md ;
//    }
    
    private MessageDigest getDigest()
    {
        if ( digestCache != null )
        {
            MessageDigest digest2 = null ;
            try {
                digest2 = (MessageDigest)digestCache.clone() ;
                return digest2 ;
            } catch (CloneNotSupportedException ex)
            {
                // Can't clone - remove cache copy.
                digestCache = null ;
            }
        }
        return createDigest() ;
    }
    
    private MessageDigest createDigest()
    {
        try { return MessageDigest.getInstance(digestName) ; }
        catch (Exception ex2) { throw new ARQInternalErrorException(ex2) ; } 
    }
    
    NodeValue lastSeen = null ;
    NodeValue lastCalc = null ;
    
    @Override
    public NodeValue eval(NodeValue v)
    {
        if ( lastSeen != null && lastSeen.equals(v) )
            return lastCalc ;
        
        Node n = v.asNode() ;
        if ( ! n.isLiteral() )
            throw new ExprEvalException("Not a literal: "+v) ;
        if ( n.getLiteralLanguage() != null && ! n.getLiteralLanguage().equals("") )
            throw new ExprEvalException("Can't make a digest of an RDF term with a language tag") ; 
        // Literal, no language tag.
        if ( n.getLiteralDatatype() != null && ! XSDDatatype.XSDstring.equals(n.getLiteralDatatype()) )
            throw new ExprEvalException("Not a simple literal nor an XSD string") ;
        
        try { 
            MessageDigest digest = getDigest() ;
            String x = n.getLiteralLexicalForm() ;
            byte b[] = x.getBytes("UTF-8") ;
            byte d[] = digest.digest(b) ;
            String y = Bytes.asHexLC(d) ;
            NodeValue result = NodeValue.makeString(y) ;
            
            // Cache
            lastSeen = v ;
            lastCalc = result ;
            
            return result ;
            
        } catch (Exception ex2) { throw new ARQInternalErrorException(ex2) ; } 
        
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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
