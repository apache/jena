package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.enhanced.*;
/**
 * @author jjc
 *
 */
public class Checker {
   static Personality personality = new GraphPersonality().
            add(CNodeI.class,CNode.factory);
            
}
