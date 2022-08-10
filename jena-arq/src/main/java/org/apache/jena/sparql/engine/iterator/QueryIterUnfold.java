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
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeFactoryExtra;

public class QueryIterUnfold extends QueryIterRepeatApply
{
	public static final String datatypeUriUntypedList = "http://www.w3.org/1999/02/22-rdf-syntax-ns#UntypedList";
	public static final String datatypeUriUntypedMap  = "http://www.w3.org/1999/02/22-rdf-syntax-ns#UntypedMap";
	public static final String datatypeUriTypedList   = "http://www.w3.org/1999/02/22-rdf-syntax-ns#TypedList";
	public static final String datatypeUriTypedMap    = "http://www.w3.org/1999/02/22-rdf-syntax-ns#TypedMap";

	public static final RDFDatatype datatypeUntypedList = new BaseDatatype(datatypeUriUntypedList);
	public static final RDFDatatype datatypeUntypedMap  = new BaseDatatype(datatypeUriUntypedMap);
	public static final RDFDatatype datatypeTypedList   = new BaseDatatype(datatypeUriTypedList);
	public static final RDFDatatype datatypeTypedMap    = new BaseDatatype(datatypeUriTypedMap);

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
        final NodeValue nv;
        try {
            nv = expr.eval( inputBinding, getExecContext() );
        }
        catch ( final ExprEvalException ex ) {
            // If the expression failed to evaluate, we create no
            // no assignment (exactly as in the case of BIND, see
            // the 'accept' method in 'QueryIterAssign')

            return QueryIterSingleton.create( inputBinding, getExecContext() );
        }

        Node n = nv.asNode();
        if ( n.isLiteral() ) {
            String dtURI = n.getLiteralDatatypeURI();
            if ( datatypeUriUntypedList.equals(dtURI) )
                return unfoldUntypedList(n.getLiteralLexicalForm(), inputBinding);
            if ( datatypeUriTypedList.equals(dtURI) )
                return unfoldTypedList(n.getLiteralLexicalForm(), inputBinding);
            if ( datatypeUriUntypedMap.equals(dtURI) )
                return unfoldUntypedMap(n.getLiteralLexicalForm(), inputBinding);
            if ( datatypeUriTypedMap.equals(dtURI) )
                return unfoldTypedMap(n.getLiteralLexicalForm(), inputBinding);
        }

        return QueryIterSingleton.create( inputBinding, var1, n, getExecContext() );
    }

    protected QueryIterator unfoldUntypedList(String listAsValue, Binding inputBinding) {
        Iterator<Node> itListElmts = parseUntypedList(listAsValue);
        return new QueryIterUnfoldWorker(inputBinding, itListElmts);
    }

    protected Iterator<Node> parseUntypedList(String listAsValue) {
        if ( listAsValue.startsWith("[") )
            listAsValue = listAsValue.substring( 1, listAsValue.length() - 1 );

        return parseList(listAsValue);
    }

    protected QueryIterator unfoldTypedList(String listAsValue, Binding inputBinding) {
        Iterator<Node> itListElmts = parseTypedList(listAsValue);
        return new QueryIterUnfoldWorker(inputBinding, itListElmts);
    }

    protected Iterator<Node> parseTypedList(String listAsValue) {
        listAsValue = listAsValue.substring( 1, listAsValue.lastIndexOf("]") );
        return parseList(listAsValue);
    }

    protected QueryIterator unfoldUntypedMap(String mapAsValue, Binding inputBinding) {
        throw new UnsupportedOperationException("TODO");
    }

    protected QueryIterator unfoldTypedMap(String mapAsValue, Binding inputBinding) {
        throw new UnsupportedOperationException("TODO");
    }

    protected Iterator<Node> parseList(String listAsValue) {
        final PrefixMap pmap = (getExecContext().getDataset() == null) ? null : getExecContext().getDataset().prefixes();
        final Iterator<String> itListElmts = extractListElements(listAsValue);
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return itListElmts.hasNext();
            }

            @Override
            public Node next() {
                final String listElmt = itListElmts.next();
                final Node n;
                if ( listElmt.startsWith("[") && listElmt.endsWith("]") )
                    n = NodeFactory.createLiteral(listElmt, datatypeUntypedList);
                else if ( listElmt.startsWith("[") && ! listElmt.endsWith("]") )
                    n = NodeFactory.createLiteral(listElmt, datatypeTypedList); // brittle
                else if ( listElmt.startsWith("{") && listElmt.startsWith("}") )
                    n = NodeFactory.createLiteral(listElmt, datatypeUntypedMap);
                else if ( listElmt.startsWith("{") && ! listElmt.startsWith("}") )
                    n = NodeFactory.createLiteral(listElmt, datatypeTypedMap); // brittle
                else if ( pmap != null )
                    n = NodeFactoryExtra.parseNode(listElmt, pmap);
                else
                    n = NodeFactoryExtra.parseNode(listElmt);
                return n;
            }
        };
    }

    protected Iterator<String> extractListElements(String listAsValue) {
        listAsValue = listAsValue.strip();

        if ( listAsValue.isEmpty() ) {
            return new Iterator<String>() {
                @Override public boolean hasNext() { return false; }
                @Override public String next() { throw new NoSuchElementException(); }
            };
        }

// TODO: this method needs to be improved and, in particular, made more robust in terms
//       of parsing the given lexical form of the literal; for instance, simply splitting
//       by commas is an issue if the list contains literals with commas inside -- can we
//       use existing code for parsing Turtle here?

        return new ListElementExtractor(listAsValue);
    }


    protected class QueryIterUnfoldWorker extends QueryIteratorBase {
        protected final Binding inputBinding;
        protected final Iterator<Node> itListElmts;

        public QueryIterUnfoldWorker(Binding inputBinding, Iterator<Node> itListElmts) {
            this.inputBinding = inputBinding;
            this.itListElmts = itListElmts;
        }

        @Override
        public void output(IndentedWriter out, SerializationContext sCxt) {
            out.write("QueryIterUnfoldWorker");
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
    }


    protected static class ListElementExtractor implements Iterator<String> {
        protected final String listAsString;
        protected int cursor = 0;
        protected String nextElmt = null;

        public ListElementExtractor( final String listAsString ) {
            this.listAsString = listAsString.strip();
        }

        @Override
        public boolean hasNext() {
            if ( nextElmt != null )
                return true;

            consumeWhiteSpace();
            if ( cursor >= listAsString.length() ) {
                return false;
            }

            final int nextElmtBegin = cursor;
            if ( listAsString.charAt(cursor) == '[' ) {
                advanceToEndOfSubList();
            }
            else if ( listAsString.charAt(cursor) == '{' ) {
                advanceToEndOfSubMap();
            }
            advanceToNextCommaOrEnd();
            nextElmt = listAsString.substring(nextElmtBegin, cursor);
            cursor++; // move cursor to after comma
            return true;
        }

        @Override
        public String next() {
            if ( ! hasNext() )
                throw new NoSuchElementException();

            String r = nextElmt;
            nextElmt = null;
            return r;
        }

        protected void consumeWhiteSpace() {
            while ( cursor < listAsString.length() && listAsString.charAt(cursor) == ' ' )
                cursor++;
        }

        protected void advanceToEndOfSubList() {
            while ( cursor < listAsString.length() && listAsString.charAt(cursor) != ']' ) {
                cursor++;
                if ( listAsString.charAt(cursor) == '[' ) {
                    advanceToEndOfSubList();
                    cursor++;
                }
                else if ( listAsString.charAt(cursor) == '{' ) {
                    advanceToEndOfSubMap();
                    cursor++;
                }
            }
        }

        protected void advanceToEndOfSubMap() {
            while ( cursor < listAsString.length() && listAsString.charAt(cursor) != '}' ) {
                cursor++;
                if ( listAsString.charAt(cursor) == '[' ) {
                    advanceToEndOfSubList();
                    cursor++;
                }
                else if ( listAsString.charAt(cursor) == '{' ) {
                    advanceToEndOfSubMap();
                    cursor++;
                }
            }
        }

        protected void advanceToNextCommaOrEnd() {
            while ( cursor < listAsString.length() && listAsString.charAt(cursor) != ',' )
                cursor++;
        }
    }

}
