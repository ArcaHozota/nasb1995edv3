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
						<li class="breadcrumb-item"><a href="#" style="text-decoration: none;"
								id="toMainmenu2">メインメニュー</a></li>
						<li class="breadcrumb-item"><a href="#" style="text-decoration: none;"
								id="toHymnPages">データリスト</a></li>
						<li class="breadcrumb-item active" aria-current="page">データ追加</li>
					</ol>
				</nav>
				<div class="card border-darkgreen mb-3">
					<div class="card-header text-bg-darkgreen mb-3">
						<h5 class="card-title" style="padding-top: 8px;">
							<i class="fa-solid fa-bars-staggered"></i> 賛美歌情報追加
						</h5>
					</div>
					<div class="card-body">
						<form id="inputForm">
							<input type="hidden" value="${pageNum}" id="pageNumContainer">
							<div class="form-group row">
								<label for="nameJpInput" class="col-sm-3 col-form-label text-end">日本語名称</label>
								<div class="col-sm-7" style="height: 8vh;">
									<input type="text" class="form-control" id="nameJpInput"
										placeholder="日本語名称を入力してください">
									<span class="form-text" style="font-size: 10px;margin-top: 0px;"></span>
								</div>
							</div>
							<div class="form-group row">
								<label for="nameKrInput" class="col-sm-3 col-form-label text-end">韓国語名称</label>
								<div class="col-sm-7" style="height: 8vh;">
									<input type="text" class="form-control" id="nameKrInput"
										placeholder="韓国語名称を入力してください">
									<span class="form-text" style="font-size: 10px;margin-top: 0px;"></span>
								</div>
							</div>
							<div class="form-group row">
								<label for="linkInput" class="col-sm-3 col-form-label text-end">リンク</label>
								<div class="col-sm-7" style="height: 8vh;">
									<input type="text" class="form-control" id="linkInput" placeholder="リンクを入力してください">
									<span class="form-text" style="font-size: 10px;margin-top: 0px;"></span>
								</div>
							</div>
							<div class="form-group row">
								<label for="serifInput" class="col-sm-3 col-form-label text-end">セリフ</label>
								<div class="col-sm-7" style="height: 26vh;">
									<textarea class="form-control" style="height: 100%" id="serifInput"
										placeholder="セリフを入力してください"></textarea>
									<span class="form-text" style="font-size: 10px;margin-top: 0px;"></span>
								</div>
							</div>
						</form>
					</div>
					<div class="card-footer d-flex justify-content-end">
						<button type="button" class="btn btn-success my-2 my-sm-0" id="infoStorageBtn">
							<i class="fa-solid fa-music"></i> 追加
						</button>
						<button type="button" class="btn btn-secondary my-2 my-sm-0" id="resetBtn">
							<i class="fa-solid fa-arrow-rotate-left"></i> リセット
						</button>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript" src="/static/customizes/hymns-addition.js" nonce="${nonce}"></script>
</body>

</html>