/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.query;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.core.QueryCompare;
import com.hp.hpl.jena.sparql.core.QueryHashCode;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarAlloc;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.E_Aggregator;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.aggregate.AggregateFactory;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.serializer.Serializer;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

/** The data structure for a query as presented externally.
 *  There are two ways of creating a query - use the parser to turn
 *  a string description of the query into the executable form, and
 *  the programmatic way (the parser is calling the programmatic
 *  operations driven by the quyery string).  The declarative approach
 *  of passing in a string is preferred.
 *
 * Once a query is built, it can be passed to the QueryFactory to produce a query execution engine.
 * @see QueryExecutionFactory
 * @see ResultSet
 * 
 * @author		Andy Seaborne
 */

public class Query extends Prologue implements Cloneable
{
    static { ARQ.init() ; /* Ensure everything has started properly */ }
    
    public static final int QueryTypeUnknown    = -123 ;
    public static final int QueryTypeSelect     = 111 ;
    public static final int QueryTypeConstruct  = 222 ;
    public static final int QueryTypeDescribe   = 333 ;
    public static final int QueryTypeAsk        = 444 ;
    int queryType = QueryTypeUnknown ; 
    
    // If no model is provided explicitly, the query engine will load
    // a model from the URL.  Never a list of zero items.
    
    List<String> graphURIs = new ArrayList<String>() ;
    List<String> namedGraphURIs = new ArrayList<String>() ;
    
    // The WHERE clause
    Element queryPattern = null ;
    
    // Query syntax
    Syntax syntax = Syntax.syntaxSPARQL ; // Default
    
    // LIMIT/OFFSET
    public static final long  NOLIMIT = Long.MIN_VALUE ;
    long resultLimit   = NOLIMIT ;
    long resultOffset  = NOLIMIT ;
    
    // ORDER BY
    List<SortCondition> orderBy       = null ;
    public static final int ORDER_ASCENDING           = 1 ; 
    public static final int ORDER_DESCENDING          = -1 ;
    public static final int ORDER_DEFAULT             = -2 ;    // Not explicitly given. 
    public static final int ORDER_UNKNOW              = -3 ; 

    boolean strictQuery = true ;
    
    // SELECT * / CONSTRUCT * or DESCRIBE * seen
    protected boolean queryResultStar        = false ;
    
    protected boolean distinct               = false ;
    protected boolean reduced                = false ;
    
    // CONSTRUCT
    protected Template constructTemplate  = null ;
    
    // DESCRIBE
    // Any URIs/QNames in the DESCRIBE clause
    // Also uses resultVars
    protected List<Node> resultNodes               = new ArrayList<Node>() ;     // Type in list: Node
    
    public Query()
    {
        syntax = Syntax.syntaxSPARQL ;
    }
    
    public Query(Prologue prologue)
    {
        this() ;
        usePrologueFrom(prologue) ;
    }
    
    // Allocate variables that are unique to this query.
    private VarAlloc varAlloc = new VarAlloc(ARQConstants.allocVarMarker) ;
    private Var allocInternVar() { return varAlloc.allocVar() ; }
    
    //private VarAlloc varAnonAlloc = new VarAlloc(ARQConstants.allocVarAnonMarker) ;
    //public Var allocVarAnon() { return varAnonAlloc.allocVar() ; }
    
    
    public void setQueryType(int qType)         { queryType = qType ; }
    
    public void setQuerySelectType()            { setQueryType(QueryTypeSelect) ; }
    public void setQueryConstructType()         { setQueryType(QueryTypeConstruct) ; }
    public void setQueryDescribeType()          { setQueryType(QueryTypeDescribe) ; }
    public void setQueryAskType()               { setQueryType(QueryTypeAsk) ; }
    
    public int getQueryType()                   { return queryType ; }
    
    public boolean isSelectType()      { return queryType == QueryTypeSelect ; }

    public boolean isConstructType()   { return queryType == QueryTypeConstruct ; }

    public boolean isDescribeType()    { return queryType == QueryTypeDescribe ; }

    public boolean isAskType()         { return queryType == QueryTypeAsk ; }

    public boolean isUnknownType()     { return queryType == QueryTypeUnknown ; }

    public void setStrict(boolean isStrict)
    { 
        strictQuery = isStrict ;
        
        if ( strictQuery )
            initStrict() ;
        else
            initLax() ;
    }
    
    public boolean isStrict()                { return strictQuery ; }
    
    private void initStrict()
    {
//        if ( prefixMap.getGlobalPrefixMapping() == globalPrefixMap )
//            prefixMap.setGlobalPrefixMapping(null) ;
    }

    private void initLax()
    {
//        if ( prefixMap.getGlobalPrefixMapping() == null )
//            prefixMap.setGlobalPrefixMapping(globalPrefixMap) ;
    }
    
    public void setDistinct(boolean b) { distinct = b ; }
    public boolean isDistinct()        { return distinct ; }
    
    public void setReduced(boolean b) { reduced = b ; }
    public boolean isReduced()        { return reduced ; }

    /** @return Returns the syntax. */
    public Syntax getSyntax()         { return syntax ; }

    /** @param syntax The syntax to set. */
    public void setSyntax(Syntax syntax)
    { 
        this.syntax = syntax ;
        if ( syntax != Syntax.syntaxSPARQL )
            strictQuery = false ;
    }

    // ---- Limit/offset
    
    public long getLimit()             { return resultLimit ; } 
    public void setLimit(long limit)   { resultLimit = limit ; }
    public boolean hasLimit()          { return resultLimit != NOLIMIT ; }
    
    public long getOffset()            { return resultOffset ; } 
    public void setOffset(long offset) { resultOffset = offset ; }
    public boolean hasOffset()         { return resultOffset != NOLIMIT ; }
    
    // ---- Order By
    
    public boolean hasOrderBy()        { return orderBy != null && orderBy.size() > 0 ; }
    
    public void addOrderBy(SortCondition condition)
    {
        if ( orderBy == null )
            orderBy = new ArrayList<SortCondition>() ;

        orderBy.add(condition) ;
    }
    public void addOrderBy(Expr expr, int direction)
    {
        SortCondition sc = new SortCondition(expr, direction) ;
        addOrderBy(sc) ;
    }
    
    public void addOrderBy(Node var, int direction)
    { 
        if ( ! var.isVariable() )
            throw new QueryException("Not a variable: "+var) ;
        SortCondition sc = new SortCondition(var, direction) ;
        addOrderBy(sc) ;
    }
    
    public void addOrderBy(String varName, int direction)
    { 
        varName = Var.canonical(varName) ;
        SortCondition sc = new SortCondition(new ExprVar(varName), direction) ;
        addOrderBy(sc) ;
    }

    public List<SortCondition> getOrderBy()           { return orderBy ; }
    
    // ---- 
    
    /** Answer whether the query had SELECT/DESCRIBE/CONSTRUCT *
     * @return boolean as to whether a * result form was seen
     */ 
    public boolean isQueryResultStar() { return queryResultStar ; }

    /**Set whether the query had SELECT/DESCRIBE/CONSTRUCT *
     * 
     * @param isQueryStar 
     */
    public void setQueryResultStar(boolean isQueryStar) { queryResultStar = isQueryStar ; }
    
    public void setQueryPattern(Element elt)
    {
        queryPattern = elt ;
//        if ( queryBlock == null )
//            queryBlock = new ElementBlock(null, null) ;
//        queryBlock.setPatternElement(elt) ;
    }
    
    public Element getQueryPattern() { return queryPattern ; }
    
     /** Location of the source for the data.  If the model is not set,
     *  then the QueryEngine will attempt to load the data from these URIs
     *  into the default (unamed) graph.
     */
    public void addGraphURI(String s)
    {
        if ( graphURIs == null )
            graphURIs = new ArrayList<String>() ;
        graphURIs.add(s) ;
    }

    /** Location of the source for the data.  If the model is not set,
     *  then the QueryEngine will attempt to load the data from these URIs
     *  as named graphs in the dataset.
     */
    public void addNamedGraphURI(String uri)
    {
        if ( namedGraphURIs == null )
            namedGraphURIs = new ArrayList<String>() ;
        if ( namedGraphURIs.contains(uri) )
            throw new QueryException("URI already in named graph set: "+uri) ;
        else
            namedGraphURIs.add(uri) ;
    }
    
    /** Return the list of URIs (strings) for the unnamed graph
     * 
     * @return List of strings
     */
    
    public List<String> getGraphURIs() { return graphURIs ; }

    /** Test whether the query mentions a URI in forming the default graph (FROM clause) 
     * 
     * @param uri
     * @return boolean  True if the URI used in a FROM clause 
     */
    public boolean usesGraphURI(String uri) { return graphURIs.contains(uri) ; }
    
    /** Return the list of URIs (strings) for the named graphs (FROM NAMED clause)
     * 
     * @return List of strings
     */
    
    public List<String> getNamedGraphURIs() { return namedGraphURIs ; }

    /** Test whether the query mentions a URI for a named graph. 
     * 
     * @param uri
     * @return True if the URI used in a FROM NAMED clause 
     */
    public boolean usesNamedGraphURI(String uri) { return namedGraphURIs.contains(uri) ; }
    
    /** Return true if the query has either some graph
     * URIs or some named graph URIs in its description.
     * This does not mean these URIs will be used - just that
     * they are noted as part of the query. 
     */ 
    
    public boolean hasDatasetDescription()
    {
        if ( getGraphURIs() != null && getGraphURIs().size() > 0 )
            return true ;
        if ( getNamedGraphURIs() != null && getNamedGraphURIs().size() > 0 )
            return true ;
        return false ;
    }
    
    // ---- SELECT

    protected VarExprList projectVars = new VarExprList() ;
//    protected Map resultExprs = new HashMap() ;
//    protected List resultVars = new ArrayList() ;

    /** Return a list of the variables requested (SELECT) */
    public List<String> getResultVars()
    { 
        // Ensure "SELECT *" processed
        setResultVars() ;
        return Var.varNames(projectVars.getVars()) ;
    }
    
    public VarExprList getProject()
    {
        return projectVars ;
    }
    
    /** Add a collection of projection variables to a SELECT query */
    public void addProjectVars(Collection<?> vars)
    {
        for ( Iterator<?> iter = vars.iterator() ; iter.hasNext() ; )
        {
            Object obj = iter.next();
            if ( obj instanceof String )
            {
                this.addResultVar((String)obj) ;
                continue ;
            }
            if ( obj instanceof Var )
            {
                this.addResultVar((Var)obj) ;
                continue ;
            }
            throw new QueryException("Not a variable or variable name: "+obj) ;
        }
        resultVarsSet = true ;
    }

    
    /** Add a projection variable to a SELECT query */
    public void addResultVar(String varName)
    {
        varName = Var.canonical(varName) ;
        _addResultVar(varName) ;
    }

    public void addResultVar(Node v)
    {
        if ( !v.isVariable() )
            throw new QueryException("Not a variable: "+v) ;
        _addResultVar(v.getName()) ;
    }
    
    // Add raw name.
    private void _addResultVar(String varName)
    {
        projectVars.add(Var.alloc(varName)) ;
        resultVarsSet = true ;
    }
    
    public void addResultVar(Node v, Expr expr)
    {
        Var var = null ; 
        if ( v == null )
            var = allocInternVar() ;
        else
        {
            if ( !v.isVariable() )
                throw new QueryException("Not a variable: "+v) ;
            var = Var.alloc(v) ;
        }
        _addResultVar(var, expr) ;
    }
    
    /** Add an to a SELECT query (a name will be created for it) */
    public void addResultVar(Expr expr)
    {
        _addResultVar(allocInternVar(), expr) ;
    }

    /** Add a named expression to a SELECT query */
    public void addResultVar(String varName, Expr expr)
    {
        Var var = null ; 
        if ( varName == null )
            var = allocInternVar() ;
        else
        {
            varName = Var.canonical(varName) ;
            var = Var.alloc(varName) ;
        }
        _addResultVar(var, expr) ;
    }

    private void _addResultVar(Var var, Expr expr)
    {
        projectVars.add(var, expr) ;
    }
    
    // GROUP/HAVING
    
    protected VarExprList groupVars = new VarExprList() ;
    protected List<Expr> havingExprs = new ArrayList<Expr>() ;  // Expressions : Make an ExprList?
    
    public boolean hasGroupBy()     { return ! groupVars.isEmpty() ; }
    public boolean hasHaving()      { return havingExprs != null && havingExprs.size() > 0 ; }
    
    public VarExprList getGroupBy()      { return groupVars ; }
    
    public List<Expr> getHavingExprs()    { return havingExprs ; }
    
    public void addGroupBy(String varName)
    {
        varName = Var.canonical(varName) ;
        groupVars.add(Var.alloc(varName)) ;
    }

    public void addGroupBy(Node v)
    {
        groupVars.add(Var.alloc(v)) ;
    }

    public void addGroupBy(Expr expr) { addGroupBy(null, expr) ; }
    
    public void addGroupBy(Var v, Expr expr)
    {
        if ( v == null )
            v = allocInternVar() ;
        
        if ( expr.isVariable() && v.isAllocVar() )
        {
            // It was (?x) with no AS - keep the name by adding by variable.
            addGroupBy(expr.asVar()) ;
            return ;
        }
        
        groupVars.add(v, expr) ;
    }

    public void addHavingCondition(Expr expr)
    {
        havingExprs.add(expr) ;
    }

    // ---- Aggregates

    // Record allocated aggregations.
    // Later: The same aggregation expression used in a query 
    // will always lead to the same aggregator.
    // For now, allocate a fresh one each time (cause the calcutation
    // to be done multiple times but (1) it's unusual to have repeated 
    // aggregators normally and (2) the actual calculation is cheap. 
        
    // Unlike SELECT expressions, here the expression itself (E_Aggregator) knows its variable
    // Commonality?
    
    private List<E_Aggregator> aggregators = new ArrayList<E_Aggregator>() ;
    // Note any E_Aggregator created for reuse.
    private Map<String, E_Aggregator> aggregatorsAllocated = new HashMap<String, E_Aggregator>() ; 
    
    public boolean hasAggregators() { return aggregators.size() != 0  ; }
    public List<E_Aggregator> getAggregators() { return aggregators ; }
    
    public E_Aggregator allocAggregate(AggregateFactory agg)
    {
        Aggregator a = agg.create() ;
        String key = a.key() ;
        E_Aggregator expr = aggregatorsAllocated.get(key); 
        
        if ( expr == null )
        {
            // Not see before.  Build the expression. 
            Var v = allocInternVar() ;
            expr = new E_Aggregator(v, agg.create()) ;
            aggregatorsAllocated.put(key, expr) ;
            aggregators.add(expr) ;
        }
        return expr ;
    }
    
    // ---- CONSTRUCT 
    
    /** Get the template pattern for a construct query */ 
    public Template getConstructTemplate() 
    { 
        return constructTemplate ;
    
    }
    
    /** Set triple patterns for a construct query */ 
    public void setConstructTemplate(Template templ)  { constructTemplate = templ ; }

    // ---- DESCRIBE
    
    public void addDescribeNode(Node node)
    {
        if ( node.isVariable() ) { addResultVar(node) ; return ; }
        if ( node.isURI() || node.isBlank() )
        {
            if ( !resultNodes.contains(node) )
                resultNodes.add(node);
            return ;
        }
        if ( node.isLiteral() )
            throw new QueryException("Result node is a literal: "+FmtUtils.stringForNode(node)) ;
        throw new QueryException("Result node not recognized: "+node) ;
    }

    
    /** Get the result list (things wanted - not the results themselves)
     *  of a DESCRIBE query. */ 
    public List<Node> getResultURIs() { return resultNodes ; }
    
    private boolean resultVarsSet = false ; 
    /** Fix up when the query has "*" (when SELECT * or DESCRIBE *)
     *  and for a construct query.  This operation is idempotent.
     */
    public void setResultVars()
    {
        if ( resultVarsSet )
            return ;
        resultVarsSet = true ;
        
        if ( getQueryPattern() == null )
        {
            if ( ! this.isDescribeType() )
                ALog.warn(this, "setResultVars(): no query pattern") ;
            return ;
        }
        
        // May have been added via addResultVar(,Expr)
//        if ( resultVars.size() != 0 ) 
//            return ;
        
        if ( isSelectType() )
        {
            if ( isQueryResultStar() )
                findAndAddNamedVars() ;
            return ;
        }
        
        if ( isConstructType() )
        {
            // All named variables are in-scope
            findAndAddNamedVars() ;
            return ;
        }
        
        if ( isDescribeType() )
        {
            if ( isQueryResultStar() )
                findAndAddNamedVars() ;
            return ;
        }

//        if ( isAskType() )
//        {}
    }
    
    private void findAndAddNamedVars()
    {
        Iterator<Var> varIter = null ;
        if ( hasGroupBy() )
            varIter = groupVars.getVars().iterator() ;
        else
        {
            Set<Var> queryVars = this.getQueryPattern().varsMentioned() ;
            varIter = queryVars.iterator() ;
        }
        
        // All query variables, including ones from bNodes in the query.
        
        for ( ; varIter.hasNext() ; )
        {
            Object obj = varIter.next() ;
            //Var var = (Var)iter.next() ;
            Var var = (Var)obj ;
            if ( var.isNamedVar() )
                addResultVar(var) ;
        }
    }

    public void visit(QueryVisitor visitor)
    {
        visitor.startVisit(this) ;
        visitor.visitResultForm(this) ;
        visitor.visitPrologue(this) ;
        if ( this.isSelectType() )
            visitor.visitSelectResultForm(this) ;
        if ( this.isConstructType() )
            visitor.visitConstructResultForm(this) ;
        if ( this.isDescribeType() )
            visitor.visitDescribeResultForm(this) ;
        if ( this.isAskType() )
            visitor.visitAskResultForm(this) ;
        visitor.visitDatasetDecl(this) ;
        visitor.visitQueryPattern(this) ;
        visitor.visitGroupBy(this) ;
        visitor.visitHaving(this) ;
        visitor.visitOrderBy(this) ;
        visitor.visitOffset(this) ;
        visitor.visitLimit(this) ;
        visitor.finishVisit(this) ;
    }

    @Override
    public Object clone() { return cloneQuery() ; }
    
    public Query cloneQuery()
    {
        // A little crude.
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        serialize(buff, getSyntax()) ;
        String qs = buff.toString() ;
        return QueryFactory.create(qs, getSyntax()) ;
    }
    
    // ---- Query canonical syntax
    
    // Reverse of parsing : should produce a string that parses to an equivalent query
    // "Equivalent" => gives the same results on any model  
    @Override
    public String toString()
    { return serialize() ; }
    
    public String toString(Syntax syntax)
    { return serialize(syntax) ; }

    
    /** Must align with .equals */
    private int hashcode = -1 ;
    
    /** Perform some check on the query */ 
    public void validate()
    {
        setResultVars() ;
        
        if ( hasGroupBy() )
        {}
        
        if ( hasAggregators() )
        {}
        
        //if has select expressions
    }

    @Override
    public int hashCode()
    { 
        if ( hashcode == -1 )
        {
            hashcode = QueryHashCode.calc(this) ;
            if ( hashcode == -1 )
                hashcode = Integer.MIN_VALUE/2 ;
        }
        return hashcode ;
    }
    
    /** Are two queries equals - tests shape and details.
     * Equality means that the queries do the same thing, including
     * same variables, in the same places.  Being unequals does
     * <b>not</b> mean the queries do different things.  
     * 
     * For example, reordering a group or union
     * means that that a query is different.
     *  
     * Two instances of a query parsed from the same string are equal. 
     */
    
    @Override
    public boolean equals(Object other)
    { 
        if ( ! ( other instanceof Query ) )
            return false ;
        if ( this == other ) return true ;
        return QueryCompare.equals(this, (Query)other) ;
    }
    
//    public static boolean sameAs(Query query1, Query query2)
//    { return query1.sameAs(query2) ; }  

    /** Convert the query to a string */
    
    public String serialize()
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        serialize(buff) ;
        return buff.toString();
    }
    
    /** Convert the query to a string in the given syntax
     * @param syntax
     */
    
    public String serialize(Syntax syntax)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        serialize(buff, syntax) ;
        return buff.toString();
    }

    /** Output the query
     * @param out  OutputStream
     */
    public void serialize(OutputStream out) { Serializer.serialize(this, out) ; }
    
    /** Output the query
     * 
     * @param out     OutputStream
     * @param syntax  Syntax URI
     */
    
    public void serialize(OutputStream out, Syntax syntax) { Serializer.serialize(this, out, syntax) ; }

    /** Format the query into the buffer
     * 
     * @param buff    IndentedLineBuffer
     */
    
    public void serialize(IndentedLineBuffer buff) { Serializer.serialize(this, buff) ; }
    
    /** Format the query
     * 
     * @param buff       IndentedLineBuffer in which to place the unparsed query
     * @param outSyntax  Syntax URI
     */
    
    public void serialize(IndentedLineBuffer buff, Syntax outSyntax) { Serializer.serialize(this, buff, outSyntax) ; }

    /** Format the query
     * 
     * @param writer  IndentedWriter
     */
    
    public void serialize(IndentedWriter writer) { Serializer.serialize(this, writer) ; }

    /** Format the query
     * 
     * @param writer     IndentedWriter
     * @param outSyntax  Syntax URI
     */
    
    public void serialize(IndentedWriter writer, Syntax outSyntax)
    {
        Serializer.serialize(this, writer, outSyntax) ;
    }
}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
