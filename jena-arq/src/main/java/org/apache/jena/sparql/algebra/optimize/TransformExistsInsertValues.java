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

import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.Table ;
import org.apache.jena.sparql.algebra.TableFactory ;
import org.apache.jena.sparql.algebra.TransformCopy ;
import org.apache.jena.sparql.algebra.op.OpBGP ;
import org.apache.jena.sparql.algebra.op.OpJoin ;
import org.apache.jena.sparql.algebra.op.OpTable ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.function.FunctionEnv ;

public class TransformExistsInsertValues extends TransformCopy {
    
    // ** Scoping issues.  Does it "work" from the renaming? 
    // (introduces suprious out of scope values)
    
    private Binding binding ;
    private OpTable values ;

    public TransformExistsInsertValues(Binding binding, FunctionEnv env) {
        this.binding = binding ;
        this.values = bindingToValues(binding) ;
    }
    
    /*package*/ static OpTable bindingToValues(Binding binding) {
        Table table = TableFactory.create() ;
        table.addBinding(binding);
        OpTable x = OpTable.create(table) ;
        return x ;
    }

    @Override
    public Op transform(OpBGP opBGP) {
        return OpJoin.create(opBGP, values) ;
    }
    
    // The result of an empty BGP.
    @Override
    public Op transform(OpTable table) {
        if ( ! table.isJoinIdentity() )
            return super.transform(table) ;
        return values ;
    }
}
