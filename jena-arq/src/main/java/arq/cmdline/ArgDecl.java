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

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

/** A command line argument specification. */
public class ArgDecl
{
    boolean takesValue ;
    String firstName ; 
    List<String> names = new ArrayList<>() ;
    public static final boolean HasValue = true ;
    public static final boolean NoValue = false ;
    
    private void init(boolean hasValue, String name)
    {
        takesValue = hasValue ;
        firstName = canonicalForm(name) ;
    }
    
    /** Create a declaration for a command argument.
     * 
     * @param hasValue  Does it take a value or not?
     * @param name      Name of argument
     * @param names     Alternative names
     */
    
    public ArgDecl(boolean hasValue, String name, String...names)
    {
        init(hasValue, name) ;
        addName(name) ;
        for ( String n : names )
            addName(n) ;
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
        for ( String n : names )
        {
            if ( a.getName().equals( n ) )
            {
                return true;
            }
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
