/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import java.util.*;

public class Arg
{
    String name ;
    String value ;                                      // Last seen
    List<String> values = new ArrayList<String>() ;     // All seen
    
    Arg() { name = null ; value = null ; }
    
    Arg(String _name) { this() ; setName(_name) ; }
    
    Arg(String _name, String _value) { this() ; setName(_name) ; setValue(_value) ; }
    
    void setName(String n) { name = n ; }
    
    void setValue(String v) { value = v ; }
    void addValue(String v) { values.add(v) ; }
    
    public String getName() { return name ; }
    public String getValue() { return value; }
    public List<String> getValues() { return values; }
    
    public boolean hasValue() { return value != null ; }
    
    public boolean matches(ArgDecl decl)
    {
        return decl.getNames().contains(name) ;
    }
    
    @Override
    public String toString()
    {
        String base = (( name.length() == 1 )?"-":"--") + name ;
        if ( getValues().size() == 0 )
            return base ;

        String str = "" ;
        String sep = "" ;
        
        for ( Iterator<String> iter = getValues().iterator() ; iter.hasNext() ; )
        {
            String v = iter.next() ;
            str = str + sep + base + "=" + v ;
            sep = " " ;
        }
        return str ;
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
