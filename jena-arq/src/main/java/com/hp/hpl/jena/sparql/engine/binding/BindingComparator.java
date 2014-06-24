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

package com.hp.hpl.jena.sparql.engine.binding;

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Collections ;
import java.util.Comparator ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecException ;
import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.VariableNotBoundException ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.util.NodeUtils ;

public class BindingComparator implements java.util.Comparator<Binding>
{
    private static Comparator<Var> varComparator = new Comparator<Var>()
        {
            @Override
            public int compare(Var o1, Var o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        };
    
    private List<SortCondition> conditions ;
    private FunctionEnv env ;
    
    public BindingComparator(List<SortCondition> conditions, ExecutionContext execCxt)
    {
        this.conditions = conditions ;
        env = execCxt ;
    }
    
    public BindingComparator(List<SortCondition> _conditions)
    {
        conditions = _conditions ;
        this.env = new FunctionEnvBase();
    }
    
    public List<SortCondition> getConditions() { return Collections.unmodifiableList(conditions) ; } 

    // Compare bindings by iterating.
    // Node comparsion is:
    //  Compare by 

    @Override
    public int compare(Binding bind1, Binding bind2)
    {
        for ( SortCondition sc : conditions )
        {
            if ( sc.expression == null )
            {
                throw new QueryExecException( "Broken sort condition" );
            }

            NodeValue nv1 = null;
            NodeValue nv2 = null;

            try
            {
                nv1 = sc.expression.eval( bind1, env );
            }
            catch ( VariableNotBoundException ex )
            {
            }
            catch ( ExprEvalException ex )
            {
                Log.warn( this, ex.getMessage() );
            }

            try
            {
                nv2 = sc.expression.eval( bind2, env );
            }
            catch ( VariableNotBoundException ex )
            {
            }
            catch ( ExprEvalException ex )
            {
                Log.warn( this, ex.getMessage() );
            }

            Node n1 = NodeValue.toNode( nv1 );
            Node n2 = NodeValue.toNode( nv2 );
            int x = compareNodes( nv1, nv2, sc.direction );
            if ( x != Expr.CMP_EQUAL )
            {
                return x;
            }
        }
        // Same by the SortConditions - now do any extra tests to make sure they are unique.
        return compareBindingsSyntactic(bind1, bind2) ;
        //return 0 ;
    }
    
    private static int compareNodes(NodeValue nv1, NodeValue nv2, int direction)
    {
        int x = compareNodesRaw(nv1, nv2) ;
        if ( direction == Query.ORDER_DESCENDING )
            x = -x ;
        return x ;
    }
    
    public static int compareNodesRaw(NodeValue nv1, NodeValue nv2)
    {
        // Absent nodes sort to the start
        if ( nv1 == null )
            return nv2 == null ? Expr.CMP_EQUAL : Expr.CMP_LESS ;

        if ( nv2 == null )
            return Expr.CMP_GREATER ;
        
        // Compare - always getting a result.
        return NodeValue.compareAlways(nv1, nv2) ;
    }
    

    public static int compareBindingsSyntactic(Binding bind1, Binding bind2)
    {
        int x = 0 ;
        
        // The variables in bindings are unordered.  If we want a good comparison, we need an order.
        // We'll choose lexicographically by name.  Unfortunately this means a sort on every comparison.
        List<Var> varList = new ArrayList<>();
        for (Iterator<Var> iter = bind1.vars(); iter.hasNext(); )
        {
            varList.add(iter.next());
        }
        for (Iterator<Var> iter = bind2.vars(); iter.hasNext(); )
        {
            varList.add(iter.next());
        }
        // Lets try to make it a tiny bit faster by using Arrays.sort() instead of Collections.sort().
        Var[] vars = new Var[varList.size()];
        vars = varList.toArray(vars);
        Arrays.sort(vars, varComparator);
        
        for (Var v : vars)
        {
            Node n1 = bind1.get(v) ;
            Node n2 = bind2.get(v) ;
            x = NodeUtils.compareRDFTerms(n1, n2) ; 
            if  ( x != 0 )
                return x ;
        }
        return x ;
    }
}
