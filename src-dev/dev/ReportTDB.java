package dev;


import java.io.FileInputStream;
import java.io.InputStream;

import atlas.logging.Log;

import com.hp.hpl.jena.riot.JenaReaderTurtle2;

public class ReportTDB
{
    public static void main(String[] args) throws Exception{
        Log.setLog4j() ;
        //TDB.init();
        InputStream input = new FileInputStream("D.ttl") ;
        JenaReaderTurtle2.parse(input) ;
        System.out.println("END") ;
        System.exit(0) ;
    }



}
