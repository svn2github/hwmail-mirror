<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<div class="wrapper-content">
	<div class="page-header">
		<h2>Aliases <small>${domain}</small></h2>
		<div class="page-tools">
			<form class="pull-right page-search">
				<div class="input-group">
					<input type="text" class="form-control input-sm" name="search" placeholder="Search"/>
					<div class="input-group-btn">
	                	<button type="submit" class="btn btn-sm btn-primary">Search</button>
	            	</div>
				</div>
			</form>
			<button id="create" class="btn btn-default btn-sm">New</button>
			<button id="delete" class="btn btn-default btn-sm">Delete</button>
		</div>
	</div>
	<form id="alias-form">
		<input type="hidden" id="page" value="<c:out value='${pager.pageNumber}' />"/>
		<table id="alias-table" class="table table-hover table-condensed">
			<thead>
				<tr>
					<th><input type="checkbox"/></th>
					<th>Alias</th>
					<th>Redirect to</th>
					<th>&nbsp;</th>
				</tr>
			</thead>
			<tbody>
			  <c:if test="${not empty aliases}">
			   <c:forEach var="alias" items="${aliases}">
				<tr>
					<td><input type="checkbox" name="ID" value="<c:out value='${alias.ID}'/>"/></td>
					<td><a href="#"><c:out value="${alias.alias}"/></a></td>
					<td><c:out value="${alias.deliverTo}"/></td>
					<td>&nbsp;</td>
				</tr>
			   </c:forEach>
			  </c:if>
			  <c:forEach begin="${fn:length(aliases) + 1}" end="${pager.pageSize}">
			  	<tr>
			  		<td colspan="4">&nbsp;</td>
			  	</tr>
			  </c:forEach>
			</tbody>
		</table>
	</form>
	<div class="text-center">
		<ul class="pagination" id="pagination"></ul>
	</div>
</div>

<script>
$(function() {
    $('#pagination').pagination(<c:out value="${pager.itemCount}" />, {
    	items_per_page: <c:out value="${pager.pageSize}" />,
        current_page: $('#page').val(),
        callback: gotopage
    });
    
    var buttons = [
       			{ text: 'Close', style: 'default', close: true },
       			{ text: 'OK', style: 'primary', close: false, click: saveOrUpdateAlias }
           ];
    
    function saveOrUpdateAlias() {
    	$('.modal-body').load($('#main-form').attr('action'),
    		$('form[name=userForm]').serializeArray());
    }
    
    $('#alias-table').on('click', 'a', function() {
    	var uid = $(this).parents('tr:first').find('input[name=ID]').val();
    	eModal.ajax({
    		url: $('#main-form').attr('action') + '/' + uid + '/update',
    		title: 'Update Alias',
    		buttons: buttons
    	});
    });
    
    $('#create').on('click', function() {
    	eModal.ajax({
    		url: $('#main-form').attr('action') + '/add',
    		title: 'Add Aliases',
    		buttons: buttons
    	});
    });
    
    $('#delete').on('click', function() {
		var $checked = $('input[name=ID]:checked');
		if ($checked.length > 0) {
	    	$.post($('#main-form').attr('action') + '/delete',
	    		$('#alias-form').serializeArray(), function() {
	    			gotopage($('#page').val());
	    		});
    	} else return eModal.alert('Select aliases!');
    });
});
</script>