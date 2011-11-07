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

package com.hp.hpl.jena.sdb.compiler;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.Op1 ;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct ;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder ;
import com.hp.hpl.jena.sparql.algebra.op.OpProject ;
import com.hp.hpl.jena.sparql.algebra.op.OpReduced ;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice ;

// These have moved into ARQ in later SDB versions
public class OpLibSDB
{
    public static Op sub(Op1 op) { return op==null ? null : op.getSubOp() ; }

    public static boolean isProject(Op op) { return op instanceof OpProject ; }

    public static OpProject asProject(Op op)
    {  return isProject(op) ? (OpProject)op : null ; }

    public static boolean isDistinct(Op op) { return op instanceof OpDistinct ; }

    public static OpDistinct asDistinct(Op op)
    {  return isDistinct(op) ? (OpDistinct)op : null ; }

    public static boolean isReduced(Op op) { return op instanceof OpReduced ; }

    public static OpReduced asReduced(Op op)
    {  return isReduced(op) ? (OpReduced)op : null ; }

    public static boolean isOrder(Op op) { return op instanceof OpOrder ; }

    public static OpOrder asOrder(Op op)
    {  return isOrder(op) ? (OpOrder)op : null ; }

    public static boolean isSlice(Op op) { return op instanceof OpSlice ; }

    public static OpSlice asSlice(Op op)
    {  return isSlice(op) ? (OpSlice)op : null ; }

}
