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

package org.apache.jena.sparql.algebra.optimize;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.TransformCopy ;
import org.apache.jena.sparql.algebra.op.OpBGP ;
import org.apache.jena.sparql.algebra.op.OpFilter ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.E_SameTerm ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.ExprVar ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.function.FunctionEnv ;

public class TransformExistsInsertFilters extends TransformCopy {
    
    private Binding binding ;
    private ExprList filters ;

    public TransformExistsInsertFilters(Binding binding, FunctionEnv env) {
        this.binding = binding ;
        this.filters = bindingToFilters(binding) ;
    }
    
    private ExprList bindingToFilters(Binding binding) {
        ExprList exprList = new ExprList() ;
        binding.vars().forEachRemaining((v)->{
            Node n = binding.get(v) ;
            if ( n != null )
                exprList.add(new E_SameTerm(new ExprVar(v), NodeValue.makeNode(n))) ;
        }) ;
        return exprList ;
    }

    @Override
    public Op transform(OpBGP opBGP) {
        return OpFilter.filterAlways(filters, opBGP) ;
    }
}
