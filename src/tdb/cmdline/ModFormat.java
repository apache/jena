/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb.cmdline;


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

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
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