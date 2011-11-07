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

import java.math.BigDecimal ;
import java.math.BigInteger ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;

public class CastXSD_Boolean extends CastXSD
{
    public CastXSD_Boolean(XSDDatatype dt)
    {
        super(dt) ;
    }
    
    @Override
    public Function create(String uri)
    {        
        return new Instance(castType) ;
    }


    protected static class Instance extends CastXSD.Instance 
    {
        Instance(XSDDatatype dt)
        {
            super(dt) ;
        }

        @Override
        protected NodeValue cast(String s, NodeValue nv, XSDDatatype castType)
        {
            if ( nv.isNumber() )
            {
                if ( nv.isFloat() || nv.isDouble() )
                {
                    // 0, +0, -0, 0.0, 0.0E0 or NaN, then TV is false.
                    // else true.
                    double d = ( nv.isDouble() ? nv.getDouble() : nv.getFloat() ) ;
                    if ( d == 0.0e0 || Double.isNaN(d) )
                        return NodeValue.FALSE ;
                    return NodeValue.TRUE ;
                }
                if ( nv.isDecimal() ) 
                {
                    if ( nv.getDecimal().compareTo(BigDecimal.ZERO) == 0 )
                        return NodeValue.FALSE ;
                    return NodeValue.TRUE ;
                }
                
                if ( nv.isInteger() )
                {
                    if ( nv.getInteger().compareTo(BigInteger.ZERO) == 0 )
                        return NodeValue.FALSE ;
                    return NodeValue.TRUE ;
                }
            }
            
            if ( nv.isBoolean() )
                return nv ;
            return super.cast(s, nv, castType) ;
        }
    }
}
