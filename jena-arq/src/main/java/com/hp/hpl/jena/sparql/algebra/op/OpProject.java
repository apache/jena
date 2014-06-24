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

package com.hp.hpl.jena.sparql.algebra.op;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.lib.Lib ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class OpProject extends OpModifier
{
    private List<Var> vars = new ArrayList<>() ;

    public OpProject(Op subOp, List<Var> vars)
    {
        super(subOp) ;
        this.vars = vars ;
    }
    
    public List<Var> getVars() { return vars ; }
    
    @Override
    public String getName() { return Tags.tagProject ; }
    @Override
    public void visit(OpVisitor opVisitor)  { opVisitor.visit(this) ; }
    @Override
    public Op1 copy(Op subOp)                { return new OpProject(subOp, vars) ; }

    @Override
    public Op apply(Transform transform, Op subOp)
    { return transform.transform(this, subOp) ; }

    @Override
    public int hashCode()
    {
        return vars.hashCode() ^ getSubOp().hashCode() ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! (other instanceof OpProject) ) return false ;
        OpProject opProject = (OpProject)other ;
        if ( ! Lib.equal(vars, opProject.vars ) )
            return false ;
        return getSubOp().equalTo(opProject.getSubOp(), labelMap) ;
    }
}
