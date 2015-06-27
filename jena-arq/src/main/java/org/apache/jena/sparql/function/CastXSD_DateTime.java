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

package org.apache.jena.sparql.function;

import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp ;
import org.apache.jena.sparql.util.NodeUtils ;


/** Cast DateTime */
public class CastXSD_DateTime extends CastXSD implements FunctionFactory
{
    public CastXSD_DateTime(XSDDatatype dt)
    {
        super(dt) ;
    }
    
    @Override
    public Function create(String uri)
    {        
        return new InstanceDT(castType) ;
    }

    protected static class InstanceDT extends CastXSD.Instance 
    {
        InstanceDT(XSDDatatype dt) { super(dt) ; }

        @Override
        protected NodeValue cast(String s, NodeValue nv, XSDDatatype castType)
        {
            // Plain cast.
            if ( nv.isString() ||  NodeUtils.hasLang(nv.asNode()) ) 
                return super.cast(s, nv, castType) ;
            return XSDFuncOp.dateTimeCast(nv, castType) ;
        }
    }
}
