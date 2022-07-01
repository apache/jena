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

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.util.Context;

/** The minimal optimizer.
 * This does property functions and nested variable renaming
 * but no other transformations of the algebra expression.   
 */
public class OptimizerMinimal implements Rewrite {

    private final Context context;

    public OptimizerMinimal(Context context) {
        this.context = context;
    }

    @Override
    public Op rewrite(Op op) {
        // Property functions - convert lists to arguments, create (propfunc)
        op = TransformPropertyFunction.transform(op, context) ;
        // Rename variables that are hidden by subqueries etc.
        // so all variables can be treated globally.
        // Must always do this for QueryEngineMain.
        op = TransformScopeRename.transform(op) ;

        // test/functions/bnode01.rq 
        op = Transformer.transform(new TransformExtendCombine(), op) ;
        return op;
    }
}
