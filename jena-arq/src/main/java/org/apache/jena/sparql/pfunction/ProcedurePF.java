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

package org.apache.jena.sparql.pfunction;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.procedure.Procedure ;
import org.apache.jena.sparql.serializer.SerializationContext ;
import org.apache.jena.sparql.util.FmtUtils ;
import org.apache.jena.sparql.util.PrintSerializableBase ;

/** Adapter between property functions and server procedures
 *  When called, this wrapper reconstructs the usual property function calling conventions. */ 
public class ProcedurePF extends PrintSerializableBase implements Procedure
{
    private PropertyFunction propFunc ;
    private PropFuncArg subjArg ;
    private PropFuncArg objArg ;
    private Node pfNode ;

    public ProcedurePF(PropertyFunction propFunc, PropFuncArg subjArg, Node pfNode, PropFuncArg objArg)
    {
        this.propFunc = propFunc ;
        this.subjArg = subjArg ;
        this.pfNode = pfNode ;
        this.objArg = objArg ;
    }
    
    // Procedure interface
 
    @Override
    public QueryIterator proc(QueryIterator input, ExecutionContext execCxt)
    {
        return propFunc.exec(input, subjArg, pfNode, objArg, execCxt) ;
    }

    @Override
    public void build(Node procId, ExprList args, ExecutionContext execCxt)
    {}

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        out.print("ProcedurePF ["+FmtUtils.stringForNode(pfNode, sCxt)+"]") ;
        out.print("[") ;
        subjArg.output(out, sCxt) ;
        out.print("][") ;
        objArg.output(out, sCxt) ;
        out.print("]") ;
        out.println() ;
    }

}
