/*
 * (c) Copyright 2002, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package jena.cmdline ;

import java.util.* ;

/** A command line argument specification.
 *
 * @author  Andy Seaborne
 * @version $Id: ArgDecl.java,v 1.3 2003-08-27 13:03:54 andy_seaborne Exp $
 */
public class ArgDecl
{
    boolean takesValue ;
    Set names = new HashSet() ;
    boolean takesArg = false ;
    List argHooks = new ArrayList() ;
    public static final int FLAG = 1 ;         // i.e. no value
    public static final int OPTION = 2 ;       // takes a value as well
    
    
    public ArgDecl(boolean hasValue)
    {
        takesValue = hasValue ;
    }
    
    // Convenience constructors
    
    public ArgDecl(boolean hasValue, String name)
    {
        this(hasValue) ;
        addName(name) ;
    }
    public ArgDecl(boolean hasValue, String name1, String name2)
    {
        this(hasValue) ;
        addName(name1) ;
        addName(name2) ;
    }
    
    public ArgDecl(boolean hasValue, String name1, String name2, String name3)
    {
        this(hasValue) ;
        addName(name1) ;
        addName(name2) ;
        addName(name3) ;
    }
    
    public void addName(String name)
    {
        name = canonicalForm(name) ;
        names.add(name) ;
    }
    
    public Iterator getNames() { return names.iterator() ; }
    
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
 *  (c) Copyright 2002 Hewlett-Packard Development Company, LP
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
