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
 * @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1 $' Date='$Date: 2009-06-29 08:55:39 $'
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
