/*
 *  (c) Copyright 2001  Hewlett-Packard Development Company, LP
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
 *
 * $Id: DiHyperCube.java,v 1.3 2003-08-27 13:07:11 andy_seaborne Exp $
 *
 * DiHyperCube.java
 *
 * Created on June 29, 2001, 9:36 PM
 */

package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * A theoretical graph for testing purposes.
 * All nodes are anonymous resources. All edges are labelled
 * rdf:value.
 * The basic DiHyperCube consists of the nodes being the
 * corners of a hypercube (e.g. in 3D a cube)
 * with the statements being the edges of the cube, directed
 * from one corner labelled 2^n-1 to the opposite corner 
 * labelled 0. The labels are not present in the model.
 * This basic graph is then extended, for test purposes
 * by duplicating a node.
 *
 * @author  jjc
 * @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.3 $' Date='$Date: 2003-08-27 13:07:11 $'
 */
class DiHyperCube extends java.lang.Object {
    final private Resource corners[];
    final private int dim;
    final private Model model;
    
    private int id = 2000;
    /** Creates new DiHyperCube */
    public DiHyperCube(int dimension, Model m)  {
        dim = dimension;
        model = m;
        corners = new Resource[1<<dim];
        for (int i=0;i<corners.length;i++) {
            corners[i] = m.createResource();
           // ((ResourceImpl)corners[i]).id = 1000 + i;
        }
        for (int i=0;i<corners.length;i++)
            addDown(i,corners[i]);
    }
    
    private void addDown(int corner,Resource r)  {
        for (int j=0;j<dim;j++) {
            int bit = 1<<j;
            if ( (corner & bit) != 0 ) {
                model.add(r,RDF.value,corners[corner^bit]);
            }
        }
    }
    
    DiHyperCube dupe(int corner)  {
        Resource dup = model.createResource();
        //dup.id = id++;
        for (int j=0;j<dim;j++) {
            int bit = 1<<j;
            if ( (corner & bit) != 0 ) {
                model.add(dup,RDF.value,corners[corner^bit]);
            } else {
                model.add(corners[corner^bit],RDF.value,dup);
            }
        }
        return this;
    }
    
    static int bitCount(int i) {
        return java.math.BigInteger.valueOf(i).bitCount();
    }
    /*
     * We have two DiHyperCube's 
      *   to one we have added N a1's 
     *    to the other we have added N b1's 
     * Returns true if they are equal.
     */
    static boolean equal(int a1,  int b1) {
        return  bitCount(a1)==bitCount(b1);
    }
    /*
     * We have two DiHyperCube's 
      *   to one we have added N a1's and N a2's.
     *    to the other we have added N b1's and N b2's.
     * Returns true if they are equal.
     */
    static boolean equal(int a1, int a2, int b1, int b2) {
        return  bitCount(a1^a2)==bitCount(b1^b2)
           && bitCount(a1&a2)==bitCount(b1&b2)
           && bitCount(a1|a2)==bitCount(b1|b2)
           && Math.min(bitCount(a1),bitCount(a2))==
              Math.min(bitCount(b1),bitCount(b2));
    }

}
