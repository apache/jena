/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.function;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;

/** 
 * @author Andy Seaborne
 */

public class CastXSD implements FunctionFactory
{
    protected final XSDDatatype castType ;
    
    public CastXSD(XSDDatatype dt)
    {
        this.castType = dt ; 
    }
    
    public Function create(String uri)
    {        
        return new Instance(castType) ;
    }


    protected static class Instance extends FunctionBase1 
    {
        XSDDatatype castType ;
        Instance(XSDDatatype dt) {this.castType = dt ;  }

        @Override
        public NodeValue exec(NodeValue v)
        {
            // http://www.w3.org/TR/xpath-functions/#casting
            // xsd:boolean() needs to be a special.
            // casting from xsd:boolean is also special (get 0 or 1)

            String s = null ;
            Node n = v.asNode() ;

            if ( n.isBlank() )
                throw new ExprEvalException("CastXSD: Can't cast blank nodes: "+v) ;

            if ( n.isURI() )
            {
                if ( castType.equals(XSDDatatype.XSDstring) )
                    s = n.getURI() ;
                else
                    throw new ExprEvalException("CastXSD: Can't cast node: "+v+" to "+castType.getURI()) ;
            }
            else if ( n.isLiteral() ) 
                // What if there is a lang tag?
                s = n.getLiteralLexicalForm() ;
            else
                throw new ExprEvalException("CastXSD: Can't cast node: "+v+ "(not a literal, not URI to string)") ;

            if ( s == null && v.isString() ) 
                s = v.getString() ;

            if ( s == null )
                throw new ExprEvalException("CastXSD: Can't cast: "+v+ "(has no string appearance)") ;
            
            //        // Special case - non-normalised xsd:booleans use 0 and 1.
            //        if ( v.isBoolean() )
            //        {
            //            if ( s.equals("0") ) s = "false" ;
            //            if ( s.equals("1") ) s = "true" ;
            //        }

            NodeValue r = cast(s, v, castType) ;
            return r ;
        }

        protected NodeValue cast(String s, NodeValue nv, XSDDatatype castType2)
        {
            // Plain cast.
            if ( ! castType.isValid(s) )
                throw new ExprEvalException("CastXSD: Not a valid literal form: "+s) ;
            // Unfortunately, validity testing happens in NodeValue.makeNode as well.
            return NodeValue.makeNode(s, castType) ;
        }
    }
}
/*
 *  (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
