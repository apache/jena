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

package org.apache.jena.sparql.engine.ref;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.query.ResultSetFormatter ;
import org.apache.jena.query.SortCondition ;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.JoinType;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.algebra.table.TableN ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.TriplePath ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.ResultSetStream ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.* ;
import org.apache.jena.sparql.engine.main.QC ;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.procedure.ProcEval ;
import org.apache.jena.sparql.procedure.Procedure ;

public class EvaluatorSimple implements Evaluator
{
    // Simple, slow, correct

    private ExecutionContext execCxt ;
    public static boolean debug = false ;

    public EvaluatorSimple(ExecutionContext context)
    {
        this.execCxt = context ;
    }

    @Override
    public ExecutionContext getExecContext()
    { return execCxt ; }

    @Override
    public Table basicPattern(BasicPattern pattern)
    {
        QueryIterator qIter = QC.executeDirect(pattern, QueryIterRoot.create(execCxt), execCxt) ;
        return TableFactory.create(qIter) ;
    }

    @Override
    public Table pathPattern(TriplePath triplePath)
    {
        // Shudder - this may well be expensive, but this is the simple evaluator, written for correctness.
        QueryIterator qIter = new QueryIterPath(triplePath,
                                                QueryIterRoot.create(execCxt),
                                                execCxt) ;
        return TableFactory.create(qIter) ;
    }

    @Override
    public Table procedure(Table table, Node procId, ExprList args)
    {
        Procedure proc = ProcEval.build(procId, args, execCxt) ;
        QueryIterator qIter = ProcEval.eval(table.iterator(execCxt), proc, execCxt) ;
        return TableFactory.create(qIter) ;
    }

    @Override
    public Table propertyFunction(Table table, Node procId, PropFuncArg subjArgs, PropFuncArg objArgs)
    {
        Procedure proc = ProcEval.build(procId, subjArgs, objArgs, execCxt) ;
        QueryIterator qIter = ProcEval.eval(table.iterator(execCxt), proc, execCxt) ;
        return TableFactory.create(qIter) ;
    }

    @Override
    public Table join(Table tableLeft, Table tableRight)
    {
        if ( debug )
        {
            System.out.println("Join") ;
            dump(tableLeft) ;
            dump(tableRight) ;
        }
        return joinWorker(tableLeft, tableRight, false, null) ;
    }

    @Override
    public Table leftJoin(Table tableLeft, Table tableRight, ExprList exprs)
    {
        if ( debug )
        {
            System.out.println("Left Join") ;
            dump(tableLeft) ;
            dump(tableRight) ;
            if ( exprs != null )
                System.out.println(exprs) ;
        }

        return joinWorker(tableLeft, tableRight, true, exprs) ;
    }

    @Override
    public Table diff(Table tableLeft, Table tableRight)
    {
        if ( debug )
        {
            System.out.println("Diff") ;
            dump(tableLeft) ;
            dump(tableRight) ;
        }
        return diffWorker(tableLeft, tableRight) ;
    }

    @Override
    public Table minus(Table tableLeft, Table tableRight)
    {
        if ( debug )
        {
            System.out.println("Minus") ;
            dump(tableLeft) ;
            dump(tableRight) ;
        }

        return minusWorker(tableLeft, tableRight) ;
    }

    @Override
    public Table filter(ExprList expressions, Table table)
    {
        if ( debug )
        {
            System.out.println("Restriction") ;
            System.out.println(expressions) ;
            dump(table) ;
        }
        QueryIterator iter = table.iterator(execCxt) ;
        List<Binding> output = new ArrayList<>() ;
        for ( ; iter.hasNext() ; )
        {
            Binding b = iter.nextBinding() ;
            if ( expressions.isSatisfied(b, execCxt) )
                output.add(b) ;
        }
        return new TableN(QueryIterPlainWrapper.create(output.iterator(), execCxt)) ;
    }



    @Override
    public Table union(Table tableLeft, Table tableRight)
    {
        if ( debug )
        {
            System.out.println("Union") ;
            dump(tableLeft) ;
            dump(tableRight) ;
        }
        QueryIterConcat output = new QueryIterConcat(execCxt) ;
        output.add(tableLeft.iterator(execCxt)) ;
        output.add(tableRight.iterator(execCxt)) ;
        return new TableN(output) ;
    }

    @Override
    public Table condition(Table left, Table right)
    {
        if ( left.isEmpty() )
        {
            left.close();
            return right ;
        }
        right.close();
        return left ;
    }

    @Override
    public Table list(Table table) { return table ; }

    @Override
    public Table order(Table table, List<SortCondition> conditions)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterSort(qIter, conditions, getExecContext()) ;
        return new TableN(qIter) ;
    }

    @Override
    public Table groupBy(Table table, VarExprList groupVars, List<ExprAggregator> aggregators)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterGroup(qIter, groupVars, aggregators, getExecContext()) ;
        return new TableN(qIter) ;
    }

    @Override
    public Table project(Table table, List<Var> projectVars)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterProject(qIter, projectVars, getExecContext()) ;
        return new TableN(qIter) ;
    }

    @Override
    public Table reduced(Table table)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterReduced(qIter, getExecContext()) ;
        return new TableN(qIter) ;
    }

    @Override
    public Table distinct(Table table)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        // Use the simple-and-clear implementation of DISTINCT.
        qIter = new QueryIterDistinctMem(qIter, getExecContext()) ;
        return new TableN(qIter) ;
    }

    @Override
    public Table slice(Table table, long start, long length)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterSlice(qIter, start, length, getExecContext()) ;
        return new TableN(qIter) ;
    }

    @Override
    public Table assign(Table table, VarExprList exprs)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterAssign(qIter, exprs, getExecContext(), false) ;
        return new TableN(qIter) ;
    }

    @Override
    public Table extend(Table table, VarExprList exprs)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterAssign(qIter, exprs, getExecContext(), true) ;
        return new TableN(qIter) ;
    }

    @Override
    public Table unfold(Table table, Expr expr, Var var1, Var var2)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterUnfold(qIter, expr, var1, var2, getExecContext()) ;
        return new TableN(qIter) ;
    }

    @Override
    public Table unit()
    {
        return TableFactory.createUnit() ;
    }

    private Table joinWorker(Table tableLeft, Table tableRight, boolean leftJoin, ExprList conditions)
    {
        QueryIterator left = tableLeft.iterator(execCxt) ;
        JoinType joinType = (leftJoin? JoinType.LEFT : JoinType.INNER ) ;
        QueryIterator qIter = TableJoin.joinWorker(left, tableRight, joinType, conditions, execCxt) ;
        tableLeft.close() ;
        tableRight.close() ;
        // qIter and left should be properly closed by use or called code.
        return new TableN(qIter) ;
    }

    // @@ Abstract compatibility
    private Table diffWorker(Table tableLeft, Table tableRight)
    {
        QueryIterator left = tableLeft.iterator(execCxt) ;
        TableN r = new TableN() ;
        for ( ; left.hasNext() ; )
        {
            Binding b = left.nextBinding() ;
            if ( tableRight.contains(b) )
                r.addBinding(b) ;
        }
        tableLeft.close() ;
        tableRight.close() ;
        return r ;
    }

    private Table minusWorker(Table tableLeft, Table tableRight)
    {
        // Minus(Ω1, Ω2) = { μ | μ in Ω1 such that for all μ' in Ω2, either μ and μ' are not compatible or dom(μ) and dom(μ') are disjoint }

        TableN results = new TableN() ;
        QueryIterator iterLeft = tableLeft.iterator(execCxt) ;
        for ( ; iterLeft.hasNext() ; )
        {
            Binding bindingLeft = iterLeft.nextBinding() ;
            boolean includeThisRow = true ;
            // Find a reason not to include the row.
            // That's is not disjoint and not compatible.

            QueryIterator iterRight = tableRight.iterator(execCxt) ;
            for ( ; iterRight.hasNext() ; )
            {
                Binding bindingRight = iterRight.nextBinding() ;
                if ( Algebra.disjoint(bindingLeft, bindingRight) )
                    // Disjoint - not a reason to exclude
                    continue ;

                if ( ! Algebra.compatible(bindingLeft, bindingRight) )
                    // Compatible - not a reason to exclude.
                    continue ;

                includeThisRow = false ;
                break ;

            }
            iterRight.close();
            if ( includeThisRow )
                results.addBinding(bindingLeft) ;
        }

        iterLeft.close();
        return results ;
    }

    private static void dump(Table table)
    {
        System.out.println("Table: "+Lib.className(table)) ;
        QueryIterator qIter = table.iterator(null) ;
        ResultSet rs = ResultSetStream.create(table.getVarNames(), null, table.iterator(null)) ;
        ResultSetFormatter.out(rs) ;
    }
}
