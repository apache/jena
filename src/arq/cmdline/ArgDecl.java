/*
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package arq.cmdline;

import java.util.* ;

/** A command line argument specification.
 *
 * @author  Andy Seaborne
 */
public class ArgDecl
{
    boolean takesValue ;
    String firstName ; 
    List<String> names = new ArrayList<String>() ;
    public static final boolean HasValue = true ;
    public static final boolean NoValue = false ;
    
    private void init(boolean hasValue, String name)
    {
        takesValue = hasValue ;
        firstName = name ;
    }
    
    /** Create a declaration for a command argument.
     * 
     * @param hasValue  Does it take a value or not?
     * @param name      Name of argument
     */
    
    public ArgDecl(boolean hasValue, String name)
    {
        init(hasValue, name) ;
        addName(name) ;
    }

    /** Create a declaration for a command argument.
     * 
     * @param hasValue  Does it take a value or not?
     * @param name1      Name of argument
     * @param name2      Name of argument
     */

    public ArgDecl(boolean hasValue, String name1, String name2)
    {
        init(hasValue, name1) ;
        addName(name1) ;
        addName(name2) ;
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
        init(hasValue, name1) ;
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
     * @param name4      Name of argument
     */

    public ArgDecl(boolean hasValue, String name1, String name2, String name3, String name4)
    {
        init(hasValue, name1) ;
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
     * @param name5      Name of argument
     */

    public ArgDecl(boolean hasValue, String name1, String name2, String name3, String name4, String name5)
    {
        init(hasValue, name1) ;
        addName(name1) ;
        addName(name2) ;
        addName(name3) ;
        addName(name4) ;
        addName(name5) ;
    }
    
    /** Create a declaration for a command argument.
     * 
     * @param hasValue  Does it take a value or not?
     * @param name1      Name of argument
     * @param name2      Name of argument
     * @param name3      Name of argument
     * @param name4      Name of argument
     * @param name5      Name of argument
     * @param name6      Name of argument
     */

    public ArgDecl(boolean hasValue, String name1, String name2, String name3, String name4, String name5, String name6)
    {
        init(hasValue, name1) ;
        addName(name1) ;
        addName(name2) ;
        addName(name3) ;
        addName(name4) ;
        addName(name5) ;
        addName(name6) ;
    }
    
    public void addName(String name)
    {
        name = canonicalForm(name) ;
        if ( ! names.contains(name))
            names.add(name) ;
    }
    
    public String getKeyName() { return firstName ; }
    
    public List<String> getNames() { return names ; }
    public Iterator<String> names() { return names.iterator() ; }
    
    public boolean takesValue() { return takesValue ; }
    
    public boolean matches(Arg a)
    {
        for ( Iterator<String> iter = names.iterator() ; iter.hasNext() ; )
        {
            String n = iter.next() ;
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
 *  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
