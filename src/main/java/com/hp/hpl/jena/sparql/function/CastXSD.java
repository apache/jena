/**
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

package com.hp.hpl.jena.sparql.function;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;

/** General casting : lexcial form must be right. 
 * @see CastXSD_DateTime 
 * @see CastXSD_Numeric 
 */
public class CastXSD implements FunctionFactory
{
    protected final XSDDatatype castType ;
    
    public CastXSD(XSDDatatype dt)
    {
        this.castType = dt ; 
    }
    
    @Override
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
