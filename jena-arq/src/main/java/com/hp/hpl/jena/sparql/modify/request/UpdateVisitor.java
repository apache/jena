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

import org.apache.jena.atlas.lib.Sink ;

import com.hp.hpl.jena.sparql.core.Quad ;

public interface UpdateVisitor
{
    public void visit(UpdateDrop update) ;
    public void visit(UpdateClear update) ;
    
    public void visit(UpdateCreate update) ;
    public void visit(UpdateLoad update) ;
    
    public void visit(UpdateAdd update) ;
    public void visit(UpdateCopy update) ;
    public void visit(UpdateMove update) ;
    
    public void visit(UpdateDataInsert update) ;
    public void visit(UpdateDataDelete update) ;
    public void visit(UpdateDeleteWhere update) ;
    public void visit(UpdateModify update) ;
    
    public Sink<Quad> createInsertDataSink();
    public Sink<Quad> createDeleteDataSink();
}
