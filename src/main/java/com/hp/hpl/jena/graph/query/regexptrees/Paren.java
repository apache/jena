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
     Class which represents parenthesised regular expressions. Any parenthesised
     expression may have a non-zero label, meaning that it may be referred back
     to by BakReference expressions.
     
     @author hedgehog
*/
public class Paren extends RegexpTree
    {
    protected RegexpTree operand;
    protected int index;

    public Paren( RegexpTree tree ) 
        { this( tree, 0 ); }
    
    public Paren( RegexpTree tree, int index ) 
        { this.operand = tree; this.index = index; }
    
    public RegexpTree getOperand()
        { return operand; }
    
    public int getIndex()
        { return index; }

    @Override
    public boolean equals( Object other )
        { return other instanceof Paren && operand.equals( ((Paren) other).operand ); }

    @Override
    public int hashCode()
        { return operand.hashCode(); }

    @Override
    public String toString()
        { return "(" + operand + ")"; }
    }
