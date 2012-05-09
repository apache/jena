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


import java.util.Arrays;
import java.util.List;

import arq.cmdline.ArgDecl;
import arq.cmdline.ArgModuleGeneral;
import arq.cmdline.CmdArgModule;
import arq.cmdline.CmdGeneral;

public class ModFormat implements ArgModuleGeneral
{
    protected final 
    ArgDecl resultsFmtDecl = new ArgDecl(ArgDecl.HasValue, "fmt", "format") ;

    private String format = "N-TRIPLES" ;
    
    public ModFormat() {}
    
    @Override
    public void processArgs(CmdArgModule cmdline) throws IllegalArgumentException
    {
        if ( cmdline.contains(resultsFmtDecl) )
        {
            String rFmt = cmdline.getValue(resultsFmtDecl) ;
            format = lookup(rFmt) ;
            if ( format == null )
                cmdline.cmdError("Unrecognized format: "+rFmt) ;
        }
    }
    
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Output format") ;
        cmdLine.add(resultsFmtDecl,
                    "--format",
                    "Format (Result sets: text, XML, JSON; Graph: RDF serialization)") ;  
    }

    public void checkCommandLine(CmdArgModule cmdLine)
    {}

    public String getFormat() { return format ; } 
    
    public String getFormat(String defaultFormat)
    { 
        if ( format == null )
            return defaultFormat ;
        return format ;
    }
  
    private String lookup(String fmt)
    {
        for ( String x : formats )
            if ( x.equalsIgnoreCase(fmt))
                return x ;
        return "TURTLE" ;
    }

    static final List<String> formats = Arrays.asList(
        "RDF/XML",
        "RDF/XML-ABBREV",
        "N-TRIPLE",
        "N-TRIPLES",
        "N3",
        "N3-PP" ,
        "N3-PLAIN" ,
        "N3-TRIPLES" ,
        "N3-TRIPLE" ,
        "TURTLE" ,
        //"Turtle" ,
        "TTL"
        ) ;
}
