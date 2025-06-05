<!DOCTYPE html>
<html lang = "ja-JP">

<head>
	<title>NASB1995</title>
	<meta charset="UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="_csrf_header" content="${_csrf.headerName}">
	<meta name="_csrf_token" content="${_csrf.token}">
	<link rel="stylesheet" type="text/css" href="/static/bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet" type="text/css" href="/static/css/index.css">
	<link rel="stylesheet" type="text/css" href="/static/css/customize.css">
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
	<link rel="shortcut icon" type="image/x-icon" href="/static/favicon.ico">
	<link rel="preconnect" href="https://fonts.googleapis.com">
	<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
	<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Niconne&family=Noto+Sans+JP:wght@100..900&family=Noto+Serif+JP:wght@200..900&display=swap">
	<style>
		#indexTablehead,
		#randomSearchBtn,
		#tableBody, 
		#hintDiv {
			font-family: "Noto Serif JP", serif;
			font-weight: 600;
		}
		
		a.effect-shine {
			font-family: "Niconne", serif;
		}
		
		.navbar {
			background-color: #000000 !important;
		}
	</style>
	<script type="text/javascript" src="/static/bootstrap/js/bootstrap.bundle.min.js"></script>
	<script type="text/javascript" src="/static/layer/neo-layer.js"></script>
	<script type="text/javascript" src="/static/customizes/constants.js"></script>
	<script type="text/javascript" src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head>

<body>
	<nav class="navbar navbar-expand-lg bg-body-tertiary fixed-top" data-bs-theme="dark">
		<div class="container-fluid">
			<div class="d-flex justify-content-start">
				<img src="/static/image/cross.svg" alt="img01" width="60" height="60">
				<a class="navbar-brand effect-shine" style="font-size: 33px;" href="/home/index">
					NASB1995
				</a>
			</div>
			<div class="d-flex justify-content-end">
				<a href="/category/login" type="button" class="btn btn-primary me-2" style="font-weight: 600;">
					<i class="fa-solid fa-right-to-bracket"></i> ログイン
				</a>
			</div>
		</div>
	</nav>
	<#if torokuMsg?exists!''>
		<input type="hidden" value="${torokuMsg}" id="torokuMsgContainer">
	</#if>
	<div class="col-sm-6 offset-sm-3 col-md-8 offset-md-2 main">
		<div class="card border-darkgreen" style="margin-top: 2.2rem;">
			<div class="card-header text-bg-darkgreen mb-3">
				<h5 class="card-title" style="padding-top: 8px;font-weight: 500;">
					<i class="fa-solid fa-cross"></i> 賛美歌選択
				</h5>
			</div>
			<div class="card-body">
				<div class="row">
					<form class="form-inline col-md-6 offset-md-3" role="form" style="padding-right: 0px;">
						<div class="input-group col-md-6">
							<input id="keywordInput" class="form-control" type="text" placeholder="キーワードを入力してください">
							<button id="randomSearchBtn" class="btn btn-danger my-2 my-sm-0" type="button">
								<i class="fa-solid fa-magnifying-glass"></i> ランドム選択
							</button>
						</div>
					</form>
					<div class="col-md-3" style="padding-left: 0px;">
						<button type="button" class="btn btn-primary me-2" id="toIchiranHyoBtn">
							<i class="fa-solid fa-circle-info"></i>
						</button>
					</div>
				</div>
				<div class="background" id="loadingBackground" style="display: none;">
					<div class="circle" id="loadingContainer" style="display: none;">
						<div class="large">
							<div class="medium">
								<div class="small">
									<div class="core"></div>
								</div>
							</div>
						</div>
						<h1>LOADING</h1>
					</div>
				</div>
				<table class="table table-sm table-hover" id="indexTable">
					<thead id="indexTablehead">
						<tr class="table-success">
							<th scope="col" class="text-center">名称</th>
						</tr>
					</thead>
					<tbody id="tableBody" class="table-group-divider">
						<tr>
							<td>1</td>
						</tr>
						<tr>
							<td>2</td>
						</tr>
						<tr>
							<td>3</td>
						</tr>
						<tr>
							<td>4</td>
						</tr>
						<tr>
							<td>5</td>
						</tr>
					</tbody>
				</table>
				<p class="d-flex justify-content-end" style="font-size: 10px;color: #353542;">賛美歌${totalRecords}曲レコード済み</p>
			</div>
		</div>
		<div class="row" id="hintDiv">
			<p style="font-size: 11px;color: #FFF;">※赤色：アキュレイトクエリの結果、黄色：ファジークエリの結果。五つに満たされなかった場合にシステムが補充されます。</p>
		</div>
	</div>
	<script type="text/javascript" src="/static/customizes/index.js"></script>
</body>

</html>