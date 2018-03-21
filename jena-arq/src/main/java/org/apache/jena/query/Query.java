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

package org.apache.jena.query;

import java.io.OutputStream ;
import java.util.* ;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.Printable ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.algebra.table.TableData ;
import org.apache.jena.sparql.core.* ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprAggregator ;
import org.apache.jena.sparql.expr.ExprVar ;
import org.apache.jena.sparql.expr.aggregate.Aggregator ;
import org.apache.jena.sparql.serializer.QuerySerializerFactory ;
import org.apache.jena.sparql.serializer.SerializerRegistry ;
import org.apache.jena.sparql.syntax.Element ;
import org.apache.jena.sparql.syntax.PatternVars ;
import org.apache.jena.sparql.syntax.Template ;
import org.apache.jena.sparql.util.FmtUtils ;
import org.apache.jena.sys.JenaSystem ;

/** The data structure for a query as presented externally.
 *  There are two ways of creating a query - use the parser to turn
 *  a string description of the query into the executable form, and
 *  the programmatic way (the parser is calling the programmatic
 *  operations driven by the query string).  The declarative approach
 *  of passing in a string is preferred.
 *
 * Once a query is built, it can be passed to the QueryFactory to produce a query execution engine.
 * @see QueryExecutionFactory
 * @see ResultSet */

public class Query extends Prologue implements Cloneable, Printable
{
    static { JenaSystem.init() ; /* Ensure everything has started properly */ }
    
    public static final int QueryTypeUnknown    = -123 ;
    public static final int QueryTypeSelect     = 111 ;
    public static final int QueryTypeConstruct  = 222 ;
    public static final int QueryTypeDescribe   = 333 ;
    public static final int QueryTypeAsk        = 444 ;
    public static final int QueryTypeJson       = 555 ;
    int queryType = QueryTypeUnknown ; 
    
    // If no model is provided explicitly, the query engine will load
    // a model from the URL.  Never a list of zero items.
    
    private List<String> graphURIs = new ArrayList<>() ;
    private List<String> namedGraphURIs = new ArrayList<>() ;
    
    // The WHERE clause
    private Element queryPattern = null ;
    
    // Query syntax
    private Syntax syntax = Syntax.syntaxSPARQL ; // Default
    
    // LIMIT/OFFSET
    public static final long  NOLIMIT = Long.MIN_VALUE ;
    private long resultLimit   = NOLIMIT ;
    private long resultOffset  = NOLIMIT ;
    
    // ORDER BY
    private List<SortCondition> orderBy       = null ;
    public static final int ORDER_ASCENDING           = 1 ; 
    public static final int ORDER_DESCENDING          = -1 ;
    public static final int ORDER_DEFAULT             = -2 ;    // Not explicitly given. 
    public static final int ORDER_UNKNOW              = -3 ; 

    // VALUES trailing clause
    protected TableData valuesDataBlock = null ;
    
    protected boolean strictQuery = true ;
    
    // SELECT * seen
    protected boolean queryResultStar        = false ;
    
    protected boolean distinct               = false ;
    protected boolean reduced                = false ;
    
    // CONSTRUCT
    protected Template constructTemplate  = null ;
    
    // DESCRIBE
    // Any URIs/QNames in the DESCRIBE clause
    // Also uses resultVars
    protected List<Node> resultNodes               = new ArrayList<>() ;     // Type in list: Node
    
    /**
     * Creates a new empty query
     */
    public Query()
    {
        syntax = Syntax.syntaxSPARQL ;
    }
    
    /**
     * Creates a new empty query with the given prologue
     */
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
    
    public void setQuerySelectType()            { queryType = QueryTypeSelect ; }
    public void setQueryConstructType()         { queryType = QueryTypeConstruct ; queryResultStar = true ; }
    public void setQueryDescribeType()          { queryType = QueryTypeDescribe ; }
    public void setQueryAskType()               { queryType = QueryTypeAsk ; }
    public void setQueryJsonType()              { queryType = QueryTypeJson ; }
    
    public int getQueryType()                   { return queryType ; }
    
    public boolean isSelectType()               { return queryType == QueryTypeSelect ; }

    public boolean isConstructType()            { return queryType == QueryTypeConstruct ; }

    public boolean isDescribeType()             { return queryType == QueryTypeDescribe ; }

    public boolean isAskType()                  { return queryType == QueryTypeAsk ; }

    public boolean isJsonType()                 { return queryType == QueryTypeJson ; }

    public boolean isUnknownType()              { return queryType == QueryTypeUnknown ; }
    
    public boolean isConstructQuad()            { return isConstructType() && constructTemplate.containsRealQuad() ; }
    // It was a mistake to extend Prologue ... but what is done is done.
    public Prologue getPrologue()               { return this ; }
    
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
    
    public boolean isOrdered()         { return hasOrderBy() ; }

    public void addOrderBy(SortCondition condition)
    {
        if ( orderBy == null )
            orderBy = new ArrayList<>() ;

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

    /** Set whether the query had SELECT/DESCRIBE *
     * 
     * @param isQueryStar 
     */
    public void setQueryResultStar(boolean isQueryStar)
    {
        queryResultStar = isQueryStar ;
        if ( isQueryStar ) 
            resultVarsSet = false ;
    }
    
    public void setQueryPattern(Element elt)
    {
        queryPattern = elt ;
    }
    
    public Element getQueryPattern() { return queryPattern ; }
    
     /** Location of the source for the data.  If the model is not set,
     *  then the QueryEngine will attempt to load the data from these URIs
     *  into the default (unamed) graph.
     */
    public void addGraphURI(String s)
    {
        if ( graphURIs == null )
            graphURIs = new ArrayList<>() ;
        graphURIs.add(s) ;
    }

    /** Location of the source for the data.  If the model is not set,
     *  then the QueryEngine will attempt to load the data from these URIs
     *  as named graphs in the dataset.
     */
    public void addNamedGraphURI(String uri)
    {
        if ( namedGraphURIs == null )
            namedGraphURIs = new ArrayList<>() ;
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
    
    /** Return a dataset description (FROM/FROM NAMED clauses) for the query. */  
    public DatasetDescription getDatasetDescription()
    {
        if ( ! hasDatasetDescription() ) return null;
        
        DatasetDescription description = new DatasetDescription() ;
        
        description.addAllDefaultGraphURIs(getGraphURIs()) ;
        description.addAllNamedGraphURIs(getNamedGraphURIs()) ;
        return description ;
    }
    
    // ---- SELECT

    protected VarExprList projectVars = new VarExprList() ;

    /** Return a list of the variables requested (SELECT) */
    public List<String> getResultVars()
    { 
        // Ensure "SELECT *" processed
        setResultVars() ;
        return Var.varNames(projectVars.getVars()) ;
    }
    
    /** Return a list of the variables requested (SELECT) */
    public List<Var> getProjectVars()
    { 
        // Ensure "SELECT *" processed
        setResultVars() ;
        return projectVars.getVars() ;
    }
    
    public VarExprList getProject()
    {
        return projectVars ;
    }
    
    /** Add a collection of projection variables to a SELECT query */
    public void addProjectVars(Collection<?> vars)
    {
        for ( Object obj : vars )
        {
            if ( obj instanceof String )
            {
                this.addResultVar( (String) obj );
                continue;
            }
            if ( obj instanceof Var )
            {
                this.addResultVar( (Var) obj );
                continue;
            }
            throw new QueryException( "Not a variable or variable name: " + obj );
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
        _addVarExpr(projectVars, var, expr) ;
    }
    
    /** Add an to a SELECT query (a name will be created for it) */
    public void addResultVar(Expr expr)
    {
        _addVarExpr(projectVars, allocInternVar(), expr) ;
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
        _addVarExpr(projectVars, var, expr) ;
    }

    // Add raw name.
    private void _addResultVar(String varName)
    {
        Var v = Var.alloc(varName) ;
        _addVar(projectVars, v) ;
        resultVarsSet = true ;
    }

    private static void _addVar(VarExprList varExprList, Var v)
    {
        if ( varExprList.contains(v) )
        {
            Expr expr = varExprList.getExpr(v) ;
            if ( expr != null )
                
                // SELECT (?a+?b AS ?x) ?x
                throw new QueryBuildException("Duplicate variable (had an expression) in result projection '"+v+"'") ;
            // SELECT ?x ?x
            if ( ! ARQ.allowDuplicateSelectColumns )
                return ;
            // else drop thorugh and have two variables of the same name.
        }
        varExprList.add(v) ;
    }

    private static void _addVarExpr(VarExprList varExprList, Var v, Expr expr)
    {
        if ( varExprList.contains(v) )
            // SELECT ?x (?a+?b AS ?x)
            // SELECT (2*?a AS ?x) (?a+?b AS ?x)
            throw new QueryBuildException("Duplicate variable in result projection '"+v+"'") ;  
        varExprList.add(v, expr) ;
    }

    protected VarExprList groupVars = new VarExprList() ;
    protected List<Expr> havingExprs = new ArrayList<>() ;  // Expressions : Make an ExprList?
    
    public boolean hasGroupBy()     { return ! groupVars.isEmpty() || getAggregators().size() > 0 ; }
    public boolean hasHaving()      { return havingExprs != null && havingExprs.size() > 0 ; }
    
    public VarExprList getGroupBy()      { return groupVars ; }
    
    public List<Expr> getHavingExprs()    { return havingExprs ; }
    
    public void addGroupBy(String varName)
    {
        varName = Var.canonical(varName) ;
        addGroupBy(Var.alloc(varName)) ;
    }

    public void addGroupBy(Node v)
    {
        _addVar(groupVars, Var.alloc(v)) ;
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

    // SELECT JSON

    private Map<String, Object> jsonMapping = new LinkedHashMap<>();

    public void addJsonMapping(String key, Object value) {
        jsonMapping.put(key, value);
    }

    public Map<String, Object> getJsonMapping() {
        return Collections.unmodifiableMap(jsonMapping);
    }

    // ---- Aggregates

    // Record allocated aggregations.
    // Later: The same aggregation expression used in a query 
    // will always lead to the same aggregator.
    // For now, allocate a fresh one each time (cause the calculation
    // to be done multiple times but (1) it's unusual to have repeated 
    // aggregators normally and (2) the actual calculation is cheap. 
        
    // Unlike SELECT expressions, here the expression itself (E_Aggregator) knows its variable
    // Commonality?
    
    private List<ExprAggregator> aggregators = new ArrayList<>() ;
    private Map<Var, ExprAggregator> aggregatorsMap = new HashMap<>() ;
    
    // Note any E_Aggregator created for reuse.
    private Map<String, Var> aggregatorsAllocated = new HashMap<>() ;
    
    public boolean hasAggregators() { return aggregators.size() != 0  ; }
    public List<ExprAggregator> getAggregators() { return aggregators ; }
    
    public Expr allocAggregate(Aggregator agg)
    {
        // We need to track the aggregators in case one aggregator is used twice, e.g. in HAVING and in SELECT expression
        // (is that much harm to do twice?  Yes, if distinct.)
        String key = agg.key() ;
        
        Var v = aggregatorsAllocated.get(key); 
        if ( v != null )
        {
            ExprAggregator eAgg = aggregatorsMap.get(v) ; 
            if ( ! agg.equals(eAgg.getAggregator()) )
                Log.warn(Query.class, "Internal inconsistency: Aggregator: "+agg) ;
            return eAgg ;
        }
        // Allocate.
        v = allocInternVar() ;
        ExprAggregator aggExpr = new ExprAggregator(v, agg) ;
        aggregatorsAllocated.put(key, v) ;
        aggregatorsMap.put(v, aggExpr) ;
        aggregators.add(aggExpr) ;
        return aggExpr ;
    }
    
    // ---- VALUES
    
    /** Does the query have a VALUES trailing block? */
    public boolean hasValues()                { return valuesDataBlock != null ; }
    
    public List<Var> getValuesVariables()     { return valuesDataBlock==null ? null : valuesDataBlock.getVars() ; }
    
    /** VALUES data - null for a Node means undef */ 
    public List<Binding> getValuesData()      { return valuesDataBlock==null ? null : valuesDataBlock.getRows() ; }

    public void setValuesDataBlock(List<Var> variables, List<Binding> values)
    {
        checkDataBlock(variables, values) ;
        valuesDataBlock = new TableData(variables, values) ;
    }
    
    private static void checkDataBlock(List<Var> variables, List<Binding> values)
    {
        // Check.
        int N = variables.size() ;
        for ( Binding valueRow : values )
        {
            Iterator<Var> iter= valueRow.vars() ;
            for ( ; iter.hasNext() ; )
            { 
                Var v = iter.next() ;
                if ( ! variables.contains(v) )
                    throw new QueryBuildException("Variable "+v+" not found in "+variables) ;
            }
        }
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
                Log.warn(this, "setResultVars(): no query pattern") ;
            return ;
        }
        
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
            // Binding variables -- in patterns, not in filters and not in EXISTS
            LinkedHashSet<Var> queryVars = new LinkedHashSet<>() ;
            PatternVars.vars(queryVars, this.getQueryPattern()) ;
            if ( this.hasValues() )
                queryVars.addAll(getValuesVariables()) ;
//            if ( this.hasValues() )
//                queryVars.addAll(getValuesVariables()) ;
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
        if ( this.isJsonType() )
            visitor.visitJsonResultForm(this) ;
        visitor.visitDatasetDecl(this) ;
        visitor.visitQueryPattern(this) ;
        visitor.visitGroupBy(this) ;
        visitor.visitHaving(this) ;
        visitor.visitOrderBy(this) ;
        visitor.visitOffset(this) ;
        visitor.visitLimit(this) ;
        visitor.visitValues(this) ;
        visitor.finishVisit(this) ;
    }

    @Override
    public Object clone() { return cloneQuery() ; }
    
    /**
     * Makes a copy of this query.  Copies by parsing a query from the serialized form of this query
     * @return Copy of this query
     */
    public Query cloneQuery() {
        // A little crude.
        // Must use toString() rather than serialize() because we may not know how to serialize extended syntaxes
        String qs = this.toString();
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
     * means that a query is different.
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

    @Override
    public void output(IndentedWriter out)
    {
        serialize(out) ;
    }

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
    public void serialize(OutputStream out) { serialize(out, syntax); }
    
    /** Output the query
     * 
     * @param out     OutputStream
     * @param syntax  Syntax URI
     */
    
    public void serialize(OutputStream out, Syntax syntax) { 
        IndentedWriter writer = new IndentedWriter(out) ;
        serialize(writer, syntax) ;
        writer.flush() ;
        try { out.flush() ; } catch (Exception ex) { } 
    }

    /** Format the query into the buffer
     * 
     * @param buff    IndentedLineBuffer
     */
    
    public void serialize(IndentedLineBuffer buff) { 
        serialize(buff, syntax); 
    }
    
    /** Format the query
     * 
     * @param buff       IndentedLineBuffer in which to place the unparsed query
     * @param outSyntax  Syntax URI
     */
    
    public void serialize(IndentedLineBuffer buff, Syntax outSyntax) { 
        serialize((IndentedWriter)buff, outSyntax);
    }

    /** Format the query
     * 
     * @param writer  IndentedWriter
     */
    
    public void serialize(IndentedWriter writer) { 
        serialize(writer, syntax); 
    }

    /** Format the query
     * 
     * @param writer     IndentedWriter
     * @param outSyntax  Syntax URI
     */
    
    public void serialize(IndentedWriter writer, Syntax outSyntax)
    {
        // Try to use a serializer factory if available
        QuerySerializerFactory factory = SerializerRegistry.get().getQuerySerializerFactory(outSyntax);
        QueryVisitor serializer = factory.create(outSyntax, this, writer);
        this.visit(serializer);
    }
}
