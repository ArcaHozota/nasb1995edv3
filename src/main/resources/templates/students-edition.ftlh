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
							<a href="#" style="text-decoration: none;" id="toStudentsPages">データリスト</a>
						</li>
						<li class="breadcrumb-item active" aria-current="page">データ更新</li>
					</ol>
				</nav>
				<div class="card border-reflexblue mb-3">
					<div class="card-header text-bg-reflexblue mb-3">
						<h5 class="card-title" style="padding-top: 8px;">
							<i class="fa-solid fa-bars-staggered"></i> ユーザ情報更新
						</h5>
					</div>
					<div class="card-body">
						<form id="editForm">
							<input type="hidden" value="${arawaseta.id}" id="idContainer">
							<div class="form-group row">
								<label for="accountEdit" class="col-sm-3 col-form-label text-end">アカウント</label>
								<div class="col-sm-7" style="height: 9vh;">
									<input type="text" class="form-control" value="${arawaseta.loginAccount}"
										id="accountEdit" placeholder="アカウントを入力してください">
									<span class="form-text" style="font-size: 10px;"></span>
								</div>
							</div>
							<div class="form-group row">
								<label for="nameEdit" class="col-sm-3 col-form-label text-end">名称</label>
								<div class="col-sm-7" style="height: 9vh;">
									<input type="text" class="form-control" value="${arawaseta.username}"
										id="nameEdit" placeholder="名称を入力してください">
									<span class="form-text" style="font-size: 10px;"></span>
								</div>
							</div>
							<div class="form-group row">
								<label for="passwordEdit" class="col-sm-3 col-form-label text-end">パスワード</label>
								<div class="col-sm-7" style="height: 9vh;">
									<input type="password" class="form-control" value="${arawaseta.password}"
										id="passwordEdit" placeholder="パスワードを入力してください">
									<span class="form-text" style="font-size: 10px;"></span>
								</div>
							</div>
							<div class="form-group row">
								<label for="birthdayEdit" class="col-sm-3 col-form-label text-end">生年月日</label>
								<div class="col-sm-7" style="height: 9vh;">
									<input type="date" class="form-control" value="${arawaseta.dateOfBirth}"
										id="birthdayEdit" placeholder="生年月日を入力してください">
									<span class="form-text" style="font-size: 10px;"></span>
								</div>
							</div>
							<div class="form-group row">
								<label for="emailEdit" class="col-sm-3 col-form-label text-end">メール</label>
								<div class="col-sm-7" style="height: 9vh;">
									<input type="text" class="form-control" value="${arawaseta.email}"
										id="emailEdit" placeholder="メールを入力してください">
									<span class="form-text" style="font-size: 10px;"></span>
								</div>
							</div>
						</form>
					</div>
					<div class="card-footer d-flex justify-content-end">
						<button type="button" class="btn btn-primary my-2 my-sm-0" id="infoUpdateBtn">
							<i class="fa-solid fa-bolt-lightning"></i> 更新
						</button>
						<button type="button" class="btn btn-secondary my-2 my-sm-0" id="restoreBtn">
							<i class="fa-regular fa-trash-can"></i> 廃棄
						</button>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript" src="/static/customizes/students-edition.js" nonce="${nonce}"></script>
</body>

</html>