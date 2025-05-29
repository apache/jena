package org.apache.jena.sparql.exec.tracker;

import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.update.UpdateRequest;

public abstract class UpdateExecBase
    implements UpdateExec
{
    protected String updateRequestString;
    protected UpdateRequest updateRequest;

    public UpdateExecBase(UpdateRequest updateRequest, String updateRequestString) {
        super();
        // this.datasetGraph = datasetGraph;
        this.updateRequest = updateRequest;
        this.updateRequestString = updateRequestString;
    }

//    public DatasetGraph getDatasetGraph() {
//        return datasetGraph;
//    }

    @Override
    public UpdateRequest getUpdateRequest() {
        return updateRequest;
    }

    @Override
    public String getUpdateRequestString() {
        return updateRequestString;
    }
}
