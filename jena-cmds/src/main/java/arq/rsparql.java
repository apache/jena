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

package arq;

import jena.cmd.CmdException;

import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryExecution ;
import org.apache.jena.query.QueryExecutionFactory ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.sparql.engine.http.HttpQuery ;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP ;
import org.apache.jena.sparql.util.QueryExecUtils ;

import arq.cmdline.CmdARQ ;
import arq.cmdline.ModQueryIn ;
import arq.cmdline.ModRemote ;
import arq.cmdline.ModResultsOut ;

public class rsparql extends CmdARQ
{
    protected ModQueryIn    modQuery =      new ModQueryIn(Syntax.syntaxSPARQL_11) ;
    protected ModRemote     modRemote =     new ModRemote() ;
    protected ModResultsOut modResults =    new ModResultsOut() ;

    public static void main (String... argv)
    {
        new rsparql(argv).mainRun() ;
    }


    public rsparql(String[] argv)
    {
        super(argv) ;
        super.addModule(modRemote) ;
        super.addModule(modQuery) ;
        super.addModule(modResults) ;
    }
    
    
    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        if ( modRemote.getServiceURL() == null )
            throw new CmdException("No SPARQL endpoint specificied") ;
    }
    
    @Override
    protected void exec()
    {
        Query query = modQuery.getQuery() ;

        try {
            String serviceURL = modRemote.getServiceURL() ;
            QueryExecution qe = QueryExecutionFactory.sparqlService(serviceURL, query) ;
            if ( modRemote.usePost() )
                HttpQuery.urlLimit = 0 ;

            QueryExecUtils.executeQuery(query, qe, modResults.getResultsFormat()) ;
        } catch (QueryExceptionHTTP ex)
        {
            throw new CmdException("HTTP Exeception", ex) ;
        }
        catch (Exception ex)
        {
            System.out.flush() ;
            ex.printStackTrace(System.err) ;
        }
    }


    @Override
    protected String getSummary()
    {
        return null ;
    }

}
