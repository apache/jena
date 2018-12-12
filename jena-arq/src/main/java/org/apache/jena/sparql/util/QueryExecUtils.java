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

import java.util.ArrayList;
import java.util.List ;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.resultset.rw.ResultsWriter;
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
import org.apache.jena.sparql.resultset.RDFOutput ;
import org.apache.jena.sparql.resultset.ResultsFormat ;

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

    public static void executeQuery(QueryExecution queryExecution) {
        executeQuery(null, queryExecution) ;
    }

    public static void executeQuery(Prologue prologue, QueryExecution queryExecution) {
        executeQuery(prologue, queryExecution, ResultsFormat.FMT_TEXT) ;
    }

    public static void executeQuery(Prologue prologue, QueryExecution queryExecution, ResultsFormat outputFormat) {
        Query query = queryExecution.getQuery() ;
        if ( prologue == null )
            prologue = query.getPrologue() ;
        if ( prologue == null )
            prologue = dftPrologue ;
        if ( query.isSelectType() )
            doSelectQuery(prologue, queryExecution, outputFormat) ;
        else if ( query.isDescribeType() )
            doDescribeQuery(prologue, queryExecution, outputFormat) ;
        else if ( query.isConstructQuad() )
            // Before isConstructType.
            doConstructQuadsQuery(prologue, queryExecution, outputFormat) ;
        else if ( query.isConstructType() )
            doConstructQuery(prologue, queryExecution, outputFormat) ;
        else if ( query.isAskType() )
            doAskQuery(prologue, queryExecution, outputFormat) ;
        else if ( query.isJsonType() )
            doJsonQuery(prologue, queryExecution, outputFormat) ;
        else
            throw new QueryException("Unrecognized query form");
    }

    public static void execute(Op op, DatasetGraph dsg) {
        execute(op, dsg, ResultsFormat.FMT_TEXT) ;
    }

    public static void execute(Op op, DatasetGraph dsg, ResultsFormat outputFormat) {
        QueryIterator qIter = Algebra.exec(op, dsg) ;

        List<String> vars = null ;
        if ( op instanceof OpProject )
            vars = Var.varNames(((OpProject)op).getVars()) ;
        else
            // The variables defined in patterns (not Filters, nor NOT EXISTS,
            // nor ORDER BY)
            vars = Var.varNames(OpVars.visibleVars(op)) ;

        ResultSet results = ResultSetFactory.create(qIter, vars) ;
        outputResultSet(results, null, outputFormat) ;
    }

    public static void outputResultSet(ResultSet results, Prologue prologue, ResultsFormat outputFormat) {
        if ( outputFormat.equals(ResultsFormat.FMT_UNKNOWN) )
            outputFormat = ResultsFormat.FMT_TEXT ;
        // Proper ResultSet formats.
        if ( prologue == null )
            prologue = new Prologue(globalPrefixMap) ;
        Lang lang = ResultsFormat.convert(outputFormat) ;
        if ( lang != null ) {
            Context context = ARQ.getContext().copy();
            if ( prologue != null )
                context.set(ARQConstants.symPrologue, prologue);
            ResultsWriter.create().context(context).lang(lang).build().write(System.out, results);
            System.out.flush() ;
            return ;
        }
        
        boolean done = ResultsFormat.oldWrite(System.out, outputFormat, prologue, results);
        if ( !done )
            System.err.println("Unknown format request: " + outputFormat) ;
        System.out.flush() ;
    }

    private static void doSelectQuery(Prologue prologue, QueryExecution qe, ResultsFormat outputFormat) {
        if ( prologue == null )
            prologue = qe.getQuery().getPrologue() ;
        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_TEXT ;
        ResultSet results = qe.execSelect() ;
        outputResultSet(results, prologue, outputFormat) ;
    }

    private static void doJsonQuery(Prologue prologue, QueryExecution queryExecution, ResultsFormat outputFormat) {
        JsonArray results = queryExecution.execJson();
        JSON.write(System.out, results);
    }

    private static void doDescribeQuery(Prologue prologue, QueryExecution qe, ResultsFormat outputFormat) {
        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_RDF_TTL ;

        Model r = qe.execDescribe() ;
        writeModel(prologue, r, outputFormat) ;
    }

    private static void doConstructQuery(Prologue prologue, QueryExecution qe, ResultsFormat outputFormat) {
        if ( qe.getQuery().isConstructQuad() ) {
            doConstructQuadsQuery(prologue, qe, outputFormat);
            return;
        }
        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_RDF_TTL ;
        Model r = qe.execConstruct() ;
        writeModel(prologue, r, outputFormat) ;
    }

    private static void doConstructQuadsQuery(Prologue prologue, QueryExecution qe, ResultsFormat outputFormat) {
        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_RDF_TRIG;
        Dataset ds = qe.execConstructDataset();
        writeDataset(prologue, ds, outputFormat) ;
    }

    private static void writeModel(Prologue prologue, Model model, ResultsFormat outputFormat) {
        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_TEXT ;

        if ( outputFormat.equals(ResultsFormat.FMT_NONE) )
            return ;

        if ( outputFormat.equals(ResultsFormat.FMT_TEXT) ) {
            System.out.println("# ======== ") ;
            RDFDataMgr.write(System.out, model, Lang.TURTLE) ;
            System.out.println("# ======== ") ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_XML) ) {
            model.write(System.out, "RDF/XML-ABBREV", null) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_TTL) ) {
            model.write(System.out, "N3", null) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_N3) ) {
            model.write(System.out, "N3", null) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_NT) ) {
            model.write(System.out, "N-TRIPLES", null) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_NQ) ) {
            model.write(System.out, "N-QUADS", null) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_TRIG) ) {
            model.write(System.out, "TriG", null) ;
            return ;
        }

        System.err.println("Unknown format: " + outputFormat) ;
    }

    private static void writeDataset(Prologue prologue, Dataset dataset, ResultsFormat outputFormat) {
        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_TEXT ;

        if ( outputFormat.equals(ResultsFormat.FMT_NONE) )
            return ;

        if ( outputFormat.equals(ResultsFormat.FMT_TEXT) ) {
            System.out.println("# ======== ") ;
            RDFDataMgr.write(System.out, dataset, Lang.TURTLE) ;
            System.out.println("# ======== ") ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_NQ) ) {
            RDFDataMgr.write(System.out, dataset, Lang.NQUADS);
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_TRIG) ) {
            RDFDataMgr.write(System.out, dataset, Lang.TRIG);
            return ;
        }

        System.err.println("Unknown format: " + outputFormat) ;
    }

    
    private static void doAskQuery(Prologue prologue, QueryExecution qe, ResultsFormat outputFormat) {
        boolean b = qe.execAsk() ;

        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_TEXT ;

        if ( outputFormat.equals(ResultsFormat.FMT_NONE) )
            return ;

        if ( outputFormat.equals(ResultsFormat.FMT_RS_XML) ) {
            ResultSetFormatter.outputAsXML(System.out, b) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_N3) || outputFormat.equals(ResultsFormat.FMT_RDF_TTL) ) {
            RDFOutput.outputAsRDF(System.out, "TURTLE", b) ;
            System.out.flush() ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_JSON) ) {
            ResultSetFormatter.outputAsJSON(System.out, b) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_TEXT) ) {
            // ResultSetFormatter.out(System.out, b) ;
            System.out.println("Ask => " + (b ? "Yes" : "No")) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_CSV) ) {
            ResultSetFormatter.outputAsCSV(System.out, b) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_TSV) ) {
            ResultSetFormatter.outputAsTSV(System.out, b) ;
            return ;
        }
        System.err.println("Unknown format: " + outputFormat) ;
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
