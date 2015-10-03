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

package org.apache.jena.sparql.modify.request;

import org.apache.jena.graph.Node ;

public class UpdateClear extends UpdateDropClear
{
    public UpdateClear(String iri, boolean silent)      { super(iri, silent) ; }
    public UpdateClear(Target target, boolean silent)   { super(target, silent) ; }
    public UpdateClear(Node target, boolean silent)     { super(target, silent) ; }
    
    public UpdateClear(String iri)                      { super(iri, false) ; }
    public UpdateClear(Target target)                   { super(target, false) ; }
    public UpdateClear(Node target)                     { super(target, false) ; }
    
    @Override
    public void visit(UpdateVisitor visitor)
    { visitor.visit(this) ; }
}
