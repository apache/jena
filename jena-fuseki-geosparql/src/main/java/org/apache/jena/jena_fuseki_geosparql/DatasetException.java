/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.jena.jena_fuseki_geosparql;

/**
 *
 *
 */
public class DatasetException extends Exception {

    public DatasetException(String msg) {
        super(msg);
    }

    public DatasetException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
