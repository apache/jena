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

package com.hp.hpl.jena.sparql.graph;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.algebra.table.TableUnit ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.ExprAggregator ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.path.Path ;

class NodeTransformOp extends TransformCopy
{
    // This finds everywhere that node can lurk in an algebra expression:
    //   BGPs, paths, triples, quads
    //   GRAPH, GRAPH{} (DatasetNames)
    //   Filters, including inside EXISTS and expressions in LeftJoin
    //   OrderBy, GroupBy
    //   Extend, Assign
    //   Tables
    //   Projects
    // Not:
    //   Conditional (no expression)
    
    private final NodeTransform transform ;
    NodeTransformOp(NodeTransform transform)
    {
        this.transform = transform ;
    }

    @Override public Op transform(OpTriple opTriple)
    {
        Triple t2 = NodeTransformLib.transform(transform, opTriple.getTriple()) ;
        if ( t2 == opTriple.getTriple())
            return super.transform(opTriple) ;
        return new OpTriple(t2) ;
    }
    
    @Override public Op transform(OpFilter opFilter, Op subOp)
    { 
        ExprList exprList = opFilter.getExprs() ;
        ExprList exprList2 = NodeTransformLib.transform(transform, exprList) ;
        if ( exprList2 == exprList )
            return super.transform(opFilter, subOp) ;
        return OpFilter.filter(exprList2, subOp) ;
    }        
    
    @Override public Op transform(OpBGP opBGP)
    { 
        BasicPattern bgp2 = NodeTransformLib.transform(transform, opBGP.getPattern()) ;
        if ( bgp2 == opBGP.getPattern())
            return super.transform(opBGP) ;
        return new OpBGP(bgp2) ;
    }
    
    @Override public Op transform(OpPath opPath)
    { 
        TriplePath tp = opPath.getTriplePath() ;
        Node s = tp.getSubject() ;
        Node s1 = transform.convert(s) ;
        Node o = tp.getObject() ;
        Node o1 = transform.convert(o) ;
        
        if ( s1 == s && o1 == o )
            // No change.
            return super.transform(opPath) ;
        
        Path path = tp.getPath() ;
        TriplePath tp2 ;

        if ( path != null )
            tp2 = new TriplePath(s1, path, o1) ;
        else
        {
            Triple t = new Triple(s1, tp.getPredicate(), o1) ;
            tp2 = new TriplePath(t) ;
        }
        return new OpPath(tp2) ;
    }
    
    @Override public Op transform(OpQuadPattern opQuadPattern)
    { 
        // The internal representation is (graph, BGP)
        BasicPattern bgp2 = NodeTransformLib.transform(transform, opQuadPattern.getBasicPattern()) ;
        Node g2 = opQuadPattern.getGraphNode() ;
        g2 = transform.convert(g2) ;
        
        if ( g2 == opQuadPattern.getGraphNode() && bgp2 == opQuadPattern.getBasicPattern() )
            return super.transform(opQuadPattern) ;
        return new OpQuadPattern(g2, bgp2) ;
    }
    
    @Override public Op transform(OpGraph opGraph, Op subOp)
    {
        Node g2 = transform.convert(opGraph.getNode()) ;
        if ( g2 == opGraph.getNode() )
            return super.transform(opGraph, subOp) ;
        return new OpGraph(g2, subOp) ;
    }
    
    @Override public Op transform(OpDatasetNames opDatasetNames)
    {
        Node g2 = transform.convert(opDatasetNames.getGraphNode()) ;
        if ( g2 == opDatasetNames.getGraphNode() )
            return super.transform(opDatasetNames) ;
        return new OpDatasetNames(g2) ;
    }
    
    @Override public Op transform(OpTable opTable)
    {
        if ( opTable.isJoinIdentity() )
            return opTable ;
        Table table = opTable.getTable() ;
        if ( table.isEmpty() )
            return opTable ;
        if ( TableUnit.isTableUnit(table) )
            return opTable ;
        if ( table.getVars().size() == 0 )
            return opTable ;
        Table table2 = NodeTransformLib.transform(table, transform) ;
        return OpTable.create(table2) ; 
    }
    
    @Override public Op transform(OpLeftJoin opLeftJoin, Op left, Op right)
    {
        ExprList exprList = opLeftJoin.getExprs() ;
        ExprList exprList2 = exprList ;
        if ( exprList != null )
            exprList2 = NodeTransformLib.transform(transform, exprList) ;
        if ( exprList2 == exprList )
            return super.transform(opLeftJoin, left, right) ;
        return OpLeftJoin.create(left, right, exprList2) ;
    }
    
    // Not OpConditional - no expression.
    
    @Override public Op transform(OpProject opProject, Op subOp)
    { 
        List<Var> x = opProject.getVars() ;
        List<Var> x2 = NodeTransformLib.transformVars(transform, x) ;
        if ( x == x2 )
            return super.transform(opProject, subOp) ;
        return new OpProject(subOp, x2) ; 
    }
    
    @Override public Op transform(OpAssign opAssign, Op subOp)
    { 
        VarExprList varExprList = opAssign.getVarExprList() ;
        VarExprList varExprList2 = NodeTransformLib.transform(transform, varExprList) ;
        if ( varExprList == varExprList2 )
            return super.transform(opAssign, subOp) ;
        return OpAssign.assign(subOp, varExprList2) ;
    }
    
    @Override public Op transform(OpExtend opExtend, Op subOp)
    { 
        VarExprList varExprList = opExtend.getVarExprList() ;
        VarExprList varExprList2 = NodeTransformLib.transform(transform, varExprList) ;
        if ( varExprList == varExprList2 )
            return super.transform(opExtend, subOp) ;
        return OpExtend.create(subOp, varExprList2) ;
    }
    
    @Override public Op transform(OpOrder opOrder, Op subOp)
    {
        List<SortCondition> conditions = NodeTransformLib.transform(transform, opOrder.getConditions()) ;
        
        if ( conditions == opOrder.getConditions() )
            return super.transform(opOrder, subOp) ;
        return new OpOrder(subOp, conditions) ;
    }
    
    @Override public Op transform(OpGroup opGroup, Op subOp)
    {
        VarExprList groupVars = NodeTransformLib.transform(transform, opGroup.getGroupVars()) ;
        // Rename the vars in the expression as well.
        // .e.g max(?y) ==> max(?/y)  
        // These need renaming as well.
        List<ExprAggregator> aggregators = new ArrayList<>() ;
        for ( ExprAggregator agg : opGroup.getAggregators() )
            aggregators.add(agg.applyNodeTransform(transform)) ;
        
        if ( aggregators.equals(opGroup.getAggregators())) 
        {
            if ( groupVars == opGroup.getGroupVars() )
                return super.transform(opGroup, subOp) ;
        }
        
        return new OpGroup(subOp, groupVars, aggregators) ;
    }    
}
