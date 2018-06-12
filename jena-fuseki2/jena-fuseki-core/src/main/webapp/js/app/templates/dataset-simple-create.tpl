<div class="row">
  <div class="col-md-12">
      <div class="" id="simple-edit">
        <form class="form-horizontal" role="form">
          <div class="form-group">
            <label for="datasetName" class="col-sm-2 control-label">Dataset name</label>
            <div class="col-sm-10">
              <div class="validation-warning dbNameValidation">A name for the dataset is required</div>
              <input type="text" class="form-control" name="dbName" placeholder="dataset name" />
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label">Dataset type</label>
            <div class="col-sm-10">
              <div class="radio">
                <label>
                  <input type="radio" name="dbType" value="mem" checked>
                  In-memory &ndash; dataset will be recreated when Fuseki restarts, but contents will be lost
                </label>
              </div>
              <div class="radio">
                <label>
                  <input type="radio" name="dbType" value="tdb">
                  Persistent &ndash; dataset will persist across Fuseki restarts
                </label>
              </div>
              <div class="radio">
                <label>
                  <input type="radio" name="dbType" value="tdb2">
                  Persistent (TDB2) &ndash; dataset will persist across Fuseki restarts
                </label>
              </div>
            </div>
          </div>

          <div class="row">
            <div class="col-md-12">
              <div class="errorOutput bg-warning"></div>
            </div>
          </div>

          <div class="row controls">
            <div class="col-md-3 col-md-offset-9">
              <a href="#" class="btn btn-sm btn-primary action commit simple"><i class="fa fa-check"></i> create dataset</a>
            </div>
          </div>
        </form>
      </div>

      <!--
      <div class="tab-pane" id="upload">
        <p>&nbsp;</p>
        <div class="row">
          <p class="col-sm-12">If you have a Fuseki config file (i.e. a Jena assembler description),
          you can upload it here:</p>
        </div>
        <div class="row controls">
          <form id="uploadForm" method="post" action="$/datasets" class="form-horizontal col-sm-12">
            <div class="form-group">
              <label for="assemblerFile" class="col-sm-2 control-label">Configuration file</label>
              <div class="col-sm-10">
                <div class="validation-warning assemblerFileValidation">A file name is required</div>
                <input type="file" class="form-control" name="assemblerFile" />
              </div>
            </div>
          </form>
        </div>

        <div class="row">
          <div class="errorOutput col-sm-12"></div>
        </div>

        <div class="row">
          <div class="col-sm-12">
            <a href="admin-data-management.html" class="btn btn-sm btn-default"><i class="fa fa-mail-reply"></i> cancel</a>
            <a href="#" class="btn btn-sm btn-primary action upload"><i class="fa fa-upload"></i> upload config file</a>
          </div>
        </div>
      </div>
      -->

  </div><!-- /.col-md-12 -->
</div><!-- /.row -->


