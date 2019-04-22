/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.jena.jena_fuseki_geosparql.cli;

import com.beust.jcommander.IStringConverter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */
public class IntegerListConverter implements IStringConverter<List<Integer>> {

    @Override
    public List<Integer> convert(String integers) {
        String[] values = integers.split(",");
        List<Integer> integerList = new ArrayList<>();
        for (String val : values) {
            integerList.add(Integer.parseInt(val));
        }
        return integerList;
    }

}
