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

package org.apache.jena.sparql.expr.aggregate;

import java.util.Locale ;
import java.util.Objects;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.QueryExecException ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.E_Function ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.apache.jena.sparql.serializer.SerializationContext ;
import org.apache.jena.sparql.sse.writers.WriterExpr ;
import org.apache.jena.sparql.util.ExprUtils ;

/** Syntax element and framework execution for custom aggregates.  
 */
public class AggCustom extends AggregatorBase
{
    // See also ExprAggregator
    
    private final String iri ;

    public AggCustom(String iri, ExprList exprs) { 
        super("AGG", false, exprs) ;
        this.iri = iri ; 
    } 
    
    @Override
    public Aggregator copy(ExprList exprs) { return new AggCustom(iri, exprs) ; }
    
    @Override
    public String asSparqlExpr(SerializationContext sCxt) {
        IndentedLineBuffer x = new IndentedLineBuffer() ;
        if ( ! AggregateRegistry.isRegistered(iri) ) {
            // If not registered and if parsed in again not registered, it becomes a function.
            // AGG <iri>(...) syntax.  It can;'t have been legal SPARQL 1.1 unless it got
            // unregistered in which case all bets are off anyway.
            x.append(getName()) ;
            x.append(" ") ;
        }
        x.append("<") ;
        x.append(iri);
        x.append(">") ;
        if ( isDistinct )
            x.append(" DISTINCT ") ;
        x.incIndent(); 
        x.append("(") ;
        ExprUtils.fmtSPARQL(x, getExprList(), sCxt) ;
        x.append(")") ;
        return x.asString() ;
    }

    @Override
    public String toPrefixString() { 
        IndentedLineBuffer x = new IndentedLineBuffer() ;
        x.append("(") ;
        x.append(getName().toLowerCase(Locale.ROOT)) ;
        x.append(" <") ;
        x.append(iri);
        x.append("> ") ;
        x.incIndent(); 
        
        if ( isDistinct )
            x.append("distinct ") ;
        boolean first = true ;
        for ( Expr e : getExprList() ) {
            if ( ! first )
                x.append(" ");
            first = false ;
            WriterExpr.output(x, e, null) ;
            first = false ;
        }
        x.decIndent();
        x.append(")") ;
        return x.asString() ;
    }

    @Override
    public Accumulator createAccumulator()
    { 
        AccumulatorFactory f = AggregateRegistry.getAccumulatorFactory(iri) ;
        if ( f == null )
            throw new QueryExecException("Unregistered aggregate: "+iri) ;
        return f.createAccumulator(this) ;
    }

    @Override
    public Node getValueEmpty()     { return AggregateRegistry.getNoGroupValue(iri) ; } 

    @Override
    public Expr getExpr()           { return null ; }
    
    public String getIRI()                  { return iri ; }

    @Override
    public int hashCode()   {
        if ( ! AggregateRegistry.isRegistered(iri) ) {
            return asFunction().hashCode() ;
        }
        return HC_AggCustom ^ getExprList().hashCode() ^ iri.hashCode() ;
    }
    
    private E_Function asFunction() {
        return new E_Function(iri, exprList) ;
    }
    
    @Override
    public boolean equals(Aggregator other, boolean bySyntax) {
        if ( other == null ) return false ; 
        if ( this == other ) return true ;
        
        if ( ! AggregateRegistry.isRegistered(iri) ) {
            E_Function f1 = asFunction() ;
            if ( ! ( other instanceof AggCustom ) )
                return false ;
            E_Function f2 = ((AggCustom)other).asFunction() ;
            return f1.equals(f2, bySyntax) ;
        }
        
        if ( ! ( other instanceof AggCustom ) )
            return false ;
        AggCustom agg = (AggCustom)other ;
        return Objects.equals(this.iri, agg.iri) &&
               this.isDistinct == agg.isDistinct &&
               this.getExprList().equals(agg.getExprList(), bySyntax) ;
    } 

    public static Accumulator createAccNull() { return new  AccCustom() ; }
    
    // ---- Accumulator
    private static class AccCustom implements Accumulator
    {
        private int nBindings = 0 ;

        public AccCustom() { }

        @Override
        public void accumulate(Binding binding, FunctionEnv functionEnv)
        { nBindings++ ; }

        @Override
        public NodeValue getValue()
        {
            return null ;
        }
    }

}
