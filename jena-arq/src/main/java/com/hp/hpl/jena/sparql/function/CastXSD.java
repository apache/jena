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

package com.hp.hpl.jena.sparql.function;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDAbstractDateTimeType ;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDBaseNumericType ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;

/** General casting of XSD datatypes. 
 * @see CastXSD_DateTime 
 * @see CastXSD_Numeric 
 * @see CastXSD_Boolean 
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

        /*
         * In RDF (2004 at least), whitespace is not legal in lexcial forms of, for example, integers.
         *    http://www.w3.org/TR/rdf-concepts/#section-Datatypes
         * Whitespace facet processing is part of XML processing, not RDF's lexical to value mapping. 
         */
        static boolean whitespaceSurroundAllowed = false ; // ! ARQ.isStrictMode() ;
        
        protected NodeValue cast(String s, NodeValue nv, XSDDatatype castType2)
        {
            if ( whitespaceSurroundAllowed ) 
                // Maybe more convenient, but is not strictly correct.
                s = s.trim() ;
            else
            {
                // Correct mode.  No white space allowed around values types numeric, boolean, dateTime, ... 
                // See also "JenaParameters.enableWhitespaceCheckingOfTypedLiterals" which defaults to false (accept surrounding whitespace)
                // This CastXSD - not need to check it is an XSD datatype.
                // if ( castType.getURI().startsWith(XSDDatatype.XSD) &&

                if ( castType instanceof XSDBaseNumericType || 
                     castType.equals(XSDDatatype.XSDfloat) ||
                     castType.equals(XSDDatatype.XSDdouble) ||
                     castType.equals(XSDDatatype.XSDboolean) ||
                     castType instanceof XSDAbstractDateTimeType )   // Includes durations, and Gregorian
                {
                    if ( s.startsWith(" ") || s.endsWith(" ") )
                        throw new ExprEvalException("CastXSD: Not a valid literal form (has whitespace): '"+s+"'") ;
                }
            }

            // Plain cast.
            try {
                if ( ! castType.isValid(s) )
                    throw new ExprEvalException("CastXSD: Not a valid literal form: '"+s+"'") ;
                // Unfortunately, validity testing happens in NodeValue.makeNode as well.
                // but better error messages this way.
                return NodeValue.makeNode(s, castType) ;
            } catch (RuntimeException ex) {
                throw new ExprEvalException("CastXSD: Not a strictly valid literal form: '"+s+"'") ;
            }
        }
    }
}
