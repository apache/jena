/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hp.hpl.jena.reasoner.sparqlinrules.test;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import java.util.ArrayList;

/**
 *
 * @author mba
 */
public class CompareResults {
    
    public static boolean sameResult(ResultSet r1, ResultSet r2) {
        ArrayList<Binding> list_res1 = new ArrayList<Binding>();
        ArrayList<Binding> list_res2 = new ArrayList<Binding>();
        
        while(r1.hasNext()){
            list_res1.add(r1.nextBinding());
        };
        
        while(r2.hasNext()){
            list_res2.add(r2.nextBinding());
        };

         
        return (list_res1.containsAll(list_res2) && list_res2.containsAll(list_res1));
    }
    
    public static boolean sameResult (ArrayList<? extends Result> vl1, ArrayList<? extends Result> vl2) {
            boolean retV = (vl1.size() == vl2.size());

            if(retV) {

                boolean cont = true;
                while(cont && vl1.size()>0) {
                    int pos = getPosAL(vl1.get(0), vl2);
                    if(pos>=0) {
                        vl2.remove(pos);
                        vl1.remove(0);
                    }
                    else {
                        cont = false;
                    }
                }
               retV = retV && vl2.isEmpty();
            }

            return retV;
        }
        
    public static int getPosAL(Result o, ArrayList<? extends Result> alO) {
            int pos = -1;
            int i=0;
            boolean found = false; 

            while(i<alO.size() && !found) {
                if(alO.get(i).sameResult(o)) {
                    pos = i;
                    found = true;
                }
                else {
                    i++;
                }
            }

            return pos; 
        }

    }
