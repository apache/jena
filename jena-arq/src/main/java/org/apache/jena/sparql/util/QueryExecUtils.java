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

package org.apache.jena.sparql.util ;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List ;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.impl.PrefixMappingImpl ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVars ;
import org.apache.jena.sparql.algebra.op.OpProject ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Prologue ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.ResultSetStream;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecutionAdapter;
import org.apache.jena.sparql.resultset.RDFOutput;
import org.apache.jena.sparql.resultset.ResultsFormat ;
import org.apache.jena.sparql.resultset.ResultsWriter;
import org.apache.jena.sparql.resultset.SPARQLResult;

/** Some utilities for query processing. */
public class QueryExecUtils {
    protected static PrefixMapping globalPrefixMap = new PrefixMappingImpl() ;
    static {
        globalPrefixMap.setNsPrefix("rdf",  ARQConstants.rdfPrefix) ;
        globalPrefixMap.setNsPrefix("rdfs", ARQConstants.rdfsPrefix) ;
        globalPrefixMap.setNsPrefix("xsd",  ARQConstants.xsdPrefix) ;
        globalPrefixMap.setNsPrefix("owl" , ARQConstants.owlPrefix) ;
        globalPrefixMap.setNsPrefix("ex" ,  "http://example.org/") ;
        globalPrefixMap.setNsPrefix("ns" ,  "http://example.org/ns#") ;
        globalPrefixMap.setNsPrefix("" ,    "http://example/") ;
    }
    protected static Prologue      dftPrologue     = new Prologue(globalPrefixMap) ;

    public static void exec(Query query, DatasetGraph dsg) {
        QueryExec qExec = QueryExec.dataset(dsg).query(query).build();
        exec(qExec);
    }

    public static void exec(Query query, Graph graph) {
        QueryExec qExec = QueryExec.graph(graph).query(query).build();
        exec(qExec);
    }

    public static void exec(QueryExec queryExec) {
        exec(queryExec.getQuery(), queryExec) ;
    }

    public static void exec(Prologue prologue, QueryExec queryExec) {
        exec(prologue, queryExec, ResultsFormat.TEXT) ;
    }

    public static void exec(Prologue prologue, QueryExec queryExec, ResultsFormat outputFormat) {
        exec(prologue, queryExec, outputFormat, System.out);
    }

    public static void exec(Prologue prologue, QueryExec queryExec, ResultsFormat outputFormat, OutputStream output) {
        QueryExecution queryExecution = QueryExecutionAdapter.adapt(queryExec);
        executeQuery(prologue, queryExecution, outputFormat, output);
    }

    public static void executeQuery(QueryExecution queryExecution) {
        executeQuery(null, queryExecution) ;
    }

    public static void executeQuery(Prologue prologue, QueryExecution queryExecution) {
        executeQuery(prologue, queryExecution, ResultsFormat.TEXT) ;
    }

    public static void executeQuery(Prologue prologue, QueryExecution queryExecution, ResultsFormat outputFormat) {
        executeQuery(prologue, queryExecution, outputFormat, System.out);
    }

    public static void executeQuery(Prologue prologue, QueryExecution queryExecution, ResultsFormat outputFormat, OutputStream output) {
        Query query = queryExecution.getQuery() ;
        if ( prologue == null && query != null )
            prologue = query.getPrologue() ;
        if ( prologue == null )
            prologue = dftPrologue ;
        if ( query.isSelectType() )
            doSelectQuery(prologue, queryExecution, outputFormat, output) ;
        else if ( query.isDescribeType() )
            doDescribeQuery(prologue, queryExecution, outputFormat, output) ;
        else if ( query.isConstructQuad() )
            // Before isConstructType.
            doConstructQuadsQuery(prologue, queryExecution, outputFormat, output) ;
        else if ( query.isConstructType() )
            doConstructQuery(prologue, queryExecution, outputFormat, output) ;
        else if ( query.isAskType() )
            doAskQuery(prologue, queryExecution, outputFormat, output) ;
        else if ( query.isJsonType() )
            doJsonQuery(prologue, queryExecution, outputFormat, output) ;
        else
            throw new QueryException("Unrecognized query form");
    }

    public static void execute(Op op, DatasetGraph dsg) {
        execute(op, dsg, ResultsFormat.TEXT) ;
    }

    public static void execute(Op op, DatasetGraph dsg, ResultsFormat outputFormat) {
        execute(op, dsg, outputFormat, System.out);
    }

    public static void execute(Op op, DatasetGraph dsg, ResultsFormat outputFormat, OutputStream output) {
        QueryIterator qIter = Algebra.exec(op, dsg) ;

        List<Var> vars = null ;
        if ( op instanceof OpProject )
            vars = ((OpProject)op).getVars() ;
        else
            // The variables defined in patterns (not Filters, nor NOT EXISTS,
            // nor ORDER BY)
            vars = new ArrayList<>(OpVars.visibleVars(op)) ;

        ResultSet results = ResultSetStream.create(vars, qIter) ;
        outputResultSet(results, null, outputFormat, output) ;
    }


    public static void output(SPARQLResult result, ResultsFormat outputFormat, OutputStream output) {
        if ( result.isResultSet() ) {
            ResultSet rs = result.getResultSet();
            outputResultSet(rs, null, outputFormat, output);
            return;
        }

        if ( result.isModel() ) {
            Model m = result.getModel();
            writeModel(m, outputFormat, output);
            return;
        }

        if ( result.isGraph() ) {}

        if ( result.isDataset() ) {
            writeDataset(result.getDataset(), outputFormat, output);
            return;
        }
        if ( result.isJson() ) {
            result.getJsonItems();
            return;
        }
    }

    public static void outputResultSet(ResultSet resultSet, Prologue prologue, ResultsFormat outputFormat, OutputStream output) {
        if ( prologue == null )
            prologue = new Prologue(globalPrefixMap) ;

        if ( outputFormat == ResultsFormat.TEXT ) {
            ResultSetFormatter.out(output, resultSet);
            return;
        }

        if ( outputFormat == ResultsFormat.COUNT ) {
            long count = ResultSetFormatter.consume(resultSet);
            String text = String.format("%d\n", count);
            try ( PrintStream ps = new PrintStream(output) ) {
                ps.print(text);
            }
            return;
        }

        Lang rsLang = outputFormat.resultSetLang();

        if ( rsLang == null ) {
            RDFFormat asRDF = outputFormat.rdfFormat();
            Model model = RDFOutput.encodeAsModel(resultSet);
            RDFWriter.source(model).format(asRDF).output(output);
            return;
        }

        if ( ! ResultSetLang.isRegistered(rsLang) )
            throw noFormatException("Not a result set output lang: "+rsLang.getName());

        Context context = ARQ.getContext().copy();
        if ( prologue != null )
            context.set(ARQConstants.symPrologue, prologue);
        ResultsWriter.create().context(context).lang(rsLang).build().write(output, resultSet);
        return ;
    }

    private static void doSelectQuery(Prologue prologue, QueryExecution qe, ResultsFormat outputFormat, OutputStream output) {
        if ( prologue == null )
            prologue = qe.getQuery().getPrologue() ;
        if ( outputFormat == null )
            outputFormat = ResultsFormat.TEXT;
        ResultSet results = qe.execSelect() ;
        outputResultSet(results, prologue, outputFormat, output) ;
    }

    private static void doJsonQuery(Prologue prologue, QueryExecution queryExecution, ResultsFormat outputFormat, OutputStream output) {
        JsonArray results = queryExecution.execJson();
        JSON.write(output, results);
    }

    private static void doDescribeQuery(Prologue prologue, QueryExecution qe, ResultsFormat outputFormat, OutputStream output) {
        Model r = qe.execDescribe() ;
        writeModel(r, outputFormat, output) ;
    }

    private static void doConstructQuery(Prologue prologue, QueryExecution qe, ResultsFormat outputFormat, OutputStream output) {
        if ( qe.getQuery().isConstructQuad() ) {
            doConstructQuadsQuery(prologue, qe, outputFormat, output);
            return;
        }
        Model r = qe.execConstruct() ;
        writeModel(r, outputFormat, output) ;
    }

    private static void doConstructQuadsQuery(Prologue prologue, QueryExecution qe, ResultsFormat outputFormat, OutputStream output) {
        Dataset ds = qe.execConstructDataset();
        writeDataset(ds, outputFormat, output) ;
    }

    private static void doAskQuery(Prologue prologue, QueryExecution qe, ResultsFormat outputFormat, OutputStream output) {
        boolean resultBoolean = qe.execAsk() ;

        if ( outputFormat == ResultsFormat.TEXT ) {
            ResultSetFormatter.out(output, resultBoolean);
            return;
        }

        Lang rsLang = outputFormat.resultSetLang();
        if ( rsLang == null ) {
            RDFFormat asRDF = outputFormat.rdfFormat();
            Model model = RDFOutput.encodeAsModel(resultBoolean);
            RDFWriter.source(model).format(asRDF).output(output);
            return;
        }

        ResultsWriter.create().lang(rsLang).build().write(output, resultBoolean);
    }

    private static void writeModel(Model model, ResultsFormat outputFormat, OutputStream output) {
        RDFFormat rdfFormat = RDFFormat.TURTLE_PRETTY;
        RDFWriter.source(model).format(rdfFormat).output(output);
        return;
    }

    private static void writeDataset(Dataset dataset, ResultsFormat outputFormat, OutputStream output) {
        RDFFormat rdfFormat = outputFormat.rdfFormat();
        if ( rdfFormat == null )
            throw noFormatException("No dataset output format for : "+outputFormat.name());
        RDFWriter.source(dataset).format(rdfFormat).output(output);
        return;
    }


    private static RuntimeException noFormatException(String msg) {
        return new ARQException(msg);
    }

    /**
     * Execute a query, expecting the result to be one row, one column. Return
     * that one RDFNode
     */
    public static RDFNode getExactlyOne(String qs, Model model) {
        return getExactlyOne(qs, DatasetFactory.wrap(model)) ;
    }

    /**
     * Execute a query, expecting the result to be one row, one column. Return
     * that one RDFNode
     */
    public static RDFNode getExactlyOne(String qs, Dataset ds) {
        Query q = QueryFactory.create(qs) ;
        if ( q.getResultVars().size() != 1 )
            throw new ARQException("getExactlyOne: Must have exactly one result columns") ;
        String varname = q.getResultVars().get(0) ;
        try ( QueryExecution qExec = QueryExecutionFactory.create(q, ds) ) {
            return getExactlyOne(qExec, varname) ;
        }
    }

    /**
     * Execute, expecting the result to be one row, one column. Return that one.
     * RDFNode or throw an exception.
     * Use with {@code try ( QueryExecution qExec = ....)}.
     */
    public static RDFNode getExactlyOne(QueryExecution qExec, String varname) {
        ResultSet rs = qExec.execSelect() ;

        if ( !rs.hasNext() )
            throw new ARQException("Not found: var ?" + varname) ;

        QuerySolution qs = rs.nextSolution() ;
        RDFNode r = qs.get(varname) ;
        if ( rs.hasNext() )
            throw new ARQException("More than one: var ?" + varname) ;
        return r ;
    }

    /**
     * Execute, expecting the result to be one row, one column. Return that one
     * RDFNode or null. Throw exception if more than one.
     * Use with {@code try ( QueryExecution qExec = ....)}.
     */
    public static RDFNode getAtMostOne(QueryExecution qExec, String varname) {
        ResultSet rs = qExec.execSelect() ;

        if ( !rs.hasNext() )
            return null ;

        QuerySolution qs = rs.nextSolution() ;
        RDFNode r = qs.get(varname) ;
        if ( rs.hasNext() ) {
            QuerySolution qs2 = rs.next() ;
            RDFNode r2 = qs2.get(varname) ;
            if ( rs.hasNext() )
                throw new ARQException("More than one: var ?" + varname + " -> " + r + ", " + r2 + ", ...") ;
            else
                throw new ARQException("Found two matches: var ?" + varname + " -> " + r + ", " + r2) ;
        }
        return r ;
    }

    /**
     * Execute, returning all matches, which may be zero.
     */
    public static List<RDFNode> getAll(QueryExecution qExec, String varname) {
        ResultSet rs = qExec.execSelect() ;
        List<RDFNode> matches = new ArrayList<>();
        rs.forEachRemaining(qs->{
            RDFNode r = qs.get(varname) ;
            if ( r != null )
                matches.add(r);
        });
        return matches ;
    }
}
