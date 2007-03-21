/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.ref;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.*;
import com.hp.hpl.jena.sparql.engine.main.StageBuilder;
import com.hp.hpl.jena.sparql.engine.ref.table.TableSimple;
import com.hp.hpl.jena.sparql.expr.ExprList;


class EvaluatorSimple implements Evaluator
{
    // Simple, slow, correct

    private ExecutionContext execCxt ;
    boolean debug = false ;

    EvaluatorSimple(ExecutionContext context)
    {
        this.execCxt = context ;
    }

    public ExecutionContext getExecContext()
    { return execCxt ; }

    public Table basicPattern(BasicPattern pattern)
    {
        QueryIterator qIter = StageBuilder.compile(pattern, Algebra.makeRoot(execCxt), execCxt) ;
        return TableFactory.create(qIter) ;
    }

    public Table join(Table tableLeft, Table tableRight)
    {
        if ( debug )
        {
            System.out.println("Join") ;
            tableLeft.dump() ;
            tableRight.dump() ;
        }
        return joinWorker(tableLeft, tableRight, false, null) ;
    }

    public Table leftJoin(Table tableLeft, Table tableRight, ExprList exprs)
    {
        if ( debug )
        {
            System.out.println("Left Join") ;
            tableLeft.dump() ;
            tableRight.dump() ;
            if ( exprs != null )
                System.out.println(exprs.toString()) ;
        }

        return joinWorker(tableLeft, tableRight, true, exprs) ;
    }

    public Table filter(ExprList expressions, Table table)
    {
        if ( debug )
        {
            System.out.println("Restriction") ;
            System.out.println(expressions.toString()) ;
            table.dump() ;
        }
        QueryIterator iter = table.iterator(execCxt) ;
        List output = new ArrayList() ;
        for ( ; iter.hasNext() ; )
        {
            Binding b = iter.nextBinding() ;
            if ( expressions.isSatisfied(b, execCxt) )
                output.add(b) ;
        }
        return new TableSimple(new QueryIterPlainWrapper(output.iterator(), execCxt)) ;
    }



    public Table union(Table tableLeft, Table tableRight)
    {
        if ( debug )
        {
            System.out.println("Union") ;
            tableLeft.dump() ;
            tableRight.dump() ;
        }
        QueryIterConcat output = new QueryIterConcat(execCxt) ;
        output.add(tableLeft.iterator(execCxt)) ;
        output.add(tableRight.iterator(execCxt)) ;
        return new TableSimple(output) ;
    }

    public Table list(Table table) { return table ; } 

    public Table order(Table table, List conditions)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterSort(qIter, conditions, getExecContext()) ;
        return new TableSimple(qIter) ;
    }

    public Table project(Table table, List vars)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterProject(qIter, vars, getExecContext()) ;
        return new TableSimple(qIter) ;
    }

    public Table reduced(Table table)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = QueryIterFixed.create(qIter, execCxt) ;
        qIter = new QueryIterReduced(qIter, getExecContext()) ;
        return new TableSimple(qIter) ;
    }

    public Table distinct(Table table)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = QueryIterFixed.create(qIter, execCxt) ;
        qIter = new QueryIterDistinct(qIter, getExecContext()) ;
        return new TableSimple(qIter) ;
    }

    public Table slice(Table table, long start, long length)
    {
        QueryIterator qIter = table.iterator(getExecContext()) ;
        qIter = new QueryIterSlice(qIter, start, length, getExecContext()) ;
        return new TableSimple(qIter) ;
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
        return new TableSimple(output) ;
    }
}
/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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