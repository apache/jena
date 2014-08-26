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

package arq.cmdline;

import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.system.StreamRDFWriter ;
import arq.cmd.CmdException ;

public class ModLangOutput implements ArgModuleGeneral
{
    protected ArgDecl argOutput     = new ArgDecl(ArgDecl.HasValue, "out", "output") ;
    private RDFFormat format        = RDFFormat.NQUADS ;
    
    @Override
    public void registerWith(CmdGeneral cmdLine) {
        cmdLine.getUsage().startCategory("Output control") ;
        cmdLine.add(argOutput,    "--out=FMT",  "Output in the given format") ;
    }

    @Override
    public void processArgs(CmdArgModule cmdLine) {
        if ( cmdLine.contains(argOutput) ) {
            String langName = cmdLine.getValue(argOutput) ;
            Lang output = RDFLanguages.nameToLang(langName) ;
            if ( output == null )
                throw new CmdException("Not recognized as an RDF language : '"+langName+"'") ;
            if ( ! StreamRDFWriter.registered(output) ) {
                // ** Java8
//                StreamRDFWriter.registered().stream()
//                    .map(fmt -> fmt.getLang()) 
//                    .distinct()
//                    .forEach(x -> System.err.println("   "+x.getLabel())) ;
                
                Set<Lang> seen = new HashSet<>() ;
                for ( RDFFormat fmt : StreamRDFWriter.registered()) {
                    if ( seen.contains(fmt.getLang()) )
                        continue ;
                    seen.add(fmt.getLang()) ;
                    System.err.println("   "+fmt.getLang().getLabel()) ;
                }
                
                throw new CmdException("Not recognized as an streaming RDF language : '"+langName+"'") ;
            }
            format = StreamRDFWriter.defaultSerialization(output) ;
        }
    }

    public RDFFormat getOutputFormat() {
        return format ;
    }
}
