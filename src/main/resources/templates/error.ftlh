<!DOCTYPE html>
<html lang="ja-JP">
<head>
	<title>NASB1995</title>
	<meta charset="UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="_csrf_header" content="${_csrf.headerName}">
	<meta name="_csrf_token" content="${_csrf.token}">
	<link rel="stylesheet" type="text/css" href="/static/bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet" type="text/css" href="/static/css/customize.css">
	<link rel="shortcut icon" type="image/x-icon" href="/static/favicon.ico">
	<link rel="preconnect" href="https://fonts.googleapis.com">
	<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
	<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Corinthia:wght@700&family=Noto+Sans+JP:wght@100..900&display=swap">
	<script type="text/javascript" src="/static/customizes/constants.js"></script>
</head>

<body>
	<style>
		.bg-dark {
			background-color: #000000 !important;
		}
	</style>
	<nav class="navbar navbar-dark bg-dark fixed-top" role="navigation">
		<div class="container-fluid">
			<div class="d-flex justify-content-start">
				<img src="/static/image/cross.svg" alt="img01" width="60" height="60">
				<a class="navbar-brand effect-shine" style="font-size: 33px;" href="#">
					NASB1995
				</a>
			</div>
		</div>
		<form id="logoutForm" method="post" action="/category/do-logout" style="display: none;"></form>
	</nav>
	<div class="container" style="margin-top: 60px;">
		<div class="text-center">
			<h1>システム情報</h1>
			<p style="margin-top: 33px;font-size: 22px;">${exception}</p>
		</div>
		<div class="d-flex align-items-center justify-content-center" style="margin-top: 33px;">
		  <button id="backBtn" style="width: 300px;" class="btn btn-lg btn-warning btn-block">戻る</button>
		</div>
	</div>
	<script type="text/javascript" src="/static/customizes/error.js"></script>
</body>

</html>