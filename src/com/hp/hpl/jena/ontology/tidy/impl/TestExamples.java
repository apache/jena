/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.ontology.tidy.impl;

import java.util.Iterator;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;
/**
 * @author Jeremy J. Carroll
 *  
 */
public class TestExamples {
    
    static public Iterator examples(int s, int p, int o) {
        if (useSameIndex(s, p) ) {
            if ( useSameIndex(o, p) ) {
                return new ArrayIterator(
                        new Triple[]{
                                new Triple(
                                        examples[s][0],
                                        examples[p][1],
                                        examples[o][2]
                                ),
                                new Triple(
                                        examples[s][0],
                                        examples[p][0],
                                        examples[o][1]
                                ),
                                new Triple(
                                        examples[s][0],
                                        examples[p][1],
                                        examples[o][0]
                                ),
                                new Triple(
                                        examples[s][1],
                                        examples[p][0],
                                        examples[o][0]
                                ),
                                new Triple(
                                        examples[s][0],
                                        examples[p][0],
                                        examples[o][0]
                                ),
                        }
                );
            } else {
                return new ArrayIterator(
                        new Triple[]{
                                new Triple(
                                        examples[s][0],
                                        examples[p][1],
                                        examples[o][0]
                                ),
                                new Triple(
                                        examples[s][0],
                                        examples[p][0],
                                        examples[o][0]
                                ),
                        }
                );
            }
        } else if( useSameIndex(o, p) 
                || useSameIndex(o, s) ) {

            return new ArrayIterator(
                    new Triple[]{
                            new Triple(
                                    examples[s][0],
                                    examples[p][0],
                                    examples[o][1]
                            ),
                            new Triple(
                                    examples[s][0],
                                    examples[p][0],
                                    examples[o][0]
                            ),
                    }
            );
        } else {

            return new ArrayIterator(
                    new Triple[]{
                            new Triple(
                                    examples[s][0],
                                    examples[p][0],
                                    examples[o][0]
                            ),
                    }
            );
        } 
    }
    
    static boolean useSameIndex(int a, int b) {
        if (a != b) {
            return (a == Grammar.userTypedLiteral && b == Grammar.userID)
            || (b == Grammar.userTypedLiteral && a == Grammar.userID);
        }
        return a == Grammar.propertyOnly || a == Grammar.userID
        || a == Grammar.blank;
    }
    
    static public Node examples[][] = new Node[Grammar.blank + 1][];
    static {
        for (int i = 0; i < examples.length; i++) {
            String nm = i<Grammar.catNames.length?Grammar.catNames[i]:"";
            if (nm.startsWith("rdfs")) {
                examples[i] = new Node[] { Node.create("rdfs:"
                        + nm.substring(4)) };
            } else if (nm.startsWith("owl") || nm.startsWith("rdf")) {
                examples[i] = new Node[] { Node.create(nm.substring(0, 3) + ":"
                        + nm.substring(3)) };
            } else {
                switch (i) {
                case Grammar.badID:
                    examples[i] = new Node[] { Node.create("rdfs:member") };
                break;
                case Grammar.annotationPropID:// rdfs:seeAlso ??
                    examples[i] = new Node[] { Node.create("rdfs:seeAlso") };
                break;
                case Grammar.datatypeID: //xsd:int
                    examples[i] = new Node[] { Node.create("xsd:int") };
                break;
                case Grammar.dataRangeID: //rdfs:Literal
                    examples[i] = new Node[] { Node.create("rdfs:Literal") };
                break;
                case Grammar.dataAnnotationPropID: // rdfs:label
                    examples[i] = new Node[] { Node.create("rdfs:label") };
                break;
                case Grammar.classID: // owl:Thing
                    examples[i] = new Node[] { Node.create("owl:Thing") };
                break;
                case Grammar.ontologyPropertyID: // owl:priorVersion
                    examples[i] = new Node[] { Node.create("owl:priorVersion") };
                break;
                case Grammar.dlInteger:
                    examples[i] = new Node[] { Node.create("3") };
                break;
                case Grammar.liteInteger:
                    examples[i] = new Node[] { Node.create("1") };
                break;
                case Grammar.literal:
                    examples[i] = new Node[] { Node.create("'a'") };
                break;
                case Grammar.userTypedLiteral:
                    examples[i] = new Node[] { Node.create("'a'eg:a1"),
                        Node.create("'a'eg:a2"), Node.create("'a'eg:a3"), };
                break;
                default:
                    if (i == Grammar.propertyOnly) {
                        examples[i] = new Node[] { Node.create("rdf:object"),
                                Node.create("rdf:predicate"), Node.create("rdf:subject"), };
                    } else if (i == Grammar.userID) {
                        examples[i] = new Node[] { Node.create("eg:a1"),
                                Node.create("eg:a2"), Node.create("eg:a3"), };
                    } else if (i == Grammar.blank) {
                        examples[i] = new Node[] { Node.create("_b1"),
                                Node.create("_b2"), Node.create("_b3"), };
                    }else if (i == Grammar.classOnly) {
                                            examples[i] = new Node[] { Node.create("rdf:Bag") };
                    }

                }
            }
        }
    }
}

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

