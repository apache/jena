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

package com.hp.hpl.jena.sparql.expr;

import java.security.MessageDigest ;
import java.security.NoSuchAlgorithmException ;

import org.apache.jena.atlas.lib.Bytes ;

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
