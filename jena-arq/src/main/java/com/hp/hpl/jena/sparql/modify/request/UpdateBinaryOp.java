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

package com.hp.hpl.jena.sparql.modify.request;

import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;
import com.hp.hpl.jena.update.Update ;

public abstract class UpdateBinaryOp extends Update
{
    private Target src ;
    private Target dest ;
    private boolean silent ;

    protected UpdateBinaryOp(Target src, Target dest, boolean silent)
    {
        checkTarget(src) ;
        checkTarget(dest) ;
        this.src = src ; 
        this.dest = dest ;
        this.silent = silent ;
    }

    private static void checkTarget(Target target)
    {
        if ( ! target.isDefault() && ! target.isOneNamedGraph() )
            throw new ARQException("Illegal target: must identify a single graph: "+target) ; 
    }

    public Target getSrc()      { return src ; }

    public Target getDest()     { return dest ; }
    
    public boolean getSilent()  { return silent ; }
    
    @Override
    final
    public boolean equalTo(Update obj, NodeIsomorphismMap isoMap) {
        if (this == obj)
            return true ;
        if (getClass() != obj.getClass())
            return false ;
        UpdateBinaryOp other = (UpdateBinaryOp)obj ;
        if (silent != other.silent)
            return false ;

        return  dest.equalTo(other.dest, isoMap) &&
                src.equalTo(other.src, isoMap) ;
    }
}
