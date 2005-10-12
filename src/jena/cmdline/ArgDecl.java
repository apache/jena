/*
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package jena.cmdline;

import java.util.* ;

/** A command line argument specification.
 *
 * @author  Andy Seaborne
 * @version $Id: ArgDecl.java,v 1.7 2005-10-12 10:27:29 ian_dickinson Exp $
 */
public class ArgDecl
{
    boolean takesValue ;
    Set names = new HashSet() ;
    boolean takesArg = false ;
    List argHooks = new ArrayList() ;
    public static final boolean HasValue = true ;
    public static final boolean NoValue = false ;

    /** Create a declaration for a command argument.
     *
     * @param hasValue  Does it take a value or not?
     */

    public ArgDecl(boolean hasValue)
    {
        takesValue = hasValue ;
    }

    /** Create a declaration for a command argument.
     *
     * @param hasValue  Does it take a value or not?
     * @param name      Name of argument
     */

    public ArgDecl(boolean hasValue, String name)
    {
        this(hasValue) ;
        addName(name) ;
    }

    /** Create a declaration for a command argument.
     *
     * @param hasValue  Does it take a value or not?
     * @param name      Name of argument
     * @param handler   ArgHandler
     */

    public ArgDecl(boolean hasValue, String name, ArgHandler handler)
    {
        this(hasValue) ;
        addName(name) ;
        addHook( handler );
    }

    /** Create a declaration for a command argument.
     *
     * @param hasValue  Does it take a value or not?
     * @param name1      Name of argument
     * @param name2      Name of argument
     */

    public ArgDecl(boolean hasValue, String name1, String name2)
    {
        this(hasValue) ;
        addName(name1) ;
        addName(name2) ;
    }

    /** Create a declaration for a command argument.
     *
     * @param hasValue  Does it take a value or not?
     * @param name1      Name of argument
     * @param name2      Name of argument
     * @param handler   ArgHandler
     */

    public ArgDecl(boolean hasValue, String name1, String name2, ArgHandler handler)
    {
        this(hasValue) ;
        addName(name1) ;
        addName(name2) ;
        addHook( handler );
    }

    /** Create a declaration for a command argument.
     *
     * @param hasValue  Does it take a value or not?
     * @param name1      Name of argument
     * @param name2      Name of argument
     * @param name3      Name of argument
     */

    public ArgDecl(boolean hasValue, String name1, String name2, String name3)
    {
        this(hasValue) ;
        addName(name1) ;
        addName(name2) ;
        addName(name3) ;
    }

    /** Create a declaration for a command argument.
     *
     * @param hasValue  Does it take a value or not?
     * @param name1      Name of argument
     * @param name2      Name of argument
     * @param name3      Name of argument
     * @param handler   ArgHandler
     */

    public ArgDecl(boolean hasValue, String name1, String name2, String name3, ArgHandler handler)
    {
        this(hasValue) ;
        addName(name1) ;
        addName(name2) ;
        addName(name3) ;
        addHook( handler );
    }

    /** Create a declaration for a command argument.
     *
     * @param hasValue  Does it take a value or not?
     * @param name1      Name of argument
     * @param name2      Name of argument
     * @param name3      Name of argument
     * @param name4      Name of argument
     */

    public ArgDecl(boolean hasValue, String name1, String name2, String name3, String name4)
    {
        this(hasValue) ;
        addName(name1) ;
        addName(name2) ;
        addName(name3) ;
        addName(name4) ;
    }

    /** Create a declaration for a command argument.
     *
     * @param hasValue  Does it take a value or not?
     * @param name1      Name of argument
     * @param name2      Name of argument
     * @param name3      Name of argument
     * @param name4      Name of argument
     * @param handler    ArgHandler
     */

    public ArgDecl(boolean hasValue, String name1, String name2, String name3, String name4, ArgHandler handler)
    {
        this(hasValue) ;
        addName(name1) ;
        addName(name2) ;
        addName(name3) ;
        addName(name4) ;
        addHook( handler );
    }

    /** Create a declaration for a command argument.
     *
     * @param hasValue  Does it take a value or not?
     * @param name1      Name of argument
     * @param name2      Name of argument
     * @param name3      Name of argument
     * @param name4      Name of argument
     * @param name5      Name of argument
     * @param handler    ArgHandler
     */

    public ArgDecl(boolean hasValue, String name1, String name2, String name3, String name4, String name5, ArgHandler handler)
    {
        this(hasValue) ;
        addName(name1) ;
        addName(name2) ;
        addName(name3) ;
        addName(name4) ;
        addName(name5) ;
        addHook( handler );
    }

    public void addName(String name)
    {
        name = canonicalForm(name) ;
        names.add(name) ;
    }

    public Set getNames() { return names ; }
    public Iterator names() { return names.iterator() ; }

    // Callback model

    public void addHook(ArgHandler argHandler)
    {
        argHooks.add(argHandler) ;
    }

    protected void trigger(Arg arg)
    {
        for ( Iterator iter = argHooks.iterator() ; iter.hasNext() ; )
        {
            ArgHandler handler = (ArgHandler)iter.next() ;
            handler.action(arg.getName(), arg.getValue()) ;
        }
    }

    public boolean takesValue() { return takesValue ; }

    public boolean matches(Arg a)
    {
        for ( Iterator iter = names.iterator() ; iter.hasNext() ; )
        {
            String n = (String)iter.next() ;
            if ( a.getName().equals(n) )
                return true ;
        }
        return false ;
    }

    public boolean matches(String arg)
    {
        arg = canonicalForm(arg) ;
        return names.contains(arg) ;
    }

    static String canonicalForm(String str)
    {
        if ( str.startsWith("--") )
            return str.substring(2) ;

        if ( str.startsWith("-") )
            return str.substring(1) ;

        return str ;
    }
}

/*
 *  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
