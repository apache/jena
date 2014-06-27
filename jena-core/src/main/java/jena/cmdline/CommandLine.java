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

package jena.cmdline;

import java.io.* ;
import java.util.* ;

import com.hp.hpl.jena.util.FileUtils ;

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
 * <li>Does not allow multiple single letter options to be concatenated.</li>
 * <li>Options may be ended with - or --</li>
 * <li>Arguments with values can use "="</li>
 * </ul>
 */


public class CommandLine
{
    /* Extra processor called before the registered one when set.
     * Used for tracing.
     */
    protected ArgHandler argHook = null ;
    protected String usage = null ;
    protected Map<String, ArgDecl> argMap = new HashMap<>() ;
    protected Map<String, Arg> args = new HashMap<>() ;
    //protected boolean ignoreUnknown = false ;

    // Rest of the items found on the command line
    String indirectionMarker = "@" ;
    protected boolean allowItemIndirect = false ;   // Allow @ to mean contents of file
    boolean ignoreIndirectionMarker = false ;       // Allow comand line items to have leading @ but strip it.
    protected List<String> items = new ArrayList<>() ;


    /** Creates new CommandLine */
    public CommandLine()
    {
    }

    /** Set the global argument handler.  Called on every valid argument.
     * @param argHandler Handler
     */
    public void setHook(ArgHandler argHandler) { argHook = argHandler ; }

    public void setUsage(String usageMessage) { usage = usageMessage ; }

    public boolean hasArgs() { return args.size() > 0 ; }
    public boolean hasItems() { return items.size() > 0 ; }

    public Iterator<Arg> args() { return args.values().iterator() ; }
//    public Map args() { return args ; }
//    public List items() { return items ; }

    public int numArgs() { return args.size() ; }
    public int numItems() { return items.size() ; }
    public void pushItem(String s) { items.add(s) ; }

    public boolean isIndirectItem(int i)
    { return allowItemIndirect && items.get(i).startsWith(indirectionMarker) ; }

    public String getItem(int i)
    {
        return getItem(i, allowItemIndirect) ;
    }

    public String getItem(int i, boolean withIndirect)
    {
        if ( i < 0 || i >= items.size() )
            return null ;


        String item = items.get(i) ;

        if ( withIndirect && item.startsWith(indirectionMarker) )
        {
            item = item.substring(1) ;
            try { item = FileUtils.readWholeFileAsUTF8(item) ; }
            catch (Exception ex)
            { throw new IllegalArgumentException("Failed to read '"+item+"': "+ex.getMessage()) ; }
        }
        return item ;
    }


    /** Process a set of command line arguments.
     * @param argv The words of the command line.
     * @throws IllegalArgumentException Throw when something is wrong (no value found, action fails).
     */
    public void process(String[] argv) throws java.lang.IllegalArgumentException
    {
        List<String> argList = new ArrayList<>() ;
        argList.addAll(Arrays.asList(argv)) ;

        int i = 0 ;
        for ( ; i < argList.size() ; i++ )
        {
            String argStr = argList.get(i) ;
            if (endProcessing(argStr))
                break ;
            
            if ( ignoreArgument(argStr) )
                continue ;

            // If the flag has a "=" or :, it is long form --arg=value.
            // Split and insert the arg
            int j1 = argStr.indexOf('=') ;
            int j2 = argStr.indexOf(':') ;
            int j = Integer.MAX_VALUE ;

            if ( j1 > 0 && j1 < j )
                j = j1 ;
            if ( j2 > 0 && j2 < j )
                j = j2 ;

            if ( j != Integer.MAX_VALUE )
            {
                String a2 = argStr.substring(j+1) ;
                argList.add(i+1,a2) ;
                argStr = argStr.substring(0,j) ;
            }

            argStr = ArgDecl.canonicalForm(argStr) ;
            String val = null ;

            if ( argMap.containsKey(argStr) )
            {
                if ( ! args.containsKey(argStr))
                    args.put(argStr, new Arg(argStr)) ;

                Arg arg = args.get(argStr) ;
                ArgDecl argDecl = argMap.get(argStr) ;

                if ( argDecl.takesValue() )
                {
                    if ( i == (argList.size()-1) )
                        throw new IllegalArgumentException("No value for argument: "+arg.getName()) ;
                    i++ ;
                    val = argList.get(i) ;
                    arg.setValue(val) ;
                    arg.addValue(val) ;
                }

                // Global hook
                if ( argHook != null )
                    argHook.action(argStr, val) ;

                argDecl.trigger(arg) ;
            }
            else
                handleUnrecognizedArg( argList.get(i) );
//                if ( ! getIgnoreUnknown() )
//                    // Not recognized
//                    throw new IllegalArgumentException("Unknown argument: "+argStr) ;
        }

        // Remainder.
        if ( i < argList.size() )
        {
            if ( argList.get(i).equals("-") || argList.get(i).equals("--") )
                i++ ;
            for ( ; i < argList.size() ; i++ )
            {
                String item = argList.get(i) ;
                items.add(item) ;
            }
        }
    }

    /** Hook to test whether this argument should be processed further
     */
    public boolean ignoreArgument( String argStr )
    { return false ; }
    
    /** Answer true if this argument terminates argument processing for the rest
     * of the command line. Default is to stop just before the first arg that
     * does not start with "-", or is "-" or "--".
     */
    public boolean endProcessing( String argStr )
    {
        return ! argStr.startsWith("-") || argStr.equals("--") || argStr.equals("-");
    }

    /**
     * Handle an unrecognised argument; default is to throw an exception
     * @param argStr The string image of the unrecognised argument
     */
    public void handleUnrecognizedArg( String argStr ) {
        throw new IllegalArgumentException("Unknown argument: "+argStr) ;
    }


    /** Test whether an argument was seen.
     */

    public boolean contains(ArgDecl argDecl) { return getArg(argDecl) != null ; }

    /** Test whether an argument was seen.
     */

    public boolean contains(String s) { return getArg(s) != null ; }


    /** Test whether the command line had a particular argument
     *
     * @param argName
     */
    public boolean hasArg(String argName) { return getArg(argName) != null ; }

    /** Test whether the command line had a particular argument
     *
     * @param argDecl
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
        for ( Arg a : args.values() )
        {
            if ( argDecl.matches( a ) )
            {
                arg = a;
            }
        }
        return arg ;
    }

    /** Get the argument associated with the arguement name.
     *  Actually returns the LAST one seen
     *  @param arg Argument declaration to find
     *  @return Arg - Last argument that matched.
     */

    public Arg getArg(String arg)
    {
        arg = ArgDecl.canonicalForm(arg) ;
        return args.get(arg) ;
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
     * @param argName
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
    public List<String> getValues(ArgDecl argDecl)
    {
        Arg arg = getArg(argDecl) ;
        if ( arg == null )
            return null ;
        return arg.getValues() ;
    }

    /**
     * Returns all the values (0 or more strings) for an argument.
     * @param argName
     * @return List
     */
    public List<String> getValues(String argName)
    {
        Arg arg = getArg(argName) ;
        if ( arg == null )
            return null ;
        return arg.getValues() ;
    }



    /** Add an argument to those to be accepted on the command line.
     * @param argName Name
     * @param hasValue True if the command takes a (string) value
     * @return The CommandLine processor object
     */

    public CommandLine add(String argName, boolean hasValue)
    {
        return add(new ArgDecl(hasValue, argName)) ;
    }

    /** Add an argument to those to be accepted on the command line.
     *  Argument order reflects ArgDecl.
     * @param hasValue True if the command takes a (string) value
     * @param argName Name
     * @return The CommandLine processor object
     */

    public CommandLine add(boolean hasValue, String argName)
    {
        return add(new ArgDecl(hasValue, argName)) ;
    }

    /** Add an argument object
     * @param arg Argument to add
     * @return The CommandLine processor object
     */

    public CommandLine add(ArgDecl arg)
    {
        for ( Iterator<String> iter = arg.names() ; iter.hasNext() ; )
            argMap.put(iter.next(), arg) ;
        return this ;
    }

//    public boolean getIgnoreUnknown() { return ignoreUnknown ; }
//    public void setIgnoreUnknown(boolean ign) { ignoreUnknown = ign ; }

    /**
     * @return Returns whether items starting "@" have the value of named file.
     */
    public boolean allowItemIndirect()
    {
        return allowItemIndirect ;
    }

    /**
     * @param allowItemIndirect Set whether items starting "@" have the value of named file.

     */
    public void setAllowItemIndirect(boolean allowItemIndirect)
    {
        this.allowItemIndirect = allowItemIndirect ;
    }

    /**
     * @return Returns the ignoreIndirectionMarker.
     */
    public boolean isIgnoreIndirectionMarker()
    {
        return ignoreIndirectionMarker ;
    }

    /**
     * @return Returns the indirectionMarker.
     */
    public String getIndirectionMarker()
    {
        return indirectionMarker ;
    }

    /**
     * @param indirectionMarker The indirectionMarker to set.
     */
    public void setIndirectionMarker(String indirectionMarker)
    {
        this.indirectionMarker = indirectionMarker ;
    }

    /**
     * @param ignoreIndirectionMarker The ignoreIndirectionMarker to set.
     */
    public void setIgnoreIndirectionMarker(boolean ignoreIndirectionMarker)
    {
        this.ignoreIndirectionMarker = ignoreIndirectionMarker ;
    }

    public ArgHandler trace()
    {
        final PrintStream _out = System.err ;
        return new ArgHandler()
            {
                @Override
                public void action (String arg, String val) //throws java.lang.IllegalArgumentException
                {
                    if ( _out != null )
                        _out.println("Seen: "+arg+((val!=null)?" = "+val:"")) ;
                }
            } ;
    }
}
