<div class="col-md-span-12">
  <table class='table'>
    <tr class="headings"><th>Name</th><th>Services</th><th></th></tr>
    <% _.each( datasets, function( ds ) { %>
      <tr>
        <td>
          <%= ds.name() %>
        </td>
        <td>
          <% _.each( ds.serviceTypes(), function( st ) { %>
            <span class='badge'><%= st %></span>
          <% }) %>
        </td>
        <td>
          <a class="btn btn-sm action remove btn-primary" href="query.html?ds=<%= ds.name() %>"><i class='fa fa-question-circle'></i> query</a>
          <a class="btn btn-sm action remove btn-primary" href="upload.html?ds=<%= ds.name() %>"><i class='fa fa-plus-circle'></i> add data</a>

<!--          
          <a class="btn btn-sm action remove btn-primary" href="explore.html?ds=<%= ds.name() %>"><i class='fa fa-globe'></i> explore</a>
-->          
          <a class="btn btn-sm action configure btn-primary" href="admin-stats.html?ds=<%= ds.name() %>"><i class='fa fa-dashboard'></i> stats</a>
        </td>
      </tr>
    <% }) %>

  </table>
</div>
