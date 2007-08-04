/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import java.util.*;

import arq.cmd.CmdException;

import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.sparql.util.ALog;


/** Command line, using the common named/positional arguments paradigm
 *  (also called options/arguments).
 * @author Andy Seaborne
 * @version $Id: CmdLineArgs.java,v 1.9 2007/02/03 20:27:19 andy_seaborne Exp $
 */
public class CmdLineArgs extends CommandLineBase
{
    public CmdLineArgs(String[] args)
    {
        super(args) ;
       
    }

    private boolean processedArgs = false ;
    protected Map argMap = new HashMap() ;          // Map from string name to ArgDecl 
    protected Map args = new HashMap() ;            // Name to Arg  
    protected List positionals = new ArrayList() ;  // Positional arguments as strings.
    
    public void process() throws IllegalArgumentException
    {
        processedArgs = true ;
        apply(new ArgProcessor()) ;
    }
    
    // ---- Setting the ArgDecls

    /** Add an argument to those to be accepted on the command line.
     * @param argName Name
     * @param hasValue True if the command takes a (string) value
     * @return The CommandLine processor object
     */
   
    public CmdLineArgs add(String argName, boolean hasValue)
    {
        return add(new ArgDecl(hasValue, argName)) ;
    }

    /** Add an argument to those to be accepted on the command line.
     *  Argument order reflects ArgDecl.
     * @param hasValue True if the command takes a (string) value
     * @param argName Name
     * @return The CommandLine processor object
     */
   
    public CmdLineArgs add(boolean hasValue, String argName)
    {
        return add(new ArgDecl(hasValue, argName)) ;
    }

    /** Add an argument object
     * @param arg Argument to add
     * @return The CommandLine processor object
     */
   
    public CmdLineArgs add(ArgDecl arg)
    {
        for ( Iterator iter = arg.names() ; iter.hasNext() ; )
        {
            String name = (String)iter.next();
            if ( argMap.containsKey(name))
                ALog.warn(this, "Argument '"+name+"' already added") ; 
            argMap.put(name, arg) ;
        }
        return this ;
    }
    
    /**
     * Add a positional parameter
     * @param value
     * @return
     */
    public CmdLineArgs addPositional(String value)
    {
        positionals.add(value) ;
        return this ;
    }
    
    /**
     * Add a named argument which has no value.
     * @param name
     * @return
     */
    public CmdLineArgs addArg(String name)
    {
        return addArg(name, null) ;
    }

    /**
     * Add a named argument/value pair
     * @param name
     * @param value
     * @return
     */
    public CmdLineArgs addArg(String name, String value)
    {
        if ( ! args.containsKey(name) )
            args.put(name, new Arg(name)) ;
        Arg arg = (Arg)args.get(name) ;
        return addArgWorker(arg, value) ;
    }

    private CmdLineArgs addArgWorker(Arg arg, String value)
    {
        ArgDecl argDecl = (ArgDecl)argMap.get(arg.name) ;

        if ( ! argDecl.takesValue() && value != null )
                throw new IllegalArgumentException("No value for argument: "+arg.getName()) ;
        
        if ( argDecl.takesValue() )
        {
            if ( value == null )
                throw new IllegalArgumentException("No value for argument: "+arg.getName()) ;

            arg.setValue(value) ;
            arg.addValue(value) ;
        }

        return this ;
    }

    // ---- Indirection

    static final String DefaultIndirectMarker = "@" ;
    
    public boolean matchesIndirect(String s) { return matchesIndirect(s, DefaultIndirectMarker) ; }
    public boolean matchesIndirect(String s, String marker) { return s.startsWith(marker) ; }
    
    public String indirect(String s) { return indirect(s, DefaultIndirectMarker) ; }
    
    public String indirect(String s, String marker)
    {
        if ( ! matchesIndirect(s, marker) )
            return s ;
        s = s.substring(marker.length()) ; 
        s = FileManager.get().readWholeFileAsUTF8(s) ;
        return s ;
    }
    
    // ---- Argument access
    
    /** Test whether an argument was seen.
     */

    public boolean contains(ArgDecl argDecl) { return getArg(argDecl) != null ; }
    
    /** Test whether an argument was seen.
     */

    public boolean contains(String s) { return getArg(s) != null ; }
    
    public boolean hasArgs() { return args.size() > 0 ; }
    
    /** Test whether the command line had a particular argument
     * 
     * @param argName
     * @return
     */
    public boolean hasArg(String argName) { return getArg(argName) != null ; }

    /** Test whether the command line had a particular argument
     * 
     * @param argDecl
     * @return
     */
    
    public boolean hasArg(ArgDecl argDecl) { return getArg(argDecl) != null ; }

    
    /** Get the argument associated with the argument declaration.
     *  Actually returns the LAST one seen
     *  @param argDecl Argument declaration to find
     *  @return Last argument that matched.
     */
    
    public Arg getArg(ArgDecl argDecl)
    {
        Arg arg = null ;
        for ( Iterator iter = args.values().iterator() ; iter.hasNext() ; )
        {
            Arg a = (Arg)iter.next() ;
            if ( argDecl.matches(a) )
                arg = a ;
        }
        return arg ;
    }
    
    /** Get the argument associated with the arguement name.
     *  Actually returns the LAST one seen
     *  @param argDecl Argument declaration to find
     *  @return Last argument that matched.
     */
    
    public Arg getArg(String s)
    {
        s = ArgDecl.canonicalForm(s) ;
        return (Arg)args.get(s) ;
    }
    
    /**
     * Returns the value (a string) for an argument with a value - 
     * returns null for no argument and no value.  
     * @param argDecl
     * @return String
     */
    public String getValue(ArgDecl argDecl)
    {
        Arg arg = getArg(argDecl) ;
        if ( arg == null )
            return null ;
        if ( arg.hasValue())
            return arg.getValue() ;
        return null ;
    }    

    /**
     * Returns the value (a string) for an argument with a value - 
     * returns null for no argument and no value.  
     * @param argDecl
     * @return String
     */
    public String getValue(String argName)
    {
        Arg arg = getArg(argName) ;
        if ( arg == null )
            return null ;
        return arg.getValue() ;
    }    
    
    /**
     * Returns all the values (0 or more strings) for an argument. 
     * @param argDecl
     * @return List
     */
    public List getValues(ArgDecl argDecl)
    {
        Arg arg = getArg(argDecl) ;
        if ( arg == null )
            return new ArrayList() ;
        return arg.getValues() ;
    }    

    /**
     * Returns all the values (0 or more strings) for an argument. 
     * @param argDecl
     * @return List
     */
    public List getValues(String argName)
    {
        Arg arg = getArg(argName) ;
        if ( arg == null )
            return new ArrayList() ;
        return arg.getValues() ;
    }    
    
   // ---- Positional 
    /** Get the positional argument 
     * 
     */
    public String getPositionalArg(int i)
    {
        return (String)positionals.get(i) ;
    }
    
    public int getNumPositional()
    {
        return positionals.size() ;
    }

    public boolean hasPositional()
    {
        return positionals.size() > 0 ;
    }
    
    public List getPositional()
    {
        return positionals ;
    }

    
    // ----
    
    /**
     * Handle an unrecognised argument; default is to throw an exception
     * @param argStr The string image of the unrecognised argument
     */
    protected void handleUnrecognizedArg( String argStr ) {
        throw new CmdException("Unknown argument: "+argStr) ;
    }
    
    private ArgDecl find(String a)
    {
        a = ArgDecl.canonicalForm(a) ;
        return (ArgDecl)argMap.get(a) ;
    }

    public String toString()
    {
        if ( ! processedArgs ) return super.toString() ;
        String str = "" ;
        String sep = "" ;
        for ( Iterator iter = args.keySet().iterator() ; iter.hasNext() ; )
        {
            String k = (String)iter.next() ;
            Arg a = (Arg)args.get(k) ;
            str = str+sep+a ;
            sep = " " ;
        }
        sep = " -- " ;
        for ( Iterator iter = positionals.iterator() ; iter.hasNext() ; )
        {
            String v = (String)iter.next() ;
            str = str+sep+v ;
            sep = " " ;
        }
        return str ;
    }

    // ---- Process arguments after low level parsing and after ArgDecls added.
    class ArgProcessor  implements ArgProc
    {
        boolean nextArgProcessed = false ;
        boolean positionalArgsStarted = false ;
        
        public void startArgs()   { nextArgProcessed = false ; positionalArgsStarted = false ; }
        public void finishArgs()  {}
        
        public void arg(String argStr, int i)
        {
            if ( nextArgProcessed )
            {
                nextArgProcessed = false ;
                return ;
            }
            
            if ( positionalArgsStarted )
            {
                addPositional(argStr) ;
                return ;
            }
            
            if ( argStr.equals("-") || argStr.equals("--") )
            {
                positionalArgsStarted = true ;
                return ;
            }
            
            if ( ! argStr.startsWith("-") ) 
            {
                // End of flags, start of positional arguments
                positionalArgsStarted = true ;
                addPositional(argStr) ;
                return ;
            }
            
            argStr = ArgDecl.canonicalForm(argStr) ;
            if ( ! argMap.containsKey(argStr) )
            {
                handleUnrecognizedArg( argStr );
                return ;
            }
            
            // Recognized flag
            ArgDecl argDecl = (ArgDecl)argMap.get(argStr) ;
            
            if ( argDecl.takesValue() )
            {
                String val = getArg(i+1) ;
                addArg(argStr, val) ;
                nextArgProcessed = true ;
            }
            else
                addArg(argStr) ;
        }
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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