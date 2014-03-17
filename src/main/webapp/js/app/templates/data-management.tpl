<div class="row">
  <div class="col-md-span-12">
    <table class='table'>
      <tr class="headings"><th>Active?</th><th>Name</th><th></th></tr>
      <% _.each( datasets, function( ds ) { %>
        <tr>
          <td><input type='checkbox' class='checkbox' checked /></td>
          <td>
            <%= ds.name() %>
          </td>
          <td>
            <a class="btn btn-sm action remove btn-primary" data-uri='<%= ds.mgmtURL() %>'><i class='fa fa-times-circle'></i> remove</a>
            <a class="btn btn-sm action backup btn-primary" data-uri='<%= ds.mgmtURL() %>'><i class='fa fa-download'></i> backup</a>
            <a class="btn btn-sm action configure btn-primary" href="admin-dataset-details.html#<%= ds.name() %>"><i class='fa fa-wrench'></i> configure</a>
            <a class="btn btn-sm action add-data btn-primary" href="upload.html?ds=<%= ds.name %>'><i class='fa fa-plus-circle'></i> add data</a>
          </td>
        </tr>
      <% }) %>

    </table>
  </div>
</div>
<div class="row">
  <div class="col-md-3 col-md-offset-9">
    <a href="admin-dataset-details.html" class="btn btn-sm btn-primary pull-right"><i class="fa fa-plus-circle"></i> add new dataset</a>
  </div>

</div>