/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */
package com.hp.hpl.jena.util.tuple;

/**
 * The unit found in a line of a tuple.
 * Can be a string (quoted, possibly with the datatype, or unquoted) or a URI.
 * @author   Andy Seaborne
 * @version  $Id: TupleItem.java,v 1.2 2003-03-10 09:50:43 andy_seaborne Exp $
 */
public class TupleItem
{
    public static final int URI      = 0 ;
    public static final int STRING   = 1 ;
    public static final int UNKNOWN  = 2 ;
    public static final int UNQUOTED = 3 ;
    public static final int ANON     = 4 ;

    String rep ;
    String datatype ;
    String asFound ;
    int itemType ;

    TupleItem(String value, String valAsFound, int type, String dt)
    {
        rep = value ;
        asFound = valAsFound ;
        itemType = type ;
        datatype = dt ;
    }

    public int getType() { return itemType ; }

    public boolean isURI()       { return itemType == URI ; }
    public boolean isString()    { return itemType == STRING ; }
    public boolean isUnknown()   { return itemType == UNKNOWN ; }
    public boolean isUnquoted()  { return itemType == UNQUOTED ; }
    public boolean isAnon()      { return itemType == ANON ; }

    public String get() { return rep ; }
    public String getDT() { return datatype ;
    }
    public String asQuotedString() { return asFound ; }
    public String toString() { return rep ; }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2001
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
