/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hp.hpl.jena.reasoner.sparqlinrules.test;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author mba
 */
public class ResultRow extends Result {
        ArrayList<ResultField> result = new ArrayList();
        HashMap<String, Integer> resultMap = new HashMap<>();
        
        public ResultRow () {

        }
        
        public ResultRow (ResultField [] resultField) {
         }
        
        public ResultRow (ArrayList<ResultField> resultField) {
            for(ResultField rf : resultField) {
                addField(rf);
            }
        }

        public void addField(ResultField rf) {
            if(resultMap.containsKey(rf.var)) {
                result.remove(resultMap.get(rf.var));
            }
            result.add(rf);
            resultMap.put(rf.var, result.size()-1);
        }
        
        public void print() {
            for(ResultField rf : result) {
                System.out.print(rf.var + " -> " +rf.result+"; ");
            }
        }
        

        public boolean sameResult(Result presult) {
            ArrayList<ResultField> r1 = new ArrayList<>();
            r1.addAll(result);
            ArrayList<ResultField> r2 = new ArrayList<>();
            r2.addAll(((ResultRow) presult).result);
            
            return CompareResults.sameResult(r1, r2);
        }
        
    }
