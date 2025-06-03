<!DOCTYPE html>
<html lang="ja-JP">
<#include "include-header.ftlh">

<body>
	<style type="text/css">
		#booksKanriMainmenu:hover h2 {
			color: #7f0020;
		}

		#hymnsKanriMainmenu:hover h2 {
			color: #002fa7;
		}

		#randomKanriMainmenu:hover h2 {
			color: #006400;
		}
	</style>
	<#include "include-navbar.ftlh">
	<#include "include-sidebar.ftlh">
	<div class="container-fluid">
		<div class="row">
			<div class="col-md-10 ms-auto main">
				<#if loginMsg?exists>
					<input type="hidden" value="${loginMsg}" id="torokuMsgContainer">
				<#else>
					<input type="hidden" id="torokuMsgContainer">
				</#if>
				<h1 class="page-header" style="color: #7F0020; margin: 10px 0 0 0;">メインメニュー</h1>
				<div class="row">
					<div class="col" id="booksKanriMainmenu" role="button" style="height: 55vh;">
						<img src="/static/image/icons/burgundy.svg" style="height: 100%; width: 100%;"
							class="rounded" alt="img02">
						<h2 class="text-center">聖書奉読</h2>
					</div>
					<div class="col" id="hymnsKanriMainmenu" role="button" style="height: 55vh;">
						<img src="/static/image/icons/bourbon.svg" style="height: 100%; width: 100%;"
							class="rounded" alt="img03">
						<h2 class="text-center">賛美歌集め</h2>
					</div>
					<div class="col" id="randomKanriMainmenu" role="button" style="height: 55vh;">
						<img src="/static/image/icons/newaragon.svg" style="height: 100%; width: 100%;"
							class="rounded" alt="img04">
						<h2 class="text-center">ランドム選択</h2>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript" src="/static/customizes/mainmenu.js" nonce="${nonce}"></script>
</body>

</html>