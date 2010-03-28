/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.ref;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.algebra.TableFactory ;
import com.hp.hpl.jena.sparql.algebra.table.TableN ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.* ;
import com.hp.hpl.jena.sparql.engine.main.QC ;
import com.hp.hpl.jena.sparql.expr.E_Aggregator ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;
import com.hp.hpl.jena.sparql.procedure.ProcEval ;
import com.hp.hpl.jena.sparql.procedure.Procedure ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class EvaluatorSimple implements Evaluator
{
    // Simple, slow, correct

    private ExecutionContext execCxt ;
    public static boolean debug = false ;

    public EvaluatorSimple(ExecutionContext context)
    {
        this.execCxt = context ;
    }

    public ExecutionContext getExecContext()
    { return execCxt ; }

    public Table basicPattern(BasicPattern pattern)
    {
        QueryIterator qIter = QC.executeDirect(pattern, QueryIterRoot.create(execCxt), execCxt) ;
        return TableFactory.create(qIter) ;
    }

    public Table pathPattern(TriplePath triplePath)
    {
        // Shudder - this may well be expensive, but this is the simple evaluator, written for correctness. 
        QueryIterator qIter = new QueryIterPath(triplePath, 
                                                QueryIterRoot.create(execCxt),
                                                execCxt) ;
        return TableFactory.create(qIter) ;
    }

    public Table procedure(Table table, Node procId, ExprList args)
    {
        Procedure proc = ProcEval.build(procId, args, execCxt) ;
        QueryIterator qIter = ProcEval.eval(table.iterator(execCxt), proc, execCxt) ;
        return TableFactory.create(qIter) ;
    }
    
    public Table propertyFunction(Table table, Node procId, PropFuncArg subjArgs, PropFuncArg objArgs)
    {
        Procedure proc = ProcEval.build(procId, subjArgs, objArgs, execCxt) ;
        QueryIterator qIter = ProcEval.eval(table.iterator(execCxt), proc, execCxt) ;
        return TableFactory.create(qIter) ;
    }

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

    public Table filter(ExprList expressions, Table table)
    {
        if ( debug )
        {
            System.out.println("Restriction") ;
            System.out.println(expressions) ;
            dump(table) ;
        }
        QueryIterator iter = table.iterator(execCxt) ;
        List<Binding> output = new ArrayList<Binding>() ;
        for ( ; iter.hasNext() ; )
        {
            Binding b = iter.nextBinding() ;
            if ( expressions.isSatisfied(b, execCxt) )
                output.add(b) ;
        }
        return new TableN(new QueryIterPlainWrapper(output.iterator(), execCxt)) ;
    }



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

    public Table list(Table table) { return table ; } 

    public Table order(Table table, List<SortCondition> conditions)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterSort(qIter, conditions, getExecContext()) ;
        return new TableN(qIter) ;
    }

    public Table groupBy(Table table, VarExprList groupVars, List<E_Aggregator> aggregators)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterGroup(qIter, groupVars, aggregators, getExecContext()) ;
        return new TableN(qIter) ;
    }
    
    public Table project(Table table, List<Var> projectVars)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterProject(qIter, projectVars, getExecContext()) ;
        return new TableN(qIter) ;
    }

    public Table reduced(Table table)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterReduced(qIter, getExecContext()) ;
        return new TableN(qIter) ;
    }

    public Table distinct(Table table)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterDistinct(qIter, getExecContext()) ;
        return new TableN(qIter) ;
    }

    public Table slice(Table table, long start, long length)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterSlice(qIter, start, length, getExecContext()) ;
        return new TableN(qIter) ;
    }

    public Table assign(Table table, VarExprList exprs)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterAssign(qIter, exprs, getExecContext()) ;
        return new TableN(qIter) ;
    }

    public Table unit()
    {
        return TableFactory.createUnit() ;
    }

    private Table joinWorker(Table tableLeft, Table tableRight, boolean leftJoin, ExprList conditions)
    {
        // Conditional LeftJoin is (left, Filter(expr, Join(left, right)))
        // This is done in matchRightLeft
    
        // Have an iterator that yields one-by-one.
        QueryIterator left = tableLeft.iterator(execCxt) ;
        QueryIterConcat output = new QueryIterConcat(execCxt) ;
        for ( ; left.hasNext() ; )
        {
            Binding b = left.nextBinding() ;
            QueryIterator x = tableRight.matchRightLeft(b, leftJoin, conditions, execCxt) ;
            if ( x == null )
                continue ;
            output.add(x) ;
        }
        tableLeft.close() ;
        tableRight.close() ;
        return new TableN(output) ;
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
        System.out.println("Table: "+Utils.className(table)) ;
        QueryIterator qIter = table.iterator(null) ;
        ResultSet rs = new ResultSetStream(table.getVarNames(), null, table.iterator(null)) ;
        ResultSetFormatter.out(rs) ;
    }
}
/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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