/*
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package jena.cmdline ;

import java.io.* ;
import java.util.* ;

/**
 * Command line argument processing based on a trigger model.
 * An action is called whenever an argument is encountered. Example:
 * <CODE>
 * public static void main (String[] args)
 * {
 *  CommandLine cl = new CommandLine() ;
 *  cl.add(false, "verbose")
 *    .add(true, "--file") ;
 *  cl.process(args) ;
 *
 *  for ( Iterator iter = cl.args() ; iter.hasNext() ; )
 *  ...
 * }
 * </CODE>
 * A gloabl hook is provided to inspect arguments just before the
 * action.  Tracing is enabled by setting this to a suitable function
 * such as that provided by trace():
 * <CODE>
 *  cl.setHook(cl.trace()) ;
 * </CODE>
 *
 * <ul>
 * <li>Neutral as to whether options have - or --</li>
 * <li>Does not allow multiple single letter options
 *    to be concatenated.</li>
 * <li>Options may be ended with - or --</li>
 * </ul>
 * @author Andy Seaborne
 * @version $Id: CommandLine.java,v 1.4 2005-02-21 11:48:56 andy_seaborne Exp $
 */


public class CommandLine
{
    /* Extra processor called before the registered one when set.
     * Used for tracing.
     */
    protected ArgHandler argHook = null ;
    protected String usage = null ;
    protected Map argMap = new HashMap() ;
    protected Set argValue = new HashSet() ;
    protected PrintStream out = System.err ;

    // Arguments (flags and options) found
    protected List args = new ArrayList() ;
    // Rest of the items found on the command line
    protected List items = new ArrayList() ;

    
    /** Creates new CommandLine */
    public CommandLine()
    {
    }
    
    /** Set the global argument handler.  Called on every valid argument.
     * @param argHandler Handler
     */    
    public void setHook(ArgHandler argHandler) { argHook = argHandler ; }
    
    /** Set the output stream, or null for silent.
     * Default value is System.err
     */    
    public void setOutput(PrintStream out) { this.out = out ; }
    public PrintStream getOutput() { return out ; }
    public void setUsage(String usageMessage) { usage = usageMessage ; }
    
    public List args() { return args ; }
    public List items() { return items ; }
    
    
    /** Process a set of command line arguments.
     * @param argv The words of the command line.
     * @throws IllegalArgumentException Throw when something is wrong (no value found, action fails).
     */    
    public void process(String[] argv) throws java.lang.IllegalArgumentException
    {
        try {
            int i = 0 ;
            for ( ; i < argv.length ; i++ )
            {
                String argStr = argv[i] ;
                if ( ! argStr.startsWith("-") || argStr.equals("--") || argStr.equals("-") )
                    break ;
                
                argStr = ArgDecl.canonicalForm(argStr) ;
                String val = null ;

                if ( argMap.containsKey(argStr) )
                {
                    Arg arg = new Arg(argStr) ;
                    ArgDecl argDecl = (ArgDecl)argMap.get(argStr) ;
                    
                    if ( argDecl.takesValue() )
                    {
                        if ( i == (argv.length-1) )
                            throw new IllegalArgumentException("No value for argument: "+arg.getName()) ;
                        val = argv[++i] ;
                        arg.setValue(val) ;
                    }
                    
                    // Global hook
                    if ( argHook != null )
                        argHook.action(argStr, val) ;
                    
                    argDecl.trigger(arg) ;
                    args.add(arg) ;
                }
                else
                    // Not recognized
                    throw new IllegalArgumentException("Unknown argument: "+argStr) ;
            }
            
            // Remainder.
            if ( i < argv.length )
            {
                if ( argv[i].equals("-") || argv[i].equals("--") )
                    i++ ;
                for ( ; i < argv.length ; i++ )
                    items.add(argv[i]) ;
            }
        } catch (IllegalArgumentException ex)
        {
            if ( out != null )
            {
                if ( usage != null ) out.println(usage) ;
                out.println(ex.getMessage()) ;
            }
            throw ex ;
        }
    }

    /** Test whether an argument was seen.
     */

    public boolean contains(ArgDecl argDecl) { return getArg(argDecl) != null ; }
    
    /** Test whether an argument was seen.
     */

    public boolean contains(String s) { return getArg(s) != null ; }
    
    
    /** Get the argument associated with the argurment declaration.
     *  Actually retruns the LAST one seen
     *  @param argDecl Argument declaration to find
     *  @return Last argument that matched.
     */
    
    public Arg getArg(ArgDecl argDecl)
    {
        Arg arg = null ;
        for ( Iterator iter = args.iterator() ; iter.hasNext() ; )
        {
            Arg a = (Arg)iter.next() ;
            if ( argDecl.matches(a) )
                arg = a ;
        }
        return arg ;
    }
    
    public Arg getArg(String s)
    {
        s = ArgDecl.canonicalForm(s) ;
        Arg arg = null ;
        for ( Iterator iter = args.iterator() ; iter.hasNext() ; )
        {
            Arg a = (Arg)iter.next() ;
            if ( a.getName().equals(s) )
                arg = a ;
        }
        return arg ;
    }
    
    
    /** Add an argument to those to be accepted on the command line
     * @param argName Name
     * @return The CommandLine processor object
     */
   
    public CommandLine add(String argName, boolean hasValue)
    {
        return add(new ArgDecl(hasValue, argName)) ;
    }

    /** Add an argument object
     * @param arg Argument to add
     * @return The CommandLine processor object
     */
   
    public CommandLine add(ArgDecl arg)
    {
        for ( Iterator iter = arg.getNames() ; iter.hasNext() ; )
            argMap.put(iter.next(), arg) ;
        return this ;
    }
    
    public ArgHandler trace() 
    {
        final PrintStream _out = out ;
        return new ArgHandler()
            {
                public void action (String arg, String val) //throws java.lang.IllegalArgumentException
                {
                    if ( _out != null )
                        _out.println("Seen: "+arg+((val!=null)?" = "+val:"")) ;
                }
            } ;
    }
    
    
    public static void main(String[] argv)
    {
        CommandLine cl = new CommandLine() ;
        cl.setOutput(System.out) ;
        ArgDecl argA = new ArgDecl(false, "-a") ;
        cl.add(argA) ;
        cl.add("-b", false) ;
        cl.add("-file", true) ;
        
        ArgDecl argFile = new ArgDecl(false, "-v", "--verbose") ;
        argFile.addHook(cl.trace()) ;
        cl.add(argFile) ;
        
        //cl.setHook(cl.trace()) ;
        
        String[] a = new String[]{"-a", "--b", "--a", "--file", "value1", "--file", "value2", "--v", "rest"} ;
        try {
            cl.process(a) ;
            System.out.println("PROCESSED") ;
            
            // Checks
            if ( cl.getArg("file") == null )
                System.out.println("No --file seen") ;
            else
                System.out.println("--file => "+cl.getArg("file").getValue()) ;
            
            // Checks
            if ( cl.getArg(argA) == null )
                System.out.println("No --a seen") ;
            else
                System.out.println("--a seen "+cl.getArg(argFile).getValue()) ;
            
            System.out.println("DUMP") ;
            for ( Iterator iter = cl.args().iterator() ; iter.hasNext() ; )
            {
                Arg arg = (Arg)iter.next() ;
                String v = (arg.hasValue()? (" = "+arg.getValue()) : "") ;
                System.out.println("Arg: "+arg.getName()+v) ;
            }
            for ( Iterator iter = cl.items().iterator() ; iter.hasNext() ; )
                System.out.println("Item: "+(String)iter.next()) ;
            
        } catch (IllegalArgumentException ex)
        {
            System.err.println("Illegal argument: "+ex.getMessage() ) ;
        }
    }
}

/*
 *  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
