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
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;

public class CastXSD_Numeric extends CastXSD
{
    public CastXSD_Numeric(XSDDatatype dt)
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
            if ( nv.isBoolean() )
            {
                boolean b = nv.getBoolean() ;
                if ( XSDDatatype.XSDfloat.equals(castType) || XSDDatatype.XSDdouble.equals(castType) )
                    s = ( b ? "1.0E0" : "0.0E0" ) ;
                else if ( XSDDatatype.XSDdecimal.equals(castType) )
                    s = ( b ? "1.0" : "0.0" ) ;
                else if ( XSDFuncOp.isIntegerType(castType)) 
                    s = ( b ? "1" : "0" ) ;
                // else do nothing and hope. 
            }
            
            return super.cast(s, nv, castType) ;
        }
    }
}
