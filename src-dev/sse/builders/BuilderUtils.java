/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sse.builders;

import sse.ItemList;
import sse.ItemLocation;

class BuilderUtils
{
    protected static void warning(ItemLocation location, String msg)
    {
        msg = msg(location, msg) ;
        System.err.println(msg) ;
    }

    protected static void checkLength(int len1, int len2, ItemList list, String sym)
    {
        if ( list.size() < len1 || list.size()> len2 )
            broken(list, "Wrong number of arguments: want="+len1+" to "+len2+": actual="+list.size()) ;
    }
    
    protected static void checkLength(int len, ItemList list, String sym)
    {
        if ( list.size() != len )
            broken(list, "Wrong number of arguments: want="+len+" ; actual="+list.size()) ;
    }
    
    protected static void broken(ItemLocation location, String msg)
    {
        msg = msg(location, msg) ;
        System.err.println(msg) ;
        throw new ExprBuildException(msg) ;
    }
    
    private static String msg(ItemLocation location, String msg)
    {
        if ( location != null )
            msg = location.location()+" "+msg ;
        return msg ;
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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