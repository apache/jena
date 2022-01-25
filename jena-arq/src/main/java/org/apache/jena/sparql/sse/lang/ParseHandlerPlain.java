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

package org.apache.jena.sparql.sse.lang;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.sse.*;

public class ParseHandlerPlain implements ParseHandler {
    private Deque<Node> tripleTermStack   = new ArrayDeque<>();
    private int         inTripleTermDepth = 0;
    private ListStack   listStack         = new ListStack();
    private Item        currentItem       = null;
    private int         depth             = 0;
    private LabelToNode bNodeLabels       = LabelToNode.createScopeGlobal();

    // Allocation of fresh variables.
    private VarAlloc    varAlloc          = new VarAlloc(ARQConstants.allocSSEUnamedVars);
    private VarAlloc    varAllocND        = new VarAlloc(ARQConstants.allocSSEAnonVars);
    private VarAlloc    varAllocIntern    = new VarAlloc(ARQConstants.allocSSENamedVars);

    @Override
    public Item getItem() {
        return currentItem;
    }

    @Override
    public void parseStart() {
        depth = 0;
    }

    @Override
    public void parseFinish() {
        if ( depth != 0 )
            Log.warn(this, "Stack error: depth =" + depth + " at end of parse run");
        depth = -1;
    }

    @Override
    public void listStart(int line, int column) {
        ItemList list = new ItemList(line, column);
        pushList(list);
        setCurrentItem(Item.createList(list));
    }

    @Override
    public void listFinish(int line, int column) {
        ItemList list = popList();
        Item item = Item.createList(list);
        listAdd(item);
    }

    protected void setCurrentItem(Item item) {
        currentItem = item;
    }

    protected void node(Node node, int line, int column) {
        if ( inTripleTermDepth > 0 ) {
            tripleTermStack.addLast(node);
            return;
        }
        Item item = Item.createNode(node, line, column);
        listAdd(item);
    }

    protected void listAdd(Item item) {
        if ( listStack.isEmpty() ) {
            // Top level is outside a list.
            setCurrentItem(item);
            return;
        }

        if ( item != null ) {
            ItemList list = currentList();
            list.add(item);
        }
        setCurrentItem(item);
    }

    @Override
    public void emitSymbol(int line, int column, String symbol) {
        listAdd(Item.createSymbol(symbol, line, column));
    }

    @Override
    public void emitVar(int line, int column, String varName) {
        Var var = null;
        switch (varName) {
            case "" :
// "?"
                var = varAlloc.allocVar();
                break;
            case ARQConstants.allocVarAnonMarker :
// "??" -- Allocate a non-distinguished variable
                var = varAllocND.allocVar();
                break;
            case ARQConstants.allocVarMarker :
// "?." -- Allocate a named variable
                var = varAllocIntern.allocVar();
                break;
            default :
                var = Var.alloc(varName);
                break;
        }
        node(var, line, column);
    }

    @Override
    public void emitLiteral(int line, int column, String lexicalForm, String langTag, String datatypeIRI, String datatypePN) {
        Node n = null;

        if ( datatypeIRI != null || datatypePN != null ) {
            if ( datatypePN != null )
                datatypeIRI = resolvePrefixedName(datatypePN, line, column);

            RDFDatatype dType = TypeMapper.getInstance().getSafeTypeByName(datatypeIRI);
            n = NodeFactory.createLiteral(lexicalForm, dType);
        } else
            n = NodeFactory.createLiteral(lexicalForm, langTag);
        node(n, line, column);
    }

    @Override
    final public void emitBNode(int line, int column, String label) {
        Node n = null;
        if ( label.equals("") )
            // Fresh anonymous bNode
            n = NodeFactory.createBlankNode();
        else
            n = bNodeLabels.get(null, label);
        node(n, line, column);
    }

    @Override
    public void emitIRI(int line, int column, String iriStr) {
        Node n = RiotLib.createIRIorBNode(iriStr);
        node(n, line, column);
    }

    @Override
    public void emitPName(int line, int column, String pname) {
        String iriStr = resolvePrefixedName(pname, line, column);
        emitIRI(line, column, iriStr);
    }

    @Override
    public void tripleTermStart(int line, int column) {
        inTripleTermDepth++;
    }

    @Override
    public void tripleTermFinish(int line, int column) {
        // Note - reverse order
        Node o = tripleTermStack.removeLast();
        Node p = tripleTermStack.removeLast();
        Node s = tripleTermStack.removeLast();
        Node n = NodeFactory.createTripleNode(s, p, o);
        inTripleTermDepth--;

        if ( inTripleTermDepth == 0 ) {
            if ( !tripleTermStack.isEmpty() )
                Log.warn(this, "Triple Term Stack error: stack not empty");
        }
        node(n, line, column);
    }

    protected ItemList currentList() {
        return listStack.getCurrent();
    }

    protected ItemList popList() {
        depth--;
        setCurrentItem(null);
        return listStack.pop();
    }

    protected void pushList(ItemList list) {
        listStack.push(list);
        depth++;
    }

    protected String resolvePrefixedName(String pname, int line, int column) {
        // Not resolving. Make a strange URI.
        return "pname:" + pname;
    }

    protected static class ListStack {
        private Deque<ItemList> stack = new ArrayDeque<>();

        boolean isEmpty() {
            return stack.size() == 0;
        }

        ItemList getCurrent() {
            if ( stack.size() == 0 )
                return null;
            return stack.peek();
        }

        void push(ItemList list) {
            stack.push(list);
        }

        ItemList pop() {
            return stack.pop();
        }
    }

    protected static void throwException(String msg, int line, int column) {
        throw new SSE_ParseException("[" + line + ", " + column + "] " + msg, line, column);
    }

    protected static void throwException(String msg, ItemLocation loc) {
        throwException(msg, loc.getLine(), loc.getColumn());
    }
}
