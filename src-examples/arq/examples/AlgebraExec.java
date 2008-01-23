/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.examples;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.util.FmtUtils;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

/** Build an algebra expression and execute it */

public class AlgebraExec
{
    public static void main (String[] argv)
    {
        String BASE = "http://example/" ; 
        BasicPattern bp = new BasicPattern() ;
        Var var_x = Var.alloc("x") ;
        Var var_z = Var.alloc("z") ;
        
        // ---- Build expression
        bp.add(new Triple(var_x, Node.createURI(BASE+"p"), var_z)) ;
        Op op = new OpBGP(bp) ;
        //Expr expr = ExprUtils.parse("?z < 2 ") ;
        Expr expr = new E_LessThan(new ExprVar(var_z), NodeValue.makeNodeInteger(2)) ;
        op = OpFilter.filter(expr, op) ;

        // ---- Example setup
        Model m = makeModel() ;
        m.write(System.out, "TTL") ;
        System.out.println("--------------") ;
        System.out.print(op) ;
        System.out.println("--------------") ;

        // ---- Execute expression
        QueryIterator qIter = Algebra.exec(op, m.getGraph()) ;
        
        // -------- Either read the query iterator directly ...
        if ( false )
        {
            for ( ; qIter.hasNext() ; )
            {
                Binding b = qIter.nextBinding() ;
                Node n = b.get(var_x) ;
                System.out.println(FmtUtils.stringForNode(n)) ;
                System.out.println(b) ; 
            }
            qIter.close() ;
        }
        else
        {
            // -------- Or make ResultSet from it (but not both - reading an
            //          iterator consumes the current solution)
            List varNames = new ArrayList() ;
            varNames.add("x") ;
            varNames.add("z") ;
            ResultSet rs = new ResultSetStream(varNames, m, qIter);
            ResultSetFormatter.out(rs) ;
            qIter.close() ;
        }
        System.exit(0) ;
    }

    private static Model makeModel()
    {
        String BASE = "http://example/" ;
        Model model = ModelFactory.createDefaultModel() ;
        model.setNsPrefix("", BASE) ;
        Resource r1 = model.createResource(BASE+"r1") ;
        Resource r2 = model.createResource(BASE+"r2") ;
        Property p1 = model.createProperty(BASE+"p") ;
        Property p2 = model.createProperty(BASE+"p2") ;
        RDFNode v1 = model.createTypedLiteral("1", XSDDatatype.XSDinteger) ;
        RDFNode v2 = model.createTypedLiteral("2", XSDDatatype.XSDinteger) ;
        
        r1.addProperty(p1, v1).addProperty(p1, v2) ;
        r1.addProperty(p2, v1).addProperty(p2, v2) ;
        r2.addProperty(p1, v1).addProperty(p1, v2) ;
        
        return model  ;
    }
 
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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