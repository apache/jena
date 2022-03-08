package org.apache.jena.riot.rowset.rw;

import java.io.InputStream;
import java.util.Objects;

import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.RowSetReader;
import org.apache.jena.riot.rowset.RowSetReaderFactory;
import org.apache.jena.sparql.exec.QueryExecResult;
import org.apache.jena.sparql.exec.RowSetBuffered;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.util.Context;

public class RowSetReaderJSONStreaming
    implements RowSetReader
{
    public static final RowSetReaderFactory factory = lang -> {
        if (!Objects.equals(lang, ResultSetLang.RS_JSON ) )
            throw new ResultSetException("RowSet for JSON asked for a "+lang);
        return new RowSetReaderJSONStreaming();
    };

    @Override
    public QueryExecResult readAny(InputStream in, Context context) {
        return process(in, context);
    }


    static public QueryExecResult process(InputStream in, Context context) {
        QueryExecResult result = null;
        RowSetBuffered<RowSetJSONStreaming> rs = RowSetJSONStreaming.createBuffered(in, context);

        // If there are no bindings we check for an ask result
        if (!rs.hasNext()) {
            // Unwrapping in order to access the ask result
            RowSetJSONStreaming inner = rs.getDelegate();
            Boolean askResult = inner.getAskResult();

            if (askResult != null) {
                result = new QueryExecResult(askResult);
            }
        }

        if (result == null) {
            result = new QueryExecResult(rs);
        }

        return result;
    }
}
