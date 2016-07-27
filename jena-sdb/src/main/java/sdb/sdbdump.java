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

package sdb;

import java.util.List;

import jena.cmd.ArgDecl;
import jena.cmd.CmdException ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.sdb.SDB ;
import sdb.cmd.CmdArgsDB;
import sdb.cmd.ModGraph;

 /** Write out the data from an SDB model.  Only works for small models
  * because of JDBC limitations in default configurations. 
  * 
  *  <p>
  *  Usage:<pre>
  *  sdbdump [db spec]
  *  where [db spec] is:
  *    --spec file        Contains an RDF description of the model 
  *    --db JDBC_url --dbUser userId --dbPassword password --dbType [--model modelName]  
  *  </pre>
  *  </p>
  */ 
 
public class sdbdump extends CmdArgsDB
{
    public static final String usage = "sdbdump --sdb <SPEC> [--out syntax]" ;

    private static ModGraph modGraph = new ModGraph() ;
    static ArgDecl argDeclSyntax = new ArgDecl(true, "output", "out") ;

    public static void main(String... argv) {
        SDB.init() ;
        new sdbdump(argv).mainRun() ;
    }

    protected sdbdump(String... args) {
        super(args) ;
        addModule(modGraph) ;
        add(argDeclSyntax, "--output=", "RDF Syntax for output (For datasets, TriG, N-Quads; for graphs, any RDF syntax)") ;
    }

    @Override
    protected String getCommandName() { return Lib.className(this) ; }
    
    @Override
    protected String getSummary()  { return Lib.className(this)+" --sdb <SPEC> [--out syntax]" ; }

    @Override
    protected void processModulesAndArgs() {
        if ( getNumPositional() > 0 )
            cmdError("No positional arguments allowed", true) ;
    }

    @Override
    protected void execCmd(List<String> args) {
        // This is a streamable syntax.
        String syntax = "N-QUADS";
        if ( contains(argDeclSyntax) )
            syntax = getArg(argDeclSyntax).getValue();
        Lang lang = RDFLanguages.nameToLang(syntax);

        try {
            if ( modGraph.getGraphName() == null ) {
                if ( ! RDFLanguages.isQuads(lang) )
                    cmdError("Not a 'quads' language (try 'N-Quads' or 'TriG')", true) ;
                Dataset dataset = getModStore().getDataset();
                RDFDataMgr.write(System.out, dataset, lang);
            } else {
                Model model = modGraph.getModel(getStore());
                RDFDataMgr.write(System.out, model, lang);
            }
        }
        catch (CmdException ex) { throw ex ; }
        catch (Exception ex) {
            System.err.println("Exception: " + ex + " :: " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }
}
