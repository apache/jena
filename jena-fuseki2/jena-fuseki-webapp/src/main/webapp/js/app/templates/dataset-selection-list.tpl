<div class="col-md-span-12">
  <% if (datasets.length > 0) { %>
    <table class='table ijd'>
      <tr class="headings"><th>dataset name</th><th>actions</th></tr>
      <% _.each( datasets, function( ds ) { %>
        <tr>
          <td>
            <%- ds.name() %>
          </td>
          <td>
            <a class="btn btn-sm action remove btn-primary" href="dataset.html?tab=query&ds=<%- ds.name() %>"><i class='fa fa-question-circle'></i> query</a>
            <a class="btn btn-sm action remove btn-primary" href="dataset.html?tab=upload&ds=<%- ds.name() %>"><i class='fa fa-upload'></i> add data</a>
            <a class="btn btn-sm action configure btn-primary" href="dataset.html?tab=info&ds=<%- ds.name() %>"><i class='fa fa-dashboard'></i> info</a>
          </td>
        </tr>
      <% }) %>

    </table>
   <% } else { %>
    <p>There are no datasets on this server yet. <a href="manage.html?tab=new-dataset">Add one.</a></p>
   <% } %>
</div>
