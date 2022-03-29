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

package org.apache.jena.shex.parser;

import static org.apache.jena.shex.parser.ParserShExC.Inline.INLINE;
import static org.apache.jena.shex.parser.ParserShExC.Inline.NOT_INLINE;
import static org.apache.jena.sparql.util.NodeUtils.nullToAny;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.EscapeStr;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.irix.IRIs;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.lang.extra.LangParserBase;
import org.apache.jena.riot.lang.extra.LangParserLib;
import org.apache.jena.shex.*;
import org.apache.jena.shex.expressions.*;
import org.apache.jena.shex.sys.SysShex;

/** ShEx Compact syntax parser */
public class ParserShExC extends LangParserBase {

    private IndentedWriter out;
    /** Print the call nesting */
    public static boolean DEBUG_PARSE = false;
    /** Print the stack operations */
    public static boolean DEBUG_STACK = false;

    /** Print various unexpected situations */
    public static boolean DEBUG_DEV = false;

    static enum Inline { INLINE, NOT_INLINE }

    protected ParserShExC() {
        this.out = IndentedWriter.clone(IndentedWriter.stdout);
    }

    // -- All the top level shapes, as seen in order.
    private List<ShexShape> shapes = new ArrayList<>();
    // The distinguished start shape. This is also in the list of all shapes.
    private ShexShape startShape = null;
    private List<String> imports = null;
    private String sourceURI = null;
    private boolean explicitBase = false;
    private String baseURI = null;

    // The shape currently in progress.
    private Node currentShexShapeLabel = null;
    // TripleExpression references
    private Map<Node, TripleExpression> tripleExprRefs = new HashMap<>();

    // Stack of shape expressions used during parsing a top level shape.
    private Deque<ShapeExpression> shapeExprStack = new ArrayDeque<>();
    private ShapeExpression currentShapeExpression() { return peek(shapeExprStack); }

    private Deque<TripleExpression> tripleExprStack = new ArrayDeque<>();
    private TripleExpression currentTripleExpression() { return peek(tripleExprStack); }

    private void printState() {
        if ( DEBUG_DEV ) {
            printStack("shapeExprStack", shapeExprStack);
            printStack("tripleExprStack", tripleExprStack);
        }
    }

    // Convert exceptions, if necessary.
    protected String unescapeStr(String lex, int line, int column) {
        return convert(()->LangParserLib.unescapeStr(lex, line, column), line, column);
    }

    private static String convert(Supplier<String> action, int line, int column) {
        try { return action.get(); }
        catch (RiotException ex) { throw new ShexParseException(ex.getMessage(), line, column); }
    }

    @Override
    protected String resolveQuotedIRI(String iriStr, int line, int column) {
        return convert(()->super.resolveQuotedIRI(iriStr, line, column), line, column);
    }

    @Override
    protected String resolvePName(String pname, int line, int column) {
        return convert(()->super.resolvePName(pname, line, column), line, column);
    }

    private <T> void printStack(String string, Deque<T> stack) {
        System.out.printf("%s: %d: %s\n", string, stack.size(), stack);
    }

    // -- Parser

    @Override
    protected void setBase(String iriStr, int line, int column) {
        super.setBase(iriStr, line, column);
        if ( ! explicitBase )
            // Record first base seen.
            this.baseURI = iriStr;
        this.explicitBase = true;
    }

    public void setSourceAndBase(String originURI, String baseURI ) {
        this.sourceURI = originURI;
        this.explicitBase = false;
        this.baseURI = baseURI;
    }

    public void parseShapesStart() { }

    public ShexSchema parseShapesFinish() {
        // Check stacks empty.
        if ( currentShexShapeLabel != null )
            throw new InternalErrorException("shape in-progress");
        if (! shapeExprStack.isEmpty() )
            throw new InternalErrorException("shape expresion stack not empty");
        // last base seen.
        return ShexSchema.shapes(sourceURI, baseURI, super.profile.getPrefixMap(), startShape, shapes, imports, tripleExprRefs);
    }

    protected void imports(String iri, int line, int column) {
        if ( imports == null )
            imports = new ArrayList<>();
        if ( ! IRIs.check(iri) )
            profile.getErrorHandler().warning("Bad IRI: <"+iri+">", line, column);
        imports.add(iri);
    }

    protected void startShexDoc() { }

    protected void finishShexDoc() { }

    // ---- One shape.

    // Start of top level shape, not "start=";
    protected void startShapeExprDecl() {
        start("ShapeExprDecl");
        startShapeExpressionTop();
    }

    protected void finishShapeExprDecl() {
        ShapeExpression sExpr = finishShapeExpressionTop();
        newShape(sExpr);
        finish("ShapeExprDecl");
    }

    protected void shapeExprDecl(Node label, int line, int column) {
        debug("shape label: %s", label);
        currentShexShapeLabel = label;
    }

    protected void shapeExternal() {
        debug("shape external");
    }

    // Start of top level shape, "start="
    protected void startStartClause() {
        start("StartClause");
        currentShexShapeLabel = SysShex.startNode;
        startShapeExpressionTop();
    }

    protected void finishStartClause() {
        ShapeExpression sExpr = finishShapeExpressionTop();
        startShape = newShape(sExpr);
        finish("StartClause");
    }

    private ShexShape newShape(ShapeExpression sExpr) {
        ShexShape newShexShape = new ShexShape(currentShexShapeLabel, sExpr);
        shapes.add(newShexShape);
        currentShexShapeLabel = null;
        return newShexShape;
    }

    // ---- Shape expressions

    private void startShapeExpressionTop() {
        start("startShapeExpressionTop");
        // Stack is empty.
        if ( DEBUG_DEV ) {
            if ( ! shapeExprStack.isEmpty() )
                debug("startShapeExpressionTop: Stack not empty");
        }
    }

    private ShapeExpression finishShapeExpressionTop() {
        if ( shapeExprStack.isEmpty() )
            return ShapeExprNone.get();

        ShapeExpression sExpr = pop(shapeExprStack);
        if ( DEBUG_DEV ) {
            if ( ! shapeExprStack.isEmpty() )
                debug("finishShapeExpressionTop: Stack not empty");
        }
        finish("finishShapeExpressionTop");
        return sExpr;
    }

    // ---- Shape expressions

    private int startShapeOp() {
        return front(shapeExprStack);
    }

    // Do nothing with the stack but pairs with startShapeOp
    private void finishShapeOpNoAction(String operation, int idx) { }

    private List<ShapeExpression> finishShapeOp(int idx) {
        return pop(shapeExprStack, idx);
    }

    private void finishShapeOp(int idx, Function<List<ShapeExpression>, ShapeExpression> action) {
        if ( action == null )
            return ;
        List<ShapeExpression> args = finishShapeOp(idx);
        if ( args == null )
            return ;
        processShapeExprArgs(args, action);
    }

    private void processShapeExprArgs(List<ShapeExpression> args, Function<List<ShapeExpression>, ShapeExpression> action) {
        if ( action != null ) {
            ShapeExpression sExpr = action.apply(args);
            if ( sExpr != null )
                push(shapeExprStack, sExpr);
        }
    }

    // ---- TripleExpression

//    private void startTripleExpressionTop() {
//        start("startTripleExpressionTop");
//        // Stack is empty.
//        if ( DEBUG ) {
//            if ( ! tripleExprStack.isEmpty() )
//                debug("startTripleExpressionTop: Stack not empty");
//        }
//    }
//
//    private TripleExpression finishTripleExpressionTop() {
//        if ( tripleExprStack.isEmpty() )
//            return TripleExpressionNone.get();
//
//        TripleExpression tExpr = pop(tripleExprStack);
//        if ( DEBUG ) {
//            if ( ! tripleExprStack.isEmpty() )
//                debug("finishShapeExpressionTop: Stack not empty");
//        }
//        finish("finishShapeExpressionTop");
//        return tExpr;
//    }

    private int startTripleOp() {
        return front(tripleExprStack);
    }

    // Do noting with the stack but pairs with startShapeOp
    private void finishTripleOpNoAction(String operation, int idx) { }

    private List<TripleExpression> finishTripleOp(int idx) {
        return pop(tripleExprStack, idx);
    }

    private void finishTripleOp(int idx, Function<List<TripleExpression>, TripleExpression> action) {
        if ( action == null )
            return ;
        List<TripleExpression> args = finishTripleOp(idx);
        if ( args == null )
            return ;
        processTripleExprArgs(args, action);
    }

    private void processTripleExprArgs(List<TripleExpression> args, Function<List<TripleExpression>, TripleExpression> action) {
        if ( action != null ) {
            TripleExpression tExpr = action.apply(args);
            if ( tExpr != null )
                push(tripleExprStack, tExpr);
        }
    }


    // -- Shape Structure

    protected int startShapeExpression(Inline inline) {
        start(inline, "ShapeExpression");
        return startShapeOp();
    }

    protected void finishShapeExpression(Inline inline, int idx) {
        finishShapeOpNoAction("ShapeExpression", idx);
        finish(inline, "ShapeExpression");
    }

    protected int startShapeOr(Inline inline) {
        start(inline, "ShapeOr");
        return startShapeOp();
    }

    protected void finishShapeOr(Inline inline, int idx) {
        finishShapeOp(idx, ShapeExprOR::create);
        finish(inline, "ShapeOr");
    }

    protected int startShapeAnd(Inline inline) {
        start(inline, "ShapeAnd");
        return startShapeOp();
    }

    protected void finishShapeAnd(Inline inline, int idx) {
        finishShapeOp(idx, ShapeExprAND::create);
        finish(inline, "ShapeAnd");
    }

    protected int startShapeNot(Inline inline) {
        start(inline, "ShapeNot");
        return startShapeOp();
    }

    protected void finishShapeNot(Inline inline, int idx, boolean negate) {
        int x = front(shapeExprStack) - idx ;
        if ( x > 1)
            throw new InternalErrorException("Shape NOT - multiple items on the stack");
        if ( negate && ! shapeExprStack.isEmpty() ) {
            ShapeExpression shExpr = pop(shapeExprStack);
            ShapeExpression shExpr2 = new ShapeExprNOT(shExpr);
            push(shapeExprStack, shExpr2);
        }
        finish(inline, "ShapeNot");
    }

    protected int startShapeAtom(Inline inline) {
        start(inline, "ShapeAtom");
        return startShapeOp();
    }

    protected void finishShapeAtom(Inline inline, int idx) {
        //Gather NodeConstraints parts, Kind, datatype and facets, together.
        finishShapeOp(idx, ShapeExprAND::create);
        //finishShapeOpNoAction("ShapeAtom", idx);
        finish(inline, "ShapeAtom");
    }

    protected void shapeAtomDOT() {
        push(shapeExprStack, new ShapeExprTrue());
    }

    protected void shapeReference(Node ref) {
        push(shapeExprStack, new ShapeExprRef(ref));
    }

    protected void startShapeDefinition() {
        start("ShapeDefinition");
    }

    protected void finishShapeDefinition(TripleExpression tripleExpr, List<Node> extras, boolean closed) {
        if ( tripleExpr == null )
            tripleExpr = TripleExprNone.get();
        ShapeExprTripleExpr shape = ShapeExprTripleExpr.newBuilder()
                //.label(???)
                .closed(closed)
                .extras(extras)
                .shapeExpr(tripleExpr).build();
        push(shapeExprStack, shape);
        finish("ShapeDefinition");
    }

    // ?? Top of TripleExpression
    protected int startTripleExpression() {
        start("TripleExpression");
        return startTripleOp();
    }

    protected TripleExpression finishTripleExpression(int idx) {
        finishTripleOp(idx, TripleExprOneOf::create);
        TripleExpression tripleExpr = pop(tripleExprStack);
        finish("TripleExpression");
        return tripleExpr;
    }

    // ---- TripleExpression, TripleConstraint

    protected int startTripleExpressionClause() {
        start("TripleExpressionClause");
        return startTripleOp();
    }

    protected void finishTripleExpressionClause(int idx) {
        finishTripleOp(idx, TripleExprEachOf::create);
        finish("TripleExpressionClause");
    }

    protected void startUnaryTripleExpr() {
        start("UnaryTripleExpression");
    }

    protected void finishUnaryTripleExpr() {
        finish("UnaryTripleExpression");
    }

    protected void startBracketedTripleExpr() {
        // Builder?
        start("BracketedTripleExpression");
    }

    protected void finishBracketedTripleExpr(Node label, TripleExpression tripleExpr, Cardinality cardinality) {
        TripleExpression tripleExpr2 = tripleExpr;
        if ( cardinality != null )
            tripleExpr2 = new TripleExprCardinality(tripleExpr, cardinality);
        push(tripleExprStack, tripleExpr2);
        if ( label != null )
            tripleExprRefs.put(label, tripleExpr2);
        finish("BracketedTripleExpression");
    }

    protected int startTripleConstraint() {
        start("TripleConstraint");
        return startShapeOp();
    }

    protected void finishTripleConstraint(Node label, int idx, Node predicate, boolean reverse, Cardinality cardinality) {
        if ( label != null ) { /*ref*/ } // XXX
        List<ShapeExpression> args = finishShapeOp(idx);
        if ( args == null )
            throw new InternalErrorException("TripleConstraint with null argument ShapeExpression.");
        if ( args.size() != 1 )
            throw new InternalErrorException("TripleConstraint with multiple ShapeExpressions");

        ShapeExpression arg = args.get(0);
        // Cardinality as argument.
        TripleExpression tripleExpr = new TripleConstraint(label, predicate, reverse, arg, cardinality);
        push(tripleExprStack, tripleExpr);
        if ( label != null )
            tripleExprRefs.put(label, tripleExpr);
        finish("TripleConstraint");
    }

    // ---- Node Constraints.

    protected int startLiteralNodeConstraint(int line, int column) {
        start("LiteralNodeConstraint");
        return startShapeOp();
    }

    protected void finishLiteralNodeConstraint(int idx, int line, int column) {
        finishShapeOpNoAction("LiteralNodeConstraint", idx);
        finish("LiteralNodeConstraint");
    }

    protected int startNonLiteralNodeConstraint(int line, int column) {
        start("NonLiteralNodeConstraint");
        return startShapeOp();
    }

    protected void finishNonLiteralNodeConstraint(int idx, int line, int column) {
        finishShapeOpNoAction("NonLiteralNodeConstraint", idx);
        finish("NonLiteralNodeConstraint");
    }

    private void addNodeConstraint(NodeConstraint constraint) {
        stack("NodeConstraint: %s", constraint);
        push(shapeExprStack, constraint);
    }

    protected void cDatatype(String str, int line, int column) {
        DatatypeConstraint dt = new DatatypeConstraint(str);
        addNodeConstraint(dt);
    }

    protected void cNodeKind(String nodeKindStr, int line, int column) {
        NodeKind nodeKind = NodeKind.create(nodeKindStr);
        NodeKindConstraint nk = new NodeKindConstraint(nodeKind);
        addNodeConstraint(nk);
    }

    // ----
    // Value Set build

    private List<ValueSetRange> valueSetRanges = new ArrayList<>();

    private ValueSetRange valueSetRange = null;

    protected void startValueSet() {
        start("ValueSet");
    }

    private void accumulateValueSetRange(ValueSetRange vsRange) {
        valueSetRanges.add(vsRange);
    }

    protected void finishValueSet() {
        List<ValueSetRange> x = valueSetRanges;
        valueSetRanges = new ArrayList<>();
        ValueConstraint vc = new ValueConstraint(x);
        push(shapeExprStack, vc);
        finish("ValueSet");
    }

    protected void startValueSetValue() {
        start("ValueSetValue");
    }

    protected void finishValueSetValue() {
        finish("ValueSetValue");
    }

    protected void startValueSetValueDot() {
        valueSetRange = new ValueSetRange(null, null, null, false);
    }

    protected void finishValueSetValueDot() {
        endValueSetValue();
    }

    protected void valueSetIriRange(String iriStr, boolean isStem) {
        setValueSetValue(iriStr, null, null, isStem);
    }

    protected void valueSetLiteralRange(Node literal, boolean isStem) {
        setValueSetValue(null, null, literal, isStem);
    }

    protected void valueSetLanguageRange(String lang, boolean isStem) {
        setValueSetValue(null, lang, null, isStem);
    }

    private void setValueSetValue(String iriStr, String lang, Node literal, boolean isStem) {
        if ( valueSetRange != null )
          throw new InternalErrorException("ValueSet range item already set null");
        valueSetRange = new ValueSetRange(iriStr, langtag(lang), literal, isStem);
    }

    protected void startIriRange() { start("iriRange"); }

    protected void exclusionIriRange(String iriStr, boolean isStem) {
        seenValueExclusion(iriStr, null, null, isStem);
    }

    protected void finishIriRange() { endValueSetValue(); finish("iriRange"); }

    protected void startLiteralRange() { start("literalRange"); }

    protected void exclusionLiteralRange(Node literal, boolean isStem) {
        seenValueExclusion(null, null, literal, isStem);
    }

    protected void finishLiteralRange() { endValueSetValue(); finish("literalRange"); }

    protected void startLanguageRange() { start("languageRange"); }

    protected void exclusionLanguageRange(String lang, boolean isStem) {
        seenValueExclusion(null, lang, null, isStem);
    }

    protected void finishLanguageRange() { endValueSetValue(); finish("languageRange"); }

    protected void endValueSetValue() {
        if ( valueSetRange == null )
            throw new InternalErrorException("valueSetRange range is null");
        accumulateValueSetRange(valueSetRange);
        valueSetRange = null;
    }

    private static String langtag(String lang) {
        if ( lang != null && lang.startsWith("@") )
            lang = lang.substring(1);
        return lang;
    }

    // -- ValueSet any exclusion
    protected void startValueExclusion() { start("valueExclusion"); }
    protected void finishValueExclusion(String iriStr, String lang, Node lit, boolean isStem) {
        seenValueExclusion(iriStr, lang, lit, isStem);
        finish("valueExclusion");
    }

    private void seenValueExclusion(String iriStr, String lang, Node lit, boolean isStem) {
        valueSetRange.exclusions.add(new ValueSetItem(iriStr, langtag(lang), lit, isStem));
    }

    protected Cardinality cardinalityRange(String image, int line, int column) {
        try {
            return Cardinality.create(image);
        } catch (Throwable th) {
            throw new ShexParseException("Bad cardinality: "+image, line, column);
        }
    }

    // Node Constraints.

    protected void numericFacetRange(String range, Node num, int line, int column) {
        NumRangeKind kind = NumRangeKind.create(range);
        NodeConstraint numLength = new NumRangeConstraint(kind, num);
        addNodeConstraint(numLength);
    }

    protected void numericFacetLength(String facetKind, int length, int line, int column) {
        NumLengthKind kind = NumLengthKind.create(facetKind);
        NodeConstraint numLength = new NumLengthConstraint(kind, length);
        addNodeConstraint(numLength);
    }

    // Metacharacters ., ?, *, +, {, } (, ), [ or ].
        // and "|", "$" and "^". These preserve their \.
    //    "\\" [ "n", "r", "t", "\\", "|", "." , "?", "*", "+",
    //           "(", ")", "{", "}", "$", "-", "[", "]", "^", "/"

    protected void stringFacetRegex(String regexStr, int line, int column) {
        int idx = regexStr.lastIndexOf('/');
        String pattern = regexStr.substring(1, idx);
        pattern = EscapeStr.unescapeUnicode(pattern);

        String flags = regexStr.substring(idx+1);
        pattern = ShexParserLib.unescapeShexRegex(pattern, '\\', false);
        NodeConstraint regex = new StrRegexConstraint(pattern, flags);
        addNodeConstraint(regex);
    }

    protected void stringFacetLength(String str, int len) {
        StrLengthKind lengthType = StrLengthKind.create(str);
        NodeConstraint nodeConstraint = StrLengthConstraint.create(lengthType, len);
        addNodeConstraint(nodeConstraint);
    }

    protected Node langStringLiteral(int quoteLen, String image, int line, int column) {
        // Find @ and split.
        int idx = image.lastIndexOf('@');
        if ( idx < 2*quoteLen )
            throw new ShexParseException("Bad langStringLiteral: "+image, line, column);

        String lex = image.substring(quoteLen, idx-quoteLen);
        String lang = image.substring(idx+1);
        lex = unescapeStr(lex, line, column);
        return NodeFactory.createLiteral(lex, lang);
    }

    // Special case @ns: and @ns:foo.
    protected Node resolve_AT_PName(String image, int line, int column) {
        String prefixedName = image.substring(1);
        String iriStr = resolvePName(prefixedName, line, column);
        return createURI(iriStr, line, column);
    }

    protected void ampTripleExprLabel(Node ref) {
        push(tripleExprStack, new TripleExprRef(ref));
    }

    // ---- Stacks
    //    shapeStack
    //    shapeExprStack
    //    tripleConstraints
    //    tripleExpressionClause

    // ---- Node Constraints.

    protected int integer(String image, int line, int column) {
        try {
            return Integer.parseInt(image) ;
        } catch (NumberFormatException ex) {
            throw new ShexParseException(ex.getMessage(), line, column);
        }
    }

    // ---- Node Constraints.

//    // DRY: In ShaclCompactParser as well - to LangParserBase
//    private int integerRange(String str, int i) {
//        if ( str == null || str.equals("*") )
//            return i;
//        try {
//            return Integer.parseInt(str);
//        } catch (NumberFormatException ex) {
//            throw new InternalErrorException("Number format exception");
//        }
//    }

    private <T> T peek(Deque<T> stack) {
        return stack.peek();
    }

    private <T> void push(Deque<T> stack, T item) {
        if ( item == null )
            debug("push-null", item);
        if ( DEBUG_STACK )
            debug("push(%s)", item);
        stack.push(item);
    }

    private <T> T pop(Deque<T> stack) {
        T item = stack.pop();
        if ( DEBUG_STACK )
            debug("pop(%s)", item);
        return item;
    }

    /*
     * Pop the elements at idx to stack front.
     * Return null for "no arguments"
     */
    private <T> List<T> pop(Deque<T> stack, int x) {
        int N = front(stack)-x;
        if ( N == 0 )
            return null;
        // "items" will be "earliest first" order.
        @SuppressWarnings("unchecked")
        T[] items0 = (T[])new Object[N];
        List<T> items = Arrays.asList(items0);
        for(int i = N-1 ; i>=0 ; i-- )
            items.set(i, pop(stack));
        return items;
    }

    private <T> int front(Deque<T> stack) {
        return stack.size();
    }

    // -- Development

    private void start(String label) { start(null, label); }

    private void start(Inline inline, String label) {
        if ( DEBUG_PARSE ) {
            out.print("> ");
            out.print(label);
            if ( inline == INLINE)
                out.print("'");
            out.println();
            out.incIndent();
        }
    }

    private void finish(String label) { finish(null, label); }

    private void finish(Inline inline, String label) {
        if ( DEBUG_PARSE ) {
            out.decIndent();
            out.print("< ");
            out.print(label);
            if ( inline == INLINE)
                out.print("'");
            out.println();
        }
    }

    private void stack(String fmt, Object...args) {
        if ( DEBUG_PARSE ) {
            out.print(String.format(fmt, args));
            out.println();
        }
    }

    private void debug(String fmt, Object...args) {
        if ( DEBUG_DEV ) {
            out.print(String.format(fmt, args));
            out.println();
        }
    }

    private void debugNoIndent(String fmt, Object...args) {
        if ( DEBUG_DEV ) {
            int x = out.getAbsoluteIndent();
            out.setAbsoluteIndent(0);
            out.print(String.format(fmt, args));
            if ( !fmt.endsWith("\n") )
                out.println();
            out.setAbsoluteIndent(x);
        }
    }

    // -- shape expression parser calls. translate to onegeneral one for each category.

    protected int startShapeExpression() { return startShapeExpression(NOT_INLINE); }
    protected void finishShapeExpression(int idx) { finishShapeExpression(NOT_INLINE, idx); }

    protected int startShapeOr() { return startShapeOr(NOT_INLINE); }
    protected void finishShapeOr(int idx) { finishShapeOr(NOT_INLINE, idx); }

    protected int startShapeAnd() { return startShapeAnd(NOT_INLINE); }
    protected void finishShapeAnd(int idx) { finishShapeAnd(NOT_INLINE, idx); }

    protected int startShapeNot() { return startShapeNot(NOT_INLINE); }
    protected void finishShapeNot(int idx, boolean negate) { finishShapeNot(NOT_INLINE, idx, negate); }

    protected int startShapeAtom() { return startShapeAtom(NOT_INLINE); }
    protected void finishShapeAtom(int idx) { finishShapeAtom(NOT_INLINE, idx); }

    protected int startInlineShapeExpression() { return startShapeExpression(INLINE); }
    protected void finishInlineShapeExpression(int idx) { finishShapeExpression(INLINE, idx); }

    protected int startInlineShapeOr() { return startShapeOr(INLINE); }
    protected void finishInlineShapeOr(int idx) { finishShapeOr(INLINE, idx); }

    protected int startInlineShapeAnd() { return startShapeAnd(INLINE); }
    protected void finishInlineShapeAnd(int idx) { finishShapeAnd(INLINE, idx); }

    protected int startInlineShapeNot() { return startShapeNot(INLINE); }
    protected void finishInlineShapeNot(int idx, boolean negate) { finishShapeNot(INLINE, idx, negate); }

    protected int startInlineShapeAtom() { return startShapeAtom(INLINE); }
    protected void finishInlineShapeAtom(int idx) { finishShapeAtom(INLINE, idx); }

    // ---- Shex Shape map

    private List<ShexRecord> associations = new ArrayList<>();

    public void parseShapeMapStart() {}

    public ShexMap parseShapeMapFinish() {
        return ShexMap.create(associations);
    }

    protected Triple createTriple(Node s, Node p, Node o, int line, int column) {
        s = nullToAny(s);
        p = nullToAny(p);
        o = nullToAny(o);
        return Triple.create(s, p, o);
    }

    protected void association(Node n, Triple t, Node label) {
        if ( n != null && t != null )
            throw new ShexParseException("Both node and triple in shape association");
        ShexRecord assoc;
        if ( n != null )
            assoc = new ShexRecord(n, label);
        else if ( t != null )
            assoc = new ShexRecord(t, label);
        else
            throw new ShexParseException("No node nor triple in shape association");
        associations.add(assoc);
    }
}
