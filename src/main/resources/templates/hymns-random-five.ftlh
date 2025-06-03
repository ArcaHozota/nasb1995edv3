<!DOCTYPE html>
<html lang="ja-JP">
<#include "include-header.ftlh">

<body>
	<style>
		#tableBody {
			font-family: "Noto Serif JP", serif;
		}
	</style>
	<#include "include-navbar.ftlh">
	<#include "include-sidebar.ftlh">
	<div class="container-fluid">
		<div class="row">
			<div class="col-sm-9 offset-sm-3 col-md-10 offset-md-2 main">
				<div class="card border-tunagray mb-3" style="margin-top: 1rem;">
					<div class="card-header text-bg-tunagray mb-3">
						<h5 class="card-title" style="padding-top: 8px;">
							<i class="fa-solid fa-grip-vertical"></i> 賛美歌ランドム選択
						</h5>
					</div>
					<div class="card-body">
						<div class="row">
							<form class="form-inline col-md-6 offset-md-3" role="form">
								<div class="input-group col-md-6">
									<input id="keywordInput" class="form-control" type="text"
										placeholder="キーワードを入力してください">
									<button id="randomSearchBtn" class="btn btn-info my-2 my-sm-0" type="button">
										<i class="fa-solid fa-magnifying-glass"></i> ランドム選択
									</button>
								</div>
							</form>
						</div>
						<table class="table table-sm table-hover">
							<caption style="font-size: 10px;">ランドム選択した賛美歌情報一覧</caption>
							<thead>
								<tr class="table-success">
									<th scope="col" class="text-center">名称</th>
								</tr>
							</thead>
							<tbody id="tableBody" class="table-group-divider"></tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript" src="/static/customizes/hymns-random-five.js" nonce="${nonce}"></script>
</body>

</html>