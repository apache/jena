/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModResultsIn;
import arq.cmdline.ModResultsOut;

import com.hp.hpl.jena.query.ResultSet;

/** Read and write result sets */

public class rset extends CmdARQ
{
    ModResultsIn modInput    = new ModResultsIn() ;
    ModResultsOut modOutput  = new ModResultsOut() ;
    
    static String usage = rset.class.getName()+
            " [--in syntax] [--out syntax] [--file FILE | FILE ]" ; 

    public static void main(String... argv)
    {
        new rset(argv).mainRun() ;
    }

    public rset(String[] argv)
    {
        super(argv) ;
        super.addModule(modInput) ;
        super.addModule(modOutput) ;
    }
            
    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
    }

    @Override
    protected String getSummary()
    {
        return usage ;
    }

    @Override
    protected void exec()
    {
        ResultSet rs = modInput.getResultSet() ;
        modOutput.printResultSet(rs, null) ;
    }

    @Override
    protected String getCommandName()
    {
        return "rset" ;
    }

}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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