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

package com.hp.hpl.jena.graph.query.regexptrees;

/**
    Optional - expresses a pattern which may or may not match the operand
    expression.
    
    @author kers
*/

public class Optional extends Repeated
    {
    public Optional( RegexpTree operand ) 
        { super( operand ); }
    
    @Override
    public int hashCode()
        { return operand.hashCode(); }
    
    @Override
    public boolean equals( Object x )
        { return x instanceof Optional && operand.equals( ((Optional) x).operand ); }
    
    @Override
    public String toString()
        { return "<optional " + operand.toString() + ">"; }
    }
