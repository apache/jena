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

import static org.apache.jena.atlas.iterator.Iter.filter ;
import static org.apache.jena.atlas.iterator.Iter.map ;
import static org.apache.jena.atlas.iterator.Iter.toSet ;
import static org.apache.jena.atlas.lib.SetUtils.intersection ;

import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.iterator.Iter ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sdb.core.AliasesSql ;
import com.hp.hpl.jena.sdb.core.SDBRequest ;
import com.hp.hpl.jena.sdb.core.ScopeEntry ;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn ;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode ;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock ;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable ;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads ;
import com.hp.hpl.jena.sdb.shared.SDBInternalError ;
import com.hp.hpl.jena.sdb.store.SQLBridge ;
import com.hp.hpl.jena.sdb.store.SQLBridgeFactory ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpDatasetNames ;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpProject ;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern ;
import com.hp.hpl.jena.sparql.algebra.op.OpService ;
import com.hp.hpl.jena.sparql.algebra.op.OpTable ;
import com.hp.hpl.jena.sparql.core.Var ;

public class TransformSDB extends TransformCopy
{
    private static Logger log = LoggerFactory.getLogger(TransformSDB.class) ;
    private SDBRequest request ;
    private QuadBlockCompiler quadBlockCompiler ;
    
    public TransformSDB(SDBRequest request, QuadBlockCompiler quadBlockCompiler) 
    {
        this.request = request ;
        this.quadBlockCompiler = quadBlockCompiler ;
    }
    
    @Override
    public Op transform(OpBGP opBGP)
    { return opBGP ; }

    @Override
    public Op transform(OpQuadPattern quadPattern)
    {
        QuadBlock qBlk = new QuadBlock(quadPattern) ;
        SqlNode node = quadBlockCompiler.compile(qBlk) ;
        return new OpSQL(node, quadPattern, request) ; 
    }

    @Override
    public Op transform(OpJoin opJoin, Op left, Op right)
    {
        // TEMPORARY FIX
        // Scoping problems.
        if ( true )
            return super.transform(opJoin, left, right) ;
        
        if ( ! SDB_QC.isOpSQL(left) || ! SDB_QC.isOpSQL(right) )
            return super.transform(opJoin, left, right) ;
        SqlNode sqlLeft = ((OpSQL)left).getSqlNode() ;
        SqlNode sqlRight = ((OpSQL)right).getSqlNode() ;
        
        // This is wrong.  If right is more than single triple pattern,
        // the generated SQL wil attempt to use NodeTable lookups from the
        // LHS but they are out of scope.
        
        return new OpSQL(SqlBuilder.innerJoin(request, sqlLeft, sqlRight), opJoin, request) ;
    }

    @Override
    public Op transform(OpLeftJoin opJoin, Op left, Op right)
    {
        if ( ! request.LeftJoinTranslation )
            return super.transform(opJoin, left, right) ;
        
        if ( ! SDB_QC.isOpSQL(left) || ! SDB_QC.isOpSQL(right) )
            return super.transform(opJoin, left, right) ;

        // Condition(s) in the left join.  Punt for now. 
        if ( opJoin.getExprs() != null )
            return super.transform(opJoin, left, right) ;
        
        SqlNode sqlLeft = ((OpSQL)left).getSqlNode() ;
        SqlNode sqlRight = ((OpSQL)right).getSqlNode() ;
        
        // Check for coalesce.
        // Do optional variables on the right appear only as optional variables on the left?

        Set<ScopeEntry> scopes = sqlLeft.getIdScope().findScopes() ;
        
        // Find optional-on-left
        Set<ScopeEntry> scopes2 = toSet(filter(scopes, ScopeEntry.OptionalFilter)) ;
        Set<Var> leftOptVars = toSet(map(scopes2, ScopeEntry.ToVar)) ;              // Vars from left optionals.
        
        if ( false )
        {
            Iter<ScopeEntry> iter = Iter.iter(scopes) ;
            Set<Var> leftOptVars_ = iter.filter(ScopeEntry.OptionalFilter).map(ScopeEntry.ToVar).toSet() ;
        }
        
        // Find optional-on-right (easier - it's all variables) 
        Set<Var> rightOptVars = sqlRight.getIdScope().getVars() ;
        
        // And finally, calculate the intersection of the two.
        // SetUtils extension - one side could be an iterator  
        Set<Var> coalesceVars = intersection(leftOptVars, rightOptVars) ;
        
        // Future simplification : LeftJoinClassifier.nonLinearVars 
//        if ( ! coalesceVars.equals(LeftJoinClassifier.nonLinearVars( opJoin.getLeft(), opJoin.getRight() )) )
//        { unexpected }
        
        if ( coalesceVars.size() > 0  ) 
        {
            String alias = request.genId(AliasesSql.CoalesceAliasBase) ;
            SqlNode sqlNode = SqlBuilder.leftJoinCoalesce(request, alias,
                                                  sqlLeft, sqlRight, 
                                                  coalesceVars) ;
            return new OpSQL(sqlNode, opJoin, request) ;
            
            // Punt
            //return super.transform(opJoin, left, right) ;
        }
        return new OpSQL(SqlBuilder.leftJoin(request, sqlLeft, sqlRight, null), opJoin, request) ;
    }
    
    @Override
    public Op transform(OpFilter opFilter, Op op)
    {
        // Can't really do much here because we are working in node id space, and the lexicial forms etc
        // of nodes aren't available until the bridge is added.
        // See QueryCompilerMain.
        return super.transform(opFilter, op) ;
    }
    
    @Override
    public Op transform(OpTable opTable)
    {
        if ( ! opTable.isJoinIdentity())
            log.error("OpTable : Not join identity") ;
        //return new OpSQL(null, opUnit, request) ;
        return super.transform(opTable) ;
    }
    
    // Modifiers: the structure is:
    //    slice
    //      distinct/reduced
    //        project
    //          order
    //            having
    //              group
    //                [toList]

    // modifier : having
    // modifier : group
  
    // ---- Modifiers
    
    @Override
    public Op transform(OpDistinct opDistinct, Op subOp)
    { 
        if ( ! SDB_QC.isOpSQL(subOp) )
            return super.transform(opDistinct, subOp) ;
        // Derby does not support DISTINCT on CLOBS
        if ( ! request.DistinctTranslation )
            return super.transform(opDistinct, subOp) ;
        
        OpSQL opSubSQL = (OpSQL)subOp ;
        SqlNode sqlSubOp = opSubSQL.getSqlNode() ;
        SqlNode n = SqlSelectBlock.distinct(request, sqlSubOp) ;
        OpSQL opSQL = new OpSQL(n, opDistinct, request) ;
        // Pull up bridge, if any
        opSQL.setBridge(opSubSQL.getBridge()) ;
        return opSQL ;
    }
    
    
    @Override
    public Op transform(OpProject opProject, Op subOp)
    { 
        //request.getStore().getSQLBridgeFactory().create(request, null, null)
        if ( ! SDB_QC.isOpSQL(subOp) )
            return super.transform(opProject, subOp) ;
        
        // Need to not do bridge elsewhere.
        List<Var> vars = opProject.getVars() ;
        return doBridge(request, (OpSQL)subOp, vars, opProject) ;
    }
    
    @Override
    public Op transform(OpService opService, Op subOp)
    {
        // Do not walk in any further.
        // See ARQ Optimize class for a better way to do this.
        return opService ;
    }
    
    // See QueryCompilerMain.SqlNodesFinisher.visit(OpExt op)
    // Be careful about being done twice.
    // XXX SHARE CODE!
    static private OpSQL doBridge(SDBRequest request, OpSQL opSQL, List<Var> projectVars, Op original)
    {
        SqlNode sqlNode = opSQL.getSqlNode() ;
        SQLBridgeFactory f = request.getStore().getSQLBridgeFactory() ;
        SQLBridge bridge = f.create(request, sqlNode, projectVars) ;
        bridge.build();
        sqlNode = bridge.getSqlNode() ;
        opSQL = new OpSQL(sqlNode, original, request) ; 
        opSQL.setBridge(bridge) ;
        opSQL.resetSqlNode(sqlNode) ;   // New is better?
        return opSQL ;
    }
    
    // Now done in QueryCompilerMain at a later stage.
//    @Override
//    public Op transform(OpSlice opSlice, Op subOp)
//    {
//        if ( ! request.LimitOffsetTranslation )
//            return super.transform(opSlice, subOp) ;
//
//        // Not a slice of SQL
//        if ( ! SDB_QC.isOpSQL(subOp) )
//            return super.transform(opSlice, subOp) ;
//        
//        // Two cases are currently handled:
//        // (slice (project (sql expression)))
//        // (slice (sql expression))
//
//        if ( isProject(opSlice.getSubOp()) )
//            // This should not happen because the pre-transform done in QueryEngineMain
//            // rewrites the case into the equivalent (project (slice ...))
//            return transformSliceProject(opSlice, (OpSQL)subOp, ((OpSQL)subOp).getSqlNode()) ;
//
//        // (slice (sql expression))
//        return transformSlice(opSlice, ((OpSQL)subOp).getSqlNode()) ;
//    }
//        
//    private Op transformSlice(OpSlice opSlice, SqlNode sqlSubOp)
//    {
//        SqlNode n = SqlSelectBlock.slice(request, sqlSubOp, opSlice.getStart(), opSlice.getLength()) ;
//        OpSQL x = new OpSQL(n, opSlice, request) ;
//        return x ;
//    }
//    
//    private Op transformSliceProject(OpSlice opSlice, OpSQL subOp, SqlNode sqlOp)
//    {
//        // This put the LIMIT outside all the projects left joins, which is a bit of a pity.
//        // Could improve by rewriting (slice (project...)) into (project (slice...)) in QueryCompilerMain 
//        OpSQL x = (OpSQL)transformSlice(opSlice, sqlOp) ;
//        SQLBridge bridge = subOp.getBridge() ;
//        return x ;
//    }

//    // ----
//    
//    private boolean translateConstraints = true ;
//    
//    private SDBConstraint transformFilter(OpFilter opFilter)
//    {
//        if ( ! translateConstraints )
//            return null ;
//        
//        ExprList exprs = opFilter.getExprs() ;
//        List<Expr> x = exprs.getList() ;
//        for ( Expr  expr : x )
//        {
//            ConditionCompiler cc = new RegexCompiler() ;
//            SDBConstraint psc = cc.recognize(expr) ;
//            if ( psc != null )
//                return psc ; 
//        }
//        return null ;
//    }
//
//    private Set<Var> getVarsInFilter(Expr expr)
//    {
//        Set<Var> vars = expr.getVarsMentioned() ;
//        return vars ;
//    }
    
    @Override
    public Op transform(OpDatasetNames opDatasetNames)
    {
        if ( false )
            return super.transform(opDatasetNames) ;
        
        // Basically, an implementation of "GRAPH ?g {}" 
        Node g  = opDatasetNames.getGraphNode() ;
        if ( ! Var.isVar(g) )
            throw new SDBInternalError("OpDatasetNames - not a variable: "+g) ;
        Var v = Var.alloc(g) ;

        // Inner SELECT SQL: (SELECT DISTINCT g FROM Quads)
        TableDescQuads quads = request.getStore().getQuadTableDesc() ;
        SqlTable sqlTableQ = new SqlTable(quads.getTableName()) ;
        sqlTableQ.setIdColumnForVar(v, new SqlColumn(sqlTableQ, quads.getGraphColName())) ;
        SqlNode sqlNodeQ = SqlSelectBlock.distinct(request, sqlTableQ) ;
        
        // Will have the value left join added later. 
        return new OpSQL(sqlNodeQ, opDatasetNames, request) ;
    }
}
