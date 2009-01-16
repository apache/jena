/*
  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: HashUtils.java,v 1.11 2009-01-16 18:03:19 andy_seaborne Exp $
*/
package com.hp.hpl.jena.util;

import java.util.*;

/**
     HashUtils
     @author kers
     @deprecated use CollectionFactory methods
*/
@Deprecated
public class HashUtils
    {
    public static <K,V> Map<K,V> createMap() { return new HashMap<K,V>(); }
    
    public static <K,V> Map<K,V> createMap( int size ) { return new HashMap<K,V>( size ); }
    
    public static <K,V> Map<K,V> createMap( Map<K,V> toCopy ) { return new HashMap<K,V>( toCopy ); }
        
    public static <T> Set<T> createSet() { return new HashSet<T>(); }
    
    public static <T> Set<T> createSet( Collection<T>  toCopy ) { return new HashSet<T>( toCopy ); }
    }


/*
	(c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions
	are met:

	1. Redistributions of source code must retain the above copyright
   	notice, this list of conditions and the following disclaimer.

	2. Redistributions in binary form must reproduce the above copyright
   	notice, this list of conditions and the following disclaimer in the
   	documentation and/or other materials provided with the distribution.

	3. The name of the author may not be used to endorse or promote products
   	derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
	IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
	OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
	IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
	INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
	DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
	THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/