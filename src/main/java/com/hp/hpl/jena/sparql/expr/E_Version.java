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

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.sse.Tags ;

public class E_Version extends ExprFunction0
{
    static private String fName = Tags.tagVersion ;
    
    public E_Version()
    {
        super(fName) ;
    }

    @Override
    public Expr copy()
    {
        return new E_Version() ;
    }

    @Override
    public NodeValue eval(FunctionEnv env)
    {
        return NodeValue.makeString(ARQ.NAME+" "+ARQ.VERSION) ;
    }
}
