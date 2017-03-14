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

package arq.examples;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.query.ResultSetFormatter ;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.riot.out.NodeFmtLib ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.op.OpBGP ;
import org.apache.jena.sparql.algebra.op.OpFilter ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.ResultSetStream ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.E_LessThan ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprVar ;
import org.apache.jena.sparql.expr.NodeValue ;

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
        bp.add(new Triple(var_x, NodeFactory.createURI(BASE+"p"), var_z)) ;
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
                System.out.println(NodeFmtLib.displayStr(n)) ;
                System.out.println(b) ; 
            }
            qIter.close() ;
        }
        else
        {
            // -------- Or make ResultSet from it (but not both - reading an
            //          iterator consumes the current solution)
            List<String> varNames = new ArrayList<>() ;
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
