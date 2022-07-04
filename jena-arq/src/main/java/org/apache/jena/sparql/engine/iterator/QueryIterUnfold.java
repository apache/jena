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

package org.apache.jena.sparql.engine.iterator;

import java.util.Iterator;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeFactoryExtra;

public class QueryIterUnfold extends QueryIterRepeatApply
{
	public static final String datatypeUriUntypedList = "http://www.w3.org/1999/02/22-rdf-syntax-ns#UntypedList";

    protected final Expr expr ;
    protected final Var var1 ;
    protected final Var var2 ;

    public QueryIterUnfold(QueryIterator qIter, Expr expr, Var var1, Var var2, ExecutionContext execCxt) {
        super(qIter, execCxt) ;
        this.expr = expr ;
        this.var1 = var1 ;
        this.var2 = var2 ;
    }

    @Override
    protected QueryIterator nextStage(Binding inputBinding) {
        NodeValue nv = expr.eval( inputBinding, getExecContext() );
        Node n = nv.asNode();
        if ( n.isLiteral() ) {
            String dtURI = n.getLiteralDatatypeURI();
            if ( datatypeUriUntypedList.equals(dtURI) )
                return unfoldUntypedList(n.getLiteralLexicalForm(), inputBinding);
        }

        return QueryIterSingleton.create( inputBinding, var1, n, getExecContext() );
    }

    protected QueryIterator unfoldUntypedList(String listAsValue, Binding inputBinding) {
        Iterator<Node> itListElmts = parseUntypedList(listAsValue);
        return new QueryIteratorBase() {
            @Override
            public void output(IndentedWriter out, SerializationContext sCxt) {
                out.write("a QueryIterator in QueryIterUnfold");
            }

            @Override
            protected Binding moveToNextBinding() {
                return BindingFactory.binding( inputBinding, var1, itListElmts.next() );
            }

            @Override
            protected boolean hasNextBinding() {
                return itListElmts.hasNext();
            }

            @Override
            protected void requestCancel() { } // nothing to do really

            @Override
            protected void closeIterator() { } // nothing to do really
        };
    }

    protected Iterator<Node> parseUntypedList(String listAsValue) {
        if ( listAsValue.startsWith("[") )
            listAsValue = listAsValue.substring( 1, listAsValue.length() - 1 );

// TODO: this method needs to be improved and, in particular, made more robust in terms
//       of parsing the given lexical form of the literal; for instance, simply splitting
//       by commas is an issue if the list contains literals with commas inside -- can we
//       use existing code for parsing Turtle here?

        final String[] listAsArray = listAsValue.split(",");
        final PrefixMap pmap = (getExecContext().getDataset() == null) ? null : getExecContext().getDataset().prefixes();
        return new Iterator<>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < listAsArray.length;
            }

            @Override
            public Node next() {
                String listElmt = listAsArray[i].strip();
                i++;
                if ( pmap != null )
                	return NodeFactoryExtra.parseNode(listElmt, pmap);
                else
                    return NodeFactoryExtra.parseNode(listElmt);
            }
        };
    }

}
