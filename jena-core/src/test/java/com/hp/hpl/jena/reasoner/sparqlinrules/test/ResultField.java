/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hp.hpl.jena.reasoner.sparqlinrules.test;


/**
 *
 * @author mba
 */
public class ResultField extends Result{
        String var;
        String result;
        
        public ResultField(String pvar, String presult) {
            var = pvar;
            result = presult;
        }
        
        public boolean sameResult(String presult) {
            String vresult = presult.contains("^^") ? presult.substring(0, presult.indexOf("^^")): presult;
            
            return vresult.compareTo(result)==0;
        }
        
        public boolean sameResult(Result presult) {
            return sameResult(((ResultField) presult).result);
        }
        
        public boolean sameResult(String pvar, String presult) {
            return pvar.compareTo(var) == 0 && sameResult(presult);
        }
        
    }
