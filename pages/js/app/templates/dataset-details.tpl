<div class="row">
  <div class="col-md-span-12">
    <ul class="nav nav-tabs">
      <li class="active"><a href="#simple-edit" data-toggle="tab">simple configuration</a></li>
      <li><a href="#advanced-edit" data-toggle="tab">advanced configuration editor</a></li>
      <li><a href="#upload" data-toggle="tab">upload Fuseki config file</a></li>
    </ul>

    <!-- Tab panes -->
    <div class="tab-content">
      <div class="tab-pane active" id="simple-edit">
        <p>&nbsp;</p>
        <form class="form-horizontal" role="form">
          <div class="form-group">
            <label for="datasetName" class="col-sm-2 control-label">Dataset name</label>
            <div class="col-sm-10">
              <input type="text" class="form-control" id="datasetName" placeholder="dataset name"
                <%= newDataset ? "" : ( "value='" + datasetId + "'") %>
               />
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label">Dataset type</label>
            <div class="col-sm-10">
              <div class="radio">
                <label>
                  <input type="radio" name="optionsDatasetType" id="optionsDatasetTypeMem" value="mem" checked>
                  In-memory &ndash; dataset will be recreated when Fuseki restarts, but contents will be lost
                </label>
              </div>
              <div class="radio">
                <label>
                  <input type="radio" name="optionsDatasetType" id="optionsDatasetTypeTDB" value="tdb">
                  Persistent &ndash; dataset will persist across Fuseki restarts
                </label>
              </div>
            </div>
          </div>

          <div class="row controls">
            <div class="col-md-3">
              <a href="admin-data-management.html" class="btn btn-sm btn-default"><i class="fa fa-mail-reply"></i> cancel</a>
              <a href="#" class="btn btn-sm btn-primary"><i class="fa fa-check"></i> <%= commitAction %></a>
            </div>
          </div>
        </form>
      </div>

      <div class="tab-pane" id="advanced-edit">
        <div class="row">
          <p>Advanced editor - codemirror edit box to appear here</p>
        </div>
        <div class="row controls">
          <div class="col-md-3">
            <a href="admin-data-management.html" class="btn btn-sm btn-default"><i class="fa fa-mail-reply"></i> cancel</a>
            <a href="#" class="btn btn-sm btn-primary"><i class="fa fa-check"></i> <%= commitAction %></a>
          </div>
        </div>
      </div>

      <div class="tab-pane" id="upload">
        <div class="row">
          <p class="col-sm-12">If you have a Fuseki config file (i.e. a Jena assembler description),
          you can upload it here:</p>
        </div>
        <div class="row controls">
          <div class="col-md-3">
            <a href="admin-data-management.html" class="btn btn-sm btn-default"><i class="fa fa-mail-reply"></i> cancel</a>
            <a href="#" class="btn btn-sm btn-primary"><i class="fa fa-upload"></i> upload config file</a>
          </div>
        </div>
        </div>
    </div>

  </div><!-- /.col-md-span-12 -->
</div><!-- /.row -->

