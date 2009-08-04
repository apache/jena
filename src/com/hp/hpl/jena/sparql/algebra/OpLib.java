/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import com.hp.hpl.jena.sparql.algebra.op.*;

// NB Operations take and return nulls for convenience.

public class OpLib
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

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */