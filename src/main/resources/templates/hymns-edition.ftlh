<!DOCTYPE html>
<html lang="ja-JP">
<#include "include-header.ftlh">

<body>
	<style>
		label {
			font-family: "Noto Serif JP", serif;
			font-weight: 600;
		}
	</style>
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
						<li class="breadcrumb-item active" aria-current="page">データ更新</li>
					</ol>
				</nav>
				<div class="card border-reflexblue mb-3">
					<div class="card-header text-bg-reflexblue mb-3">
						<h5 class="card-title" style="padding-top: 8px;">
							<i class="fa-solid fa-bars-staggered"></i> 賛美歌情報更新
						</h5>
					</div>
					<div class="card-body">
						<form id="editForm">
							<input type="hidden" value="${pageNum}" id="pageNumContainer">
							<input type="hidden" value="${arawaseta.id()}" id="idContainer">
							<input type="hidden" value="${arawaseta.updatedTime()}" id="datestampContainer">
							<div class="form-group row">
								<label for="nameJpEdit" class="col-sm-3 col-form-label text-end">日本語名称</label>
								<div class="col-sm-7" style="height: 8vh;">
									<input type="text" class="form-control" value="${arawaseta.nameJp()}" id="nameJpEdit"
										placeholder="日本語名称を入力してください">
									<span class="form-text" style="font-size: 10px;margin-top: 0px;"></span>
								</div>
							</div>
							<div class="form-group row">
								<label for="nameKrEdit" class="col-sm-3 col-form-label text-end">韓国語名称</label>
								<div class="col-sm-7" style="height: 8vh;">
									<input type="text" class="form-control" value="${arawaseta.nameKr()}" id="nameKrEdit"
										placeholder="韓国語名称を入力してください">
									<span class="form-text" style="font-size: 10px;margin-top: 0px;"></span>
								</div>
							</div>
							<div class="form-group row">
								<label for="linkEdit" class="col-sm-3 col-form-label text-end">リンク</label>
								<div class="col-sm-7" style="height: 8vh;">
									<input type="text" class="form-control" value="${arawaseta.link()}" id="linkEdit"
										placeholder="リンクを入力してください">
									<span class="form-text" style="font-size: 10px;margin-top: 0px;"></span>
								</div>
							</div>
							<div class="form-group row">
								<label for="serifEdit" class="col-sm-3 col-form-label text-end">セリフ</label>
								<div class="col-sm-7" style="height: 26vh;">
									<textarea class="form-control" style="height: 100%" id="serifEdit"
										placeholder="セリフを入力してください">${arawaseta.serif()}</textarea>
									<span class="form-text" style="font-size: 10px;margin-top: 0px;"></span>
								</div>
							</div>
							<div class="form-group row">
								<p class="col-sm-7 offset-sm-3" style="font-size: 10px;margin-top: 0px;margin-bottom: 0px;">
									最終更新者：${arawaseta.updatedUser()}＠${arawaseta.updatedTime()}日本標準時間
								</p>
							</div>
						</form>
					</div>
					<div class="card-footer d-flex justify-content-end">
						<button type="button" class="btn btn-primary my-2 my-sm-0" id="infoUpdationBtn">
							<i class="fa-solid fa-compact-disc"></i> 更新
						</button>
						<button type="button" class="btn btn-secondary my-2 my-sm-0" id="restoreBtn">
							<i class="fa-regular fa-trash-can"></i> 廃棄
						</button>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript" src="/static/customizes/hymns-addition.js" nonce="${nonce}"></script>
</body>

</html>