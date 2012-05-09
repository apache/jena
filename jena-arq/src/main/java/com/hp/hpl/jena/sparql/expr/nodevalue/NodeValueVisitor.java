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

package com.hp.hpl.jena.sparql.expr.nodevalue;

public interface NodeValueVisitor
{
    public void visit(NodeValueBoolean nv) ;
//    public void visit(NodeValueDate nv) ;
//    public void visit(NodeValueDateTime nv) ;
    public void visit(NodeValueDecimal nv) ;
    public void visit(NodeValueDouble nv) ;
    public void visit(NodeValueFloat nv) ;
    public void visit(NodeValueInteger nv) ;
    public void visit(NodeValueNode nv) ;
    public void visit(NodeValueString nv) ;
    public void visit(NodeValueDT nv) ;
//    public void visit(NodeValueTime nv) ;
	public void visit(NodeValueDuration nodeValueDuration);

//	public void visit(NodeValueGYear nv) ;
//    public void visit(NodeValueGYearMonth nv) ;
//    public void visit(NodeValueGMonth nv) ;
//    public void visit(NodeValueGMonthDay nv) ;
//    public void visit(NodeValueGDay nv) ;
}
