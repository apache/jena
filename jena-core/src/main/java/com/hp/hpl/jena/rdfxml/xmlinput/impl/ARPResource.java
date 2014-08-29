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

/*
 * ARPResource.java
 *
 * Created on June 25, 2001, 9:57 PM
 */

package com.hp.hpl.jena.rdfxml.xmlinput.impl;

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
