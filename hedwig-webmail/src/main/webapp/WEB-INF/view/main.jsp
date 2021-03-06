<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>Hedwig Web Mail</title>

<link rel="stylesheet" href="css/bootstrap.min.css"/>
<link rel="stylesheet" href="css/font-awesome.min.css"/>
<link rel="stylesheet" href="css/jquery-ui.css"/>
<link rel="stylesheet" href="css/skin-lion/ui.fancytree.css"/>
<link rel="stylesheet" href="css/summernote.css"/>
<link rel="stylesheet" href="css/default.css"/>

<script src="js/jquery/jquery.js"></script>
<script src="js/jquery/jquery-ui.js"></script>
<script src="js/jquery/jquery.fancytree.js"></script>
<script src="js/jquery/jquery.pagination.js"></script>
<script src="js/jquery/jquery.form.js"></script>
<script src="js/jquery/jquery.deserialize.js"></script>
<script src="js/jquery/jquery.slimscroll.js"></script>
<script src="js/bootstrap.js"></script>
<script src="js/eModal.js"></script>
<script src="js/summernote.js"></script>
<script src="js/hedwig.webmail.js"></script>
<script>
var wma_separator = '${store.folderSeparator}',
	invalid_chars = '~ . \\ / : * ? " < > |';

function addtab(tabid) {
	$('#tab').clone().attr('id','')
		.find('a').attr({'href':'#' + tabid, 'aria-controls': tabid})
		.end().appendTo($('#tablist'));
	return $('<div role="tabpanel" class="tab-pane"/>')
		.attr('id', tabid)
		.appendTo($('#tab-content'));
}
function loadtab(tabid, tabname, url, data) {
	$('#' + tabid).load(url, data, function() { showtab(tabid, tabname); });
}
function showtab(tabid, tabname) {
	if (!$('#' + tabid).length) return false;
	var tab = $('#tablist a[href=#' + tabid + ']').tab('show').closest('li').removeClass('hidden').end();
	if (tabname) tab.html(tab.has('button').length ? tab.html().replace(/.*(<[^\/].*\/.*>).*/, "$1" + tabname) : tabname);
	return true;
}
function removetab(tabid) {
	$('#tablist a[href=#main-tab]').tab('show');
	if (tabid === 'sub-tab') $('#tablist a[href=#sub-tab]').closest('li').addClass('hidden'), $('#sub-tab').empty();
	else $('#tablist a[href=#' + tabid + ']').closest('li').remove(), $('#' + tabid).remove();
}
function refresh() {
	$('#main-tab').load('folder/messages', $('#msg-list-form').serialize());
}
function gotopage(page) {
	$('#page').val(page);
	$('select[name=criteria]').val($('input[name=_criteria]').val());
	$('input[name=term]').val($('input[name=_term]').val());
	refresh();
}
function showQuota() {
	$.getJSON('quota', function(quotas) {
		if (quotas && quotas.length > 0) {
			if (quotas[0].resources && quotas[0].resources.length > 0) {
				var resource = quotas[0].resources[0];
				if (resource.limit > 0) {
					var percent = Math.min(100, Math.round((resource.usage / resource.limit) * 100));
					$('#quota').text(formatBytes(resource.usage) + ' of ' + formatBytes(resource.limit) + ' used')
						.parent().find('.progress-bar').css('width', percent + '%')
							.find('.sr-only').text(percent + '% used');

				} else {
					$('#quota').text(formatBytes(resource.usage) + ' used');
				}
			}
		}
	});
}
$(function() {
	$('#tree').fancytree({
		init: function(event, data) {
			data.tree.visit(function(node) {
				if (node.isLazy()) node.load();
			});
		},
		activate: function(event, data) {
			var node = data.node;
			loadtab('main-tab', node.title, 'folder/messages', $.param({path:node.key}));
		},
		lazyLoad: function(event, data) {
			var node = data.node;
			data.result = $.getJSON('folder/tree?' + $.param({path:node.key}));
		}
	});
	$('#tree').bind('fancytreefocustree', function(event, data) {
		if (data.node && !data.node.unselectable) {
			$('#side-menu').find('.active').removeClass('active');
			$(event.delegateTarget).addClass('active');
		}
	}).bind('fancytreebeforeactivate', function(event, data) {
		return !data.node.unselectable;
	});

	showQuota();

	$('#main-tab').load($('#main-tab > a').attr('href'));

	$('#tablist').on('click', 'li a > .close', function(e) {
		e.preventDefault(), e.stopPropagation();
		removetab($(this).closest('a').attr('href').substring(1));
	});
	$('#side-menu').on('click', 'a[data-target]', function(event) {
		var tree = $('#tree').fancytree('getTree'),
		    node = tree.getActiveNode(),
		    path = $(this).data('target'),
		    name = $(this).html().replace(/<.*[^\/]\/.*>/g, ''); // remove tags
		if (node) {
			node.setActive(false), node.setFocus(false);
		}
		loadtab('main-tab', name, 'folder/messages', $.param({path:path}));
	});
	$('#compose').click(function() {
		loadtab('sub-tab', $(this).text(), 'message/compose');
	});
	$('#manage-folder').click(function() {
		loadtab('sub-tab', $(this).text(), 'folder/manage');
	});
	$('#settings').on('click', function() {
		loadtab('sub-tab', $(this).text(), 'prefs');
	});

	$('#tab-content').on('click', '#refresh', refresh
	).on('click', '#reply, #replyall, #forward', function() {
		var data = $(this).closest('form').serializeArray();
		data.push({name:$(this).attr('id'),value:true});
		loadtab('sub-tab', $('#compose').text(), 'message/compose', $.param(data));
	}).on('click', '#delete-msg', function() {
		var form = $(this).closest('form'),
			path = form.find('input[name=path]').val()
			uid = form.find('input[name=uid]').val();
		eModal.confirm('<fmt:message key="message.confirm.delete"/>')
			.then(function() {
				$.post('message/delete', $.param({path:path,uids:uid}), function() {
					removetab('tab-' + uid);
					if ($('#path').val() == path
							&& $('#msg-list-form').find('input[name=uids][value=' + uid + ']').length > 0) {
						refresh();
					}
				});
			});
	}).on('click', '#raw-msg', function() {
		window.open('message/raw?' + $(this).closest('form').serialize());
	}).on('click', '#print', function() {
		window.open('message?' + $(this).closest('form').serialize() + '&print=true');
	}).on('click', '#prev, #next', function() {
		var form = $(this).closest('form'), 
			msg = serializeObject(form),
		    offset = $(this).attr('id') == 'prev' ? -1 : 1;
		$.getJSON('message/uid', {path:msg.path,number:msg.number,offset:offset}, function(uid) {
			if (!showtab('tab-' + uid)) {
				var pane = form.closest('.tab-pane'),
					tab = $('#tablist').find('a[href=#tab-' + msg.uid + ']'),
					title = tab.html();
				pane.load('message', $.param({path:msg.path,uid:uid}), function() {
					pane.attr('id', 'tab-' + uid);
					tab.attr('href', '#tab-' + uid).attr('aria-controls', 'tab-' + uid)
					   .html(title.replace(/.*(<[^\/].*\/.*>).*/, "$1" + pane.find('.mail-title').text()));
				});
			}
		});
	}).on('click', '.mail-attachment ul.attachment a[id]', function() {
		var form = $(this).closest('form');
		form.find('input[name=part]').val($(this).attr('id').substring(4));
		window.open('message/part?' + form.serialize());
	}).on('click', '.mail-body address', function() {
		$(this).toggleClass('showcc');
	});

	$('#modal').on('hidden.bs.modal', function() {
		$(this).find('.modal-body').empty();
	});
});
</script>
</head>
<body>
  <div id="header">
    <p class="navbar-text navbar-right">
      <a href="logout" class="navbar-link"><i class="fa fa-sign-out"></i> Log out</a>
    </p>
  </div>
  <aside id="menu">
    <div id="navigation">
      <div class="media">
        <a class="media-left" href="#">
          <img src="images/logo.jpg" class="img-circle">
        </a>
        <div class="media-body">
          <a href="http://hwmail.sourceforge.net">HEDWIG</a>
          <p><small>WEBMAIL</small></p>
        </div>
      </div>
      <div class="m-15">
        <a class="btn btn-block btn-primary" id="compose"><fmt:message key="menu.compose"/></a>
      </div>
      <ul class="nav nav-stacked" id="side-menu">
        <li role="presentation">
          <a data-target="INBOX">
            <i class="fa fa-inbox"></i> <fmt:message key="prefs.inbox"/>
            <span id="inbox-unread" class="label label-warning pull-right"></span>
          </a>
        </li>
        <li role="presentation">
          <a data-target="${prefs.sentMailArchive}">
            <i class="fa fa-paper-plane-o"></i> <fmt:message key="prefs.sentmailarchive"/>
          </a>
        </li>
        <li role="presentation">
          <a data-target="${prefs.toSendFolder}">
            <i class="fa fa-clock-o"></i> <fmt:message key="prefs.tosendfolder"/>
          </a>
        </li>
        <li id="trashInfo" role="presentation">
          <a data-target="${prefs.trashFolder}">
            <i class="fa fa-trash-o"></i> <fmt:message key="prefs.trashfolder"/>
          </a>
        </li>
        <li role="presentation">
          <a data-target="${prefs.draftFolder}">
            <i class="fa fa-pencil-square-o"></i> <fmt:message key="prefs.draftfolder"/>
          </a>
        </li>
        <li role="presentation">
            <div id="tree">
              <ul>
	              <li id="${prefs.personalFolder}" class="folder lazy">
	              	<fmt:message key="prefs.personalfolder"/>
	              </li>
<c:if test="${not empty namespaces}">
	<c:forEach var="ns" items="${namespaces}">
                  <li id="${ns.path}" class="folder lazy unselectable">${ns.name}</li>
	</c:forEach>
</c:if>
              </ul>
            </div>
        </li>
        <li>
        	<a id="manage-folder"><i class="fa fa-wrench"></i> <fmt:message key="menu.folder.manage"/></a>
        </li>
      	<li>
      	  <a id="settings"><i class="fa fa-cogs"></i> <fmt:message key="menu.settings"/></a>
      	</li>
      </ul>
      <div class="sidebar-quota">
        <div id="quota" class="text-right text-muted">{0} of {1} used</div>
        <div class="progress progress-bar-xs">
          <div class="progress-bar progress-bar-primary" role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="width:0%;">
            <span class="sr-only"></span>
          </div>
        </div>
      </div><!-- /.sidebar-quota -->
    </div><!-- /#navigation -->
  </aside><!-- /#menu -->
  <div id="wrapper">
    <div class="content">
      <ul id="tablist" class="nav nav-tabs" role="tablist">
        <li role="presentation" class="hidden" id="tab">
          <a role="tab" data-toggle="tab">
          	<button class="close">x</button> Loading...
          </a>
        </li>
        <li role="presentation" class="active">
        	<a href="#main-tab" aria-controls="main-tab" role="tab" data-toggle="tab">
        		<fmt:message key="prefs.inbox" />
        	</a>
        </li>
        <li role="presentation" class="hidden">
        	<a href="#sub-tab" aria-controls="sub-tab" role="tab" data-toggle="tab">
        		<button class="close">x</button> {0}
        	</a>
        </li>
      </ul>
      <div id="tab-content" class="tab-content">
        <div role="tabpanel" class="tab-pane active" id="main-tab">
        	<a href="<c:url value='folder/messages'>
      			<c:param name='path' value='INBOX'/>
      			<c:param name='criteria' value='${prefs.inboxType}'/>
      			<c:param name='pageSize' value='${prefs.pageSize}'/>
  			</c:url>"></a>
  			<!-- Simple splash message -->
  			<div class="splash">
  				<h1><i class="fa fa-spinner fa-spin fa-lg"></i></h1>
  			</div>
        </div><!-- /#main-tab.tab-pane -->
        <div role="tabpanel" class="tab-pane" id="sub-tab">
        </div><!-- /#sub-tab.tab-pane -->
      </div><!-- /.tab-content -->
    </div><!-- /.content -->
    <footer id="footer" class="footer">
      <span class="pull-right">HEDWIG WEBMAIL</span>
    </footer>
  </div><!-- #wrapper -->
  <!-- Modal dialog template -->
  <div id="modal" class="modal fade">
    <div class="modal-dialog">
      <div class="modal-content">
      	<div class="modal-header">
        	<button type="button" class="close" data-dismiss="modal" aria-label="Close">
        		<span aria-hidden="true">&times;</span>
        	</button>
        	<h4 class="modal-title">Modal title</h4>
      	</div>
        <div class="modal-body text-center">
        </div>
        <div class="modal-footer">
        	<button type="button" class="btn btn-default" data-dismiss="modal"><fmt:message key="menu.close"/></button>
    	</div>
      </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
  </div><!-- /.modal -->
</body>
</html>