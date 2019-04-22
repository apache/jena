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
public class LongListConverter implements IStringConverter<List<Long>> {

    @Override
    public List<Long> convert(String longs) {
        String[] values = longs.split(",");
        List<Long> longList = new ArrayList<>();
        for (String val : values) {
            longList.add(Long.parseLong(val));
        }
        return longList;
    }

}
