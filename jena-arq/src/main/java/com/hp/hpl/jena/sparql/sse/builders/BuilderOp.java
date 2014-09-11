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

package com.hp.hpl.jena.sparql.sse.builders;

import java.util.* ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.core.* ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprAggregator ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.Tags ;

public class BuilderOp
{
    // It's easier to have a object than have all statics because the
    // order of statics matters (the dispatch table gets initialized to
    // tag/null because the buildXYZ are null at that point).
    // which forces the code structure unnaturally.
    
    private static BuilderOp builderOp = new BuilderOp() ;
    
    public static Op build(Item item)
    {
        if (item.isNode() )
            BuilderLib.broken(item, "Attempt to build op structure from a plain node") ;

        if (item.isSymbol() )
            BuilderLib.broken(item, "Attempt to build op structure from a bare symbol") ;
        
        if (!item.isTagged())
            BuilderLib.broken(item, "Attempt to build op structure from a non-tagged item") ;

        return builderOp.build(item.getList()) ;
    }

    protected Map<String, Build> dispatch = new HashMap<>() ;

    public BuilderOp()
    {
        addBuild(Tags.tagBGP,           buildBGP) ;
        addBuild(Tags.tagQuadPattern,   buildQuadPattern) ;
        addBuild(Tags.tagQuadBlock,     buildQuadBlock) ;
        addBuild(Tags.tagTriple,        buildTriple) ;
        addBuild(Tags.tagQuad,          buildQuad) ;
        addBuild(Tags.tagTriplePath,    buildTriplePath) ;
        addBuild(Tags.tagFilter,        buildFilter) ;
        addBuild(Tags.tagGraph,         buildGraph) ;
        addBuild(Tags.tagService,       buildService) ;
        addBuild(Tags.tagProc,          buildProcedure) ;
        addBuild(Tags.tagPropFunc,      buildPropertyFunction) ;
        addBuild(Tags.tagJoin,          buildJoin) ;
        addBuild(Tags.tagSequence,      buildSequence) ;
        addBuild(Tags.tagDisjunction,   buildDisjunction) ;
        addBuild(Tags.tagLeftJoin,      buildLeftJoin) ;
        addBuild(Tags.tagDiff,          buildDiff) ;
        addBuild(Tags.tagMinus,         buildMinus) ;
        addBuild(Tags.tagUnion,         buildUnion) ;
        addBuild(Tags.tagDatasetNames,  buildDatasetNames) ;
        addBuild(Tags.tagConditional,   buildConditional) ;

        addBuild(Tags.tagToList,        buildToList) ;
        addBuild(Tags.tagGroupBy,       buildGroupBy) ;
        addBuild(Tags.tagOrderBy,       buildOrderBy) ;
        addBuild(Tags.tagTopN,          buildTopN) ;
        addBuild(Tags.tagProject,       buildProject) ;
        addBuild(Tags.tagDistinct,      buildDistinct) ;
        addBuild(Tags.tagReduced,       buildReduced) ;
        addBuild(Tags.tagAssign,        buildAssign) ;
        addBuild(Tags.tagExtend,        buildExtend) ;
        addBuild(Tags.symAssign,        buildAssign) ;
        addBuild(Tags.tagSlice,         buildSlice) ;

        addBuild(Tags.tagTable,         buildTable) ;
        addBuild(Tags.tagNull,          buildNull) ;
        addBuild(Tags.tagLabel,         buildLabel) ;
    }

    
    public static void add(String tag, Build builder)
    {
        builderOp.addBuild(tag, builder) ;
    }

    public static void remove(String tag)
    {
        builderOp.removeBuild(tag) ;
    }
    
    public static boolean contains(String tag)
    {
        return builderOp.containsBuild(tag) ;
    }
    
    // The main recursive build operation.
    private Op build(ItemList list)
    {
        Item head = list.get(0) ;
        String tag = head.getSymbol() ;

        Build bob = findBuild(tag) ;
        if ( bob != null )
            return bob.make(list) ;
        else
            BuilderLib.broken(head, "Unrecognized algebra operation: "+tag) ;
        return null ;
    }

    public static BasicPattern buildBGP(Item item)
    {
        if ( ! item.isTagged(Tags.tagBGP) )
            BuilderLib.broken(item, "Not a basic graph pattern") ;
        if ( ! item.isList() )
            BuilderLib.broken(item, "Not a list for a basic graph pattern") ;
        ItemList list = item.getList() ;
        return buildBGP(list) ;
        
    }
    
    private static BasicPattern buildBGP(ItemList list)
    {
        // Skips the tag.
        BasicPattern triples = new BasicPattern() ;
        for ( int i = 1 ; i < list.size() ; i++ )
        {
            Item item = list.get(i) ;
            if ( ! item.isList() )
                BuilderLib.broken(item, "Not a triple structure") ;
            Triple t = BuilderGraph.buildTriple(item.getList()) ;
            triples.add(t) ; 
        }
        return triples ;
    }
    
    protected Op build(ItemList list, int idx)
    {
        return build(list.get(idx).getList()) ;
    }

    // <<<< ---- Coordinate these 
    // Lowercase on insertion?
    protected void addBuild(String tag, Build builder)
    {
        dispatch.put(tag, builder) ;
    }
    
    protected void removeBuild(String tag)
    {
        dispatch.remove(tag) ;
    }
    
    protected boolean containsBuild(String tag)
    {
        return findBuild(tag) != null ;
        
    }

    protected Build findBuild(String str)
    {
        for ( String key : dispatch.keySet() )
        {
            if ( str.equalsIgnoreCase( key ) )
            {
                return dispatch.get( key );
            }
        }
        return null ;
    }

    // >>>> ----
    
    static public interface Build { Op make(ItemList list) ; }

    // Not static.  The initialization through the singleton would not work
    // (static initialization order - these operations would need to go
    // before the singelton. 
    // Or assign null and create object on first call but that breaks add/remove
    final protected Build buildTable = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            Item t = Item.createList(list) ;
            Table table = BuilderTable.build(t) ; 
            return OpTable.create(table) ;
        }
    } ;

    final protected Build buildBGP = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BasicPattern triples = buildBGP(list) ;
            return new OpBGP(triples) ;
        }
    } ;

    final protected Build buildQuadPattern = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            Node g = null ;
            BasicPattern bp = new BasicPattern() ;
            for ( int i = 1 ; i < list.size() ; i++ )
            {
                Item item = list.get(i) ;
                if ( ! item.isList() )
                    BuilderLib.broken(item, "Not a quad structure") ;
                Quad q = BuilderGraph.buildQuad(item.getList()) ;
                if ( g == null )
                    g = q.getGraph() ;
                else
                {
                    if ( ! g.equals(q.getGraph()) )
                        BuilderLib.broken(item, "Quad has different graph node in quadapttern: "+q) ;
                }
                bp.add(q.asTriple()) ;
                
            }
            
            OpQuadPattern op = new OpQuadPattern(g, bp) ;
            return op ;
        }
    } ;

    final protected Build buildQuadBlock = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            Node g = null ;
            QuadPattern qp = new QuadPattern() ;
            for ( int i = 1 ; i < list.size() ; i++ )
            {
                Item item = list.get(i) ;
                if ( ! item.isList() )
                    BuilderLib.broken(item, "Not a quad structure") ;
                Quad q = BuilderGraph.buildQuad(item.getList()) ;
                qp.add(q) ;
            }
            
            OpQuadBlock op = new OpQuadBlock(qp) ;
            return op ;
        }
    } ;


    final protected Build buildTriple = new Build(){
        @Override
        public Op make(ItemList list)
        {
            Triple t = BuilderGraph.buildTriple(list) ;
            return new OpTriple(t) ;
        }} ;
    
    final protected Build buildQuad = new Build(){
        @Override
        public Op make(ItemList list)
        {
            Quad q = BuilderGraph.buildQuad(list) ;
            return new OpQuad(q) ;
        }} ;
    
    final protected Build buildTriplePath = new Build(){
        @Override
        public Op make(ItemList list)
        {
            TriplePath tp = BuilderPath.buildTriplePath(list) ;
            return new OpPath(tp) ;
        }} ;
    
    final protected Build buildFilter = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "Malformed filter") ;
            Item itemExpr = list.get(1) ;
            Item itemOp = list.get(2) ;

            Op op = build(itemOp.getList()) ;
            ExprList exprList = BuilderExpr.buildExprOrExprList(itemExpr) ;
            return OpFilter.filter(exprList, op) ;
        }
    } ;

    final protected Build buildJoin = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "Join") ;
            Op left = build(list, 1) ;
            Op right  = build(list, 2) ;
            Op op = OpJoin.create(left, right) ;
            return op ;
        }
    } ;

    // Add all the operations from the list to the OpN
    final private void addOps(OpN op, ItemList list)
    {
        for ( int i = 1 ; i < list.size() ; i++ )
        {
            Op sub = build(list, i) ;
            op.add(sub) ;
        }
    }

    final protected Build buildSequence = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLengthAtLeast(2, list, "Sequence") ;
            OpSequence op = OpSequence.create() ;
            addOps(op, list) ;
            return op ;
        }
    } ;
    
    final protected Build buildDisjunction = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLengthAtLeast(2, list, "Disjunction") ;
            OpDisjunction op = OpDisjunction.create() ;
            addOps(op, list) ;
            return op ;
        }
    } ;

    final protected Build buildLeftJoin = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, 4, list, "leftjoin: wanted 2 or 3 arguments") ;
            Op left = build(list, 1) ;
            Op right  = build(list, 2) ;
            ExprList expr = null ;
            if ( list.size() == 4 )
            {
                Item exprItem = list.get(3) ;
                // Allow empty 
                if ( exprItem.isList() && exprItem.getList().isEmpty() )
                {}
                else
                    expr = BuilderExpr.buildExprOrExprList(exprItem) ;
            }
            Op op = OpLeftJoin.create(left, right, expr) ;
            return op ;
        }
    } ;

    final protected Build buildDiff = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, 4, list, "diff: wanted 2 arguments") ;
            Op left = build(list, 1) ;
            Op right  = build(list, 2) ;
            Op op = OpDiff.create(left, right) ;
            return op ;
        }
    } ;

    final protected Build buildMinus = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, 4, list, "minus: wanted 2 arguments") ;
            Op left = build(list, 1) ;
            Op right  = build(list, 2) ;
            Op op = OpMinus.create(left, right) ;
            return op ;
        }
    } ;

    final protected Build buildUnion = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "union") ;
            Op left = build(list, 1) ;
            Op right  = build(list, 2) ;
            Op op = new OpUnion(left, right) ;
            return op ;
        }
    } ;
    
    final protected Build buildDatasetNames = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(2, list, Tags.tagDatasetNames) ;
            Node n = BuilderNode.buildNode(list.get(1)) ;
            return new OpDatasetNames(n) ;
        }
    } ;
    
    final protected Build buildConditional = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(2, 3, list, "condition") ;
            Op left = build(list, 1) ;
            // No second argument means unit.
            Op right = OpTable.unit() ;
            if ( list.size() != 2 )
                right  = build(list, 2) ;
            Op op = new OpConditional(left, right) ;
            return op ;
        }
    } ;

    final protected Build buildGraph = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "graph") ;
            Node graph = BuilderNode.buildNode(list.get(1)) ;
            Op sub  = build(list, 2) ;
            return new OpGraph(graph, sub) ;
        }
    } ;

    final protected Build buildService = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            boolean silent = false ;
            BuilderLib.checkLength(3, 4, list, "service") ;
            list = list.cdr() ;
            if ( list.size() == 3 )
            {
                if ( !list.car().isSymbol() )
                    BuilderLib.broken(list, "Expected a keyword") ;
                if ( ! list.car().getSymbol().equalsIgnoreCase("SILENT") )
                    BuilderLib.broken(list, "Service: Expected SILENT") ;
                silent = true ;
                list = list.cdr() ;
            }
            
            Node service = BuilderNode.buildNode(list.car()) ;
            if ( ! service.isURI() && ! service.isVariable() )
                BuilderLib.broken(list, "Service must provide a URI or variable") ;
            list = list.cdr() ;
            Op sub  = build(list, 0) ;
            return new OpService(service, sub, silent) ;
        }
    } ;
    
    final protected Build buildProcedure = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            // (proc <foo> (args) form)
            BuilderLib.checkLength(4, list, "proc") ;
            Node procId = BuilderNode.buildNode(list.get(1)) ;
            if ( ! procId.isURI() )
                BuilderLib.broken(list, "Procedure name must be a URI") ;
            ExprList args = BuilderExpr.buildExprOrExprList(list.get(2)) ;
            Op sub  = build(list, 3) ;
            return new OpProcedure(procId, args, sub) ;
        }

    } ;

    final protected Build buildPropertyFunction = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            // (proc <foo> (subject args) (object args) form)
            BuilderLib.checkLength(5, list, "propfunc") ;
            Node property = BuilderNode.buildNode(list.get(1)) ;
            
            if ( ! property.isURI() )
                BuilderLib.broken(list, "Property function name must be a URI") ;

            PropFuncArg subjArg = readPropFuncArg(list.get(2)) ;
            PropFuncArg objArg = readPropFuncArg(list.get(3)) ;
            Op sub  = build(list, 4) ;
            return new OpPropFunc(property, subjArg, objArg, sub) ;
        }
    } ;
    
    static final private PropFuncArg readPropFuncArg(Item item)
    {
        if ( item.isNode() )
            return new PropFuncArg(BuilderNode.buildNode(item)) ;
        if ( item.isList() )
            return new PropFuncArg(BuilderNode.buildNodeList(item)) ;
        BuilderLib.broken(item, "Expected a property function argument (node or list of nodes") ;
        return null ;
    }

    final protected Build buildToList = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "tolist") ;
            Op sub = build(list, 1) ;
            Op op = new OpList(sub) ;
            return op ;
        }
    } ;


    final protected Build buildGroupBy = new Build()
    {
        // See buildProject
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, 4, list,  "Group") ;
            // GroupBy
            VarExprList vars = BuilderExpr.buildNamedExprList(list.get(1).getList()) ;
            List<ExprAggregator> aggregators = new ArrayList<>() ;
            
            if ( list.size() == 4 )
            {
                // Aggregations : assume that the exprs are legal.
                VarExprList y = BuilderExpr.buildNamedExprList(list.get(2).getList()) ;

                // Aggregations need to know the name of the variable they are associated with
                // (so it can be set by the aggregation calculation)
                // Bind aggregation to variable
                // Remember to process in order that VarExprList keeps the variables.
                
                for ( Var aggVar : y.getVars() )
                {
                    Expr e = y.getExpr(aggVar) ;
                    if ( ! ( e instanceof ExprAggregator ) )
                        BuilderLib.broken(list, "Not a aggregate expression: "+e) ;
                    ExprAggregator eAgg = (ExprAggregator)e ;
                    eAgg.setVar(aggVar) ;
                    aggregators.add(eAgg) ;    
                }
            }
            
            Op sub = build(list, list.size()-1) ;
            Op op = new OpGroup(sub,vars, aggregators) ;
            return op ;
        }
    } ;


    final protected Build buildOrderBy = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, list,  "Order") ;
            ItemList conditions = list.get(1).getList() ;
            
            // Maybe tagged (asc, desc or a raw expression)
            List<SortCondition> x = new ArrayList<>() ;
            
            for ( int i = 0 ; i < conditions.size() ; i++ )
            {
                //int direction = Query.ORDER_DEFAULT ;
                Item item = conditions.get(i) ;
                SortCondition sc = scBuilder(item) ;
                x.add(sc) ;
            }
            Op sub = build(list, 2) ;
            Op op = new OpOrder(sub, x) ;
            return op ;
        }
    } ;

    SortCondition scBuilder(Item item)
    {
        int direction = Query.ORDER_DEFAULT ;
        if ( item.isTagged("asc") || item.isTagged("desc") )
        {
            BuilderLib.checkList(item) ;
            BuilderLib.checkLength(2, item.getList(), "Direction corrupt") ;
            if ( item.isTagged("asc") )
                direction = Query.ORDER_ASCENDING ;
            else
                direction = Query.ORDER_DESCENDING ;
            item = item.getList().get(1) ; 
        }
        Expr expr = BuilderExpr.buildExpr(item) ;
        if ( expr.isVariable() )
            return new SortCondition(expr.getExprVar().asVar(), direction) ;
        else
            return new SortCondition(expr, direction) ;
    }
    
    final protected Build buildTopN = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, list,  Tags.tagTopN) ;
            int N = BuilderNode.buildInt(list.get(1).getList(), 0, -1) ;
            ItemList conditions = list.get(1).getList().cdr() ;
            
            // Maybe tagged (asc, desc or a raw expression)
            List<SortCondition> x = new ArrayList<>() ;
            
            for ( int i = 0 ; i < conditions.size() ; i++ )
            {
                //int direction = Query.ORDER_DEFAULT ;
                Item item = conditions.get(i) ;
                SortCondition sc = scBuilder(item) ;
                x.add(sc) ;
            }
            Op sub = build(list, 2) ;
            Op op = new OpTopN(sub, N, x) ;
            return op ;
        }
    } ;

    
    final protected Build buildProject = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "project") ;
            List<Var> x = BuilderNode.buildVars(list.get(1).getList()) ; 
            Op sub = build(list, 2) ;
            return new OpProject(sub, x) ;
        }
    } ;

    
    final protected Build buildDistinct = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "distinct") ;
            Op sub = build(list, 1) ;
            return OpDistinct.create(sub) ;
        }
    } ;

    final protected Build buildReduced = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "reduced") ;
            Op sub = build(list, 1) ;
            return OpReduced.create(sub) ;
        }
    } ;

    final protected Build buildAssign = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "assign") ;
            VarExprList x = BuilderExpr.buildNamedExprOrExprList(list.get(1)) ; 
            Op sub ; 
            if ( list.size() == 2 )
                sub = OpTable.unit() ;
            else
                sub = build(list, 2) ;
            return OpAssign.create(sub, x) ;
        }
    } ;

    final protected Build buildExtend = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(2, 3, list, "extend") ;
            VarExprList x = BuilderExpr.buildNamedExprOrExprList(list.get(1)) ;
            Op sub ; 
            if ( list.size() == 2 )
                sub = OpTable.unit() ;
            else
                sub = build(list, 2) ;
            return OpExtend.create(sub, x) ;
        }
    } ;

    final protected Build buildSlice = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(4, list, "slice") ;
            long start = BuilderNode.buildLong(list, 1, -1) ;
            long length = BuilderNode.buildLong(list, 2, -1) ;

            if ( start == -1 )
                start = Query.NOLIMIT ;
            if ( length == -1 )
                length = Query.NOLIMIT ;

            Op sub = build(list, 3) ;
            return new OpSlice(sub, start, length) ;
        }
    } ;

    final protected Build buildNull = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(1, list, Tags.tagNull) ;
            return OpNull.create() ;
        }
    } ;

    final protected Build buildLabel = new Build()
    {
        @Override
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(2, 3, list, Tags.tagLabel) ;
            Item label = list.get(1) ;
            Object str = null ;
            if ( label.isSymbol() )
                str = label.getSymbol() ;
            else if ( label.isNode() )
            {
                if ( label.getNode().isLiteral() )
                {
                    if ( label.getNode().getLiteralLanguage() == null ||
                        label.getNode().getLiteralLanguage().equals("") )
                    str = label.getNode().getLiteralLexicalForm() ;
                }
                else
                    str = label.getNode() ;
            }
            else
                BuilderLib.broken("No a symbol or a node") ;
            
            if ( str == null )
                str = label.toString() ;
            
            Op op = null ;
            
            if ( list.size() == 3 )
                op = build(list, 2) ;
            return OpLabel.create(str, op) ;
//            if ( op == null )
//                return new OpLabel(str) ;
//            else
//                return new OpLabel(str , op) ;
        }
    } ;
}
