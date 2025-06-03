<!DOCTYPE html>
<html lang="ja-JP">
<#include "include-header.ftlh">

<body>
	<#include "include-navbar.ftlh">
	<#include "include-sidebar.ftlh">
	<div class="container-fluid">
		<div class="row">
			<div class="col-sm-9 offset-sm-3 col-md-10 offset-md-2 main">
				<nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='10' height='10'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='%236c757d'/%3E%3C/svg%3E&#34;);"
					aria-label="breadcrumb">
					<ol class="breadcrumb" style="margin: 10px 0 0 0;">
						<li class="breadcrumb-item">
							<a href="#" style="text-decoration: none;" id="toMainmenu2">メインメニュー</a>
						</li>
						<li class="breadcrumb-item">
							<a href="#" style="text-decoration: none;" id="toHymnPages">データリスト</a>
						</li>
						<li class="breadcrumb-item active" aria-current="page">楽譜アプロード</li>
					</ol>
				</nav>
				<div class="card border-burgundy mb-3">
					<div class="card-header text-bg-burgundy mb-3">
						<h5 class="card-title" style="padding-top: 8px;">
							<i class="fa-solid fa-bars-staggered"></i> 賛美歌楽譜アプロード
						</h5>
					</div>
					<div class="card-body">
						<form id="inputForm">
							<input type="hidden" value="${pageNum}" id="pageNumContainer">
							<input type="hidden" value="${arawaseta.id()}" id="idContainer">
							<div class="form-group row">
								<label for="scoreEdit" class="col-sm-3 col-form-label text-end">楽譜</label>
								<div class="col-sm-7" style="height: 8vh;">
									<input type="file" class="form-control" name="file" id="scoreEdit" accept=".pdf,.jpg,.png,.jpeg" required>
								</div>
							</div>
						</form>
					</div>
					<div class="card-footer d-flex justify-content-end">
						<button type="button" class="btn btn-danger my-2 my-sm-0" id="scoreUploadBtn">
							<i class="fa-brands fa-soundcloud"></i> アプロード
						</button>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript" src="/static/customizes/hymns-score-upload.js" nonce="${nonce}"></script>
</body>

</html>