/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
 
 * * $Id: ARPResource.java,v 1.1 2009-06-29 08:55:38 castagna Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/
/*
 * ARPResource.java
 *
 * Created on June 25, 2001, 9:57 PM
 */

package com.hp.hpl.jena.rdf.arp.impl;


/**
 *
 * @author  jjc
 */
public class ARPResource extends TaintImpl implements  AResourceInternal {
    // Constants cribbed from com.megginson.sax.rdf.RDFFilter
    static public final boolean DEBUG = false;
    final private XMLHandler arp;
    
    final String nodeID;
    
    static private int genIdCounter = 0;
    final private int genId = genIdCounter++;
    static String dummy = "http://jena.hpl.hp.com/arp/not/a/real/uri/";
	static String nullDummy = "nullpointerexception://jena.hpl.hp.com/arp/";
    
    public ARPResource(XMLHandler parent) {
        this(parent,null);
    } 
    public ARPResource(XMLHandler parent, String nid) {
        arp = parent;
        nodeID = nid;
        if (DEBUG) {
            RuntimeException rte = new RuntimeException("bnode allocated here");
            rte.fillInStackTrace();
            userData = rte;
        }
            
    }
          
    
  
    
    

    // AResource interface.
    @Override
    public boolean isAnonymous() {
        return true;
    }
    
    @Override
    public String getAnonymousID() {
        return nodeID==null
                ? ( "A" + Integer.toString(genId) )
                : "U" + nodeID;
    }
    
    
    @Override
    public String getURI() {
        return null;
    }
    
    @Override
    public String toString() {
        return "_:"+getAnonymousID();
    }
    
    @Override
    public int hashCode() {
       return nodeID==null ?genId: nodeID.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if ( o == null || !(o instanceof ARPResource))
            return false;
        if ( this == o)
          return true;
//        AResourceInternal a=(AResourceInternal)o;
//        if ( uri != null )
//		      return (!a.isAnonymous()) && uri.equals(a.getURI());
		    ARPResource aa = (ARPResource)o;
        return nodeID != null && nodeID.equals(aa.nodeID);
    }
    
    private Object userData;
    
    @Override
    public Object getUserData() {
//    	if ( uri != null )
//    	  throw new IllegalStateException("User data only supported on blank nodes");
        return nodeID == null ?
                userData : arp.getUserData(nodeID);
    }
    
    @Override
    public void setUserData(Object d) {
//    	if ( uri != null )
//    	  throw new IllegalStateException("User data only supported on blank nodes");
     	if ( nodeID == null ) 
            userData = d;
        else
            arp.setUserData(nodeID,d);
    }

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.AResource#hasNodeID()
	 */
	@Override
    public boolean hasNodeID() {
		return nodeID!=null;
	}
  private boolean used = false;

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.AResourceInternal#setHasBeenUsed()
	 */
	@Override
    public void setHasBeenUsed() {
		used = true;
	} 


	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.AResourceInternal#getHasBeenUsed()
	 */
	@Override
    public boolean getHasBeenUsed() {
		return used;
	}
    
}
