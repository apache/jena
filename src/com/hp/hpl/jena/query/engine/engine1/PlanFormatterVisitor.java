/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.engine1;

import java.util.Collection;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.Plan;
import com.hp.hpl.jena.query.engine.engine1.plan.*;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.query.serializer.FmtExprARQ;
import com.hp.hpl.jena.query.serializer.SerializationContext;
import com.hp.hpl.jena.query.util.FmtUtils;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.shared.PrefixMapping;

public class PlanFormatterVisitor implements PlanVisitor
{
    protected final static int INDENT = 2 ;
    private static boolean defaultClosingBracketOnSameLine = true ;

    
    protected IndentedWriter out ;
    protected SerializationContext context ; 
    private boolean closingBracketOnSameLine = defaultClosingBracketOnSameLine ;

    public PlanFormatterVisitor(IndentedWriter w, PrefixMapping pmap)
    {
        this(w, new SerializationContext(pmap) ) ;
    }


    public PlanFormatterVisitor(IndentedWriter w, SerializationContext context)
    {
        out = w ;
        this.context = context ; 
    }

    public void startVisit() {}
    public void finishVisit()
    {
        out.ensureStartOfLine() ;
        out.flush() ;
    }

    public void visit(PlanTriples planElt)
    {
        start("Triples") ;

        // Only one?  Always one line
        if ( planElt.getPattern().size() == 1 /*&& planElt.getConstraints().size() == 0 */)
        {
            out.print(" ") ;
            Triple t = (Triple)planElt.getPattern().get(0) ;
            formatTriple(t) ;
            finish() ;
            return ;
        }

        out.incIndent(INDENT) ;
        for ( Iterator iter = planElt.getPattern().iterator() ; iter.hasNext() ; )
        {
            out.newline() ;
            Triple t = (Triple)iter.next() ;
            formatTriple(t) ;
        }

        // Regardless, we place the closing "]" on a  new line.  
        if ( closingBracketOnSameLine )
            out.newline() ;

        out.decIndent(INDENT) ;
        finish() ;
    }

    public void visit(PlanTriplesBlock planElt)
    {
        String s = "BasicGraphPattern" ;
        multipleSubPlans(s, planElt.iterator() ) ;

    }

    public void visit(PlanGroup planElt)
    {
        String s = "Group" ;
        if ( ! planElt.canReorder() )
            s = "Group(fixed)" ;

        multipleSubPlans(s, planElt.iterator() ) ;
    }

    public void visit(PlanUnion planElt)
    {
        multipleSubPlans("Union", planElt.getSubElements().iterator() ) ;
    }

    public void visit(PlanOptional planElt)
    {
        singleSubPlan("Optional", planElt.getOptional()) ;
    }

    public void visit(PlanUnsaid planElt)
    {
        singleSubPlan("Unsaid", planElt.getSubElement()) ;
    }

    public void visit(PlanFilter planElt)
    {
        start("Constraint") ;
        out.print(" ") ;
        Expr c = planElt.getExpr() ;
        FmtExprARQ.format(out, context, c) ;
        finish() ;
    }

    public void visit(PlanNamedGraph planElt)
    {
        //singleSubPlan("NamedGraph", planElt.getSubElement()) ;
        start("NamedGraph") ;
        out.print(" ") ;
        out.print(slotToString(planElt.getGraphNameNode())) ;
        out.incIndent(INDENT) ;
        if ( planElt.getSubElement() != null )
        {
            out.println() ;  
            planElt.getSubElement().visit(this) ;
        }
        out.decIndent(INDENT) ;
        finish() ;
    }

    public void visit(PlanPropertyFunction planElt)
    {
        start("PropertyFunction") ;
        out.print(" ") ;
        planElt.getSubjArgs().output(out, context) ;
        out.print(" ") ;
        out.print(FmtUtils.stringForNode(planElt.getPredicate(), context)) ;
        out.print(" ") ;
        planElt.getObjArgs().output(out, context) ;
        finish() ;
        
    }
    
    public void visit(PlanExtension planElt)
    {
        start("Extension") ;
        out.print(" <") ;
        out.print(planElt.getElement().getURI()) ;
        out.print(">") ;
        finish() ;

    }

    public void visit(PlanDataset planElt)
    {
        singleSubPlan("Block", planElt.getSubElement()) ;
    }

    public void visit(PlanElementExternal planElt)
    { planElt.output(out, context) ; }
    
    public void visit(PlanDistinct planElt)
    {
        singleSubPlan("Distinct", planElt.getSubElement(), planElt.getVars()) ;
    }

    public void visit(PlanProject planElt)
    {
        singleSubPlan("Project", planElt.getSubElement(), planElt.getVars()) ;
    }

    public void visit(PlanOrderBy planElt)
    {
        singleSubPlan("OrderBy", planElt.getSubElement()) ;
    }

    public void visit(PlanLimitOffset planElt)
    {
        singleSubPlan("LimitOffset", planElt.getSubElement()) ;
    }


    private void plainPlan(String label)
    { 
        start(label) ;
        finish() ;
    }


    private void singleSubPlan(String label, PlanElement subElt)
    {
        singleSubPlan(label, subElt, null) ;
    }

    private void singleSubPlan(String label, PlanElement subElt, Collection vars)
    {
        start(label) ;
        if ( vars != null )
        {
            for ( Iterator iter = vars.iterator() ; iter.hasNext() ; )
            {
                Var vn = (Var)iter.next() ;
                out.print(" ?") ;
                out.print(vn.getVarName()) ;
            }
        }
        out.incIndent(INDENT) ;
        if ( subElt != null )
        {
            out.println() ;  
            subElt.visit(this) ;
        }
        out.decIndent(INDENT) ;
        finish() ;
    }

    private void multipleSubPlans(String label, Iterator iter)
    {
        start(label) ;
        out.incIndent(INDENT) ;
        for ( ; iter.hasNext() ; )
        {
            out.newline() ;
            PlanElement element = (PlanElement)iter.next() ;
            element.visit(this) ;
        }
        out.decIndent(INDENT) ;
        finish() ;
    }

    private void start(String name)
    {
        out.print(Plan.startMarker) ;
        out.print(name) ;
    }

    private void finish()
    {
        if ( closingBracketOnSameLine )
            out.print(Plan.finishMarker) ;
        else
        {
            out.println();
            out.print(Plan.finishMarker) ;
        }
    }

    private void formatTriple(Triple tp)
    {
        out.print(slotToString(tp.getSubject())) ;
        out.print(" ") ;
        out.print(slotToString(tp.getPredicate())) ;
        out.print(" ") ;
        out.print(slotToString(tp.getObject())) ;
    }

    private String slotToString(Node n)
    {
        return FmtUtils.stringForNode(n, context) ;
    }

}
/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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