/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package arq.cmdline;

import java.util.* ;

/** 
 * Incoming String[] to a list of argument/values + items.
 * @author Andy Seaborne
 */


public class CommandLineBase
{
    // TODO Case sensitivity : flags and arg

    private List<String> argList = new ArrayList<String>() ;
    boolean splitTerms = true ;

    public CommandLineBase(String[]args)
    {
        setArgs(args) ;
    }

    public CommandLineBase() {}
    
    public void setArgs(String[] argv)
    { argList = processArgv(argv) ; }
    
//    static void process(String[]args, ArgProc proc)
//    {
//        new CommandLineBase(args).apply(proc) ;
//    }
    
    
    protected List<String> getArgList() { return argList ; }
    protected String getArg(int i)
    { 
        if ( i < 0 || i >= argList.size() )
            return null ;
        return argList.get(i) ;
    }

    protected void apply(ArgProc a)
    {
        a.startArgs() ;
        for ( int i = 0 ; i < argList.size() ; i++ )
        {
            String arg = argList.get(i) ;
            a.arg(arg, i) ;
        }
        a.finishArgs() ;
    }
    
    /** Process String[] to a list of tokens.
     *  All "=" and ":" terms are split.
     *  Make -flag/--flag consistent.
     * @param argv The words of the command line.
     */    
    private List<String> processArgv(String[] argv)
    {
        // Combine with processedArgs/process?
        List<String> argList = new ArrayList<String>() ;
        
        boolean positional = false ;
        
        for ( int i = 0 ; i < argv.length ; i++ )
        {
            String argStr = argv[i] ;
            
//            if ( ! argStr.startsWith("-") )
//                positional = true ;
//                // and get caught by next if statement 
//            
//            if ( positional )
//            {
//                argList.add(argStr) ; 
//                continue ;
//            }

            if ( positional || ! argStr.startsWith("-") )
            {
                argList.add(argStr) ; 
                continue ;
            } 
            
            if ( argStr.equals("-") || argStr.equals("--") )
            {
                positional = true ;
                argList.add("--") ; 
                continue ;
            }
            
            // Starts with a "-"
            // Do not canonicalize positional arguments.
            if ( !argStr.startsWith("--") )
                argStr = "-"+argStr ;
            
            if ( !splitTerms )
            {
                argList.add(argStr) ;
                continue ;
            }
                
            // If the flag has a "=" or :, it is long form --arg=value.
            // Split and insert the arg
            int j1 = argStr.indexOf('=') ;
            int j2 = argStr.indexOf(':') ;
            int j = -1 ;

            if ( j1 > 0 && j2 > 0 )
                j = Math.min(j1,j2) ;
            else
            {
                if ( j1 > 0 )
                    j = j1 ;
                if ( j2 > 0 )
                    j = j2 ;
            }

            if ( j < 0 )
            {
                argList.add(argStr) ;
                continue ;
            }  
            
            // Split it.
            String argStr1 = argStr.substring(0,j) ;
            String argStr2 = argStr.substring(j+1) ;
            
            argList.add(argStr1) ;
            argList.add(argStr2) ;
        }
        return argList ;
    }
}

/*
 *  (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
