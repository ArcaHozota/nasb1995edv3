<!DOCTYPE html>
<html lang="ja-JP">
<body>
	<#assign principalAdmin = Session.SPRING_SECURITY_CONTEXT.authentication.principal
	        originalAdmin = principalAdmin.originalAdmin>
	<nav class="navbar navbar-expand-lg bg-body-tertiary fixed-top" data-bs-theme="dark">
		<div class="container-fluid">
			<div class="d-flex justify-content-start">
				<img src="/static/image/cross.svg" alt="img01" width="60" height="60">
				<a class="navbar-brand effect-shine" style="font-size: 33px;" href="/category/to-mainmenu">
					NASB1995
				</a>
			</div>
			<div class="collapse navbar-collapse" id="navbarSupportedContents">
				<div class="input-group d-flex justify-content-end">
					<input type="text" class="form-control" id="searchInput" placeholder="検索"
						style="max-width: 180px;">
					<button class="input-group-text bg-transparent p-0 px-2" id="searchBtn">
						<i class="fa-solid fa-magnifying-glass"></i>
					</button>
				</div>
				<ul class="navbar-nav mb-2 mb-lg-0 d-flex" id="personalInfo">
					<li class="nav-item dropdown">
						<a class="nav-link dropdown-toggle btn btn-primary me-2" href="#" role="button"
							data-bs-toggle="dropdown" aria-expanded="false" style="height: 37.6px;">
							<i class="fa-solid fa-user-circle"></i> ${originalAdmin.username()}
						</a>
						<ul class=" dropdown-menu dropdown-menu-end" role="menu">
							<li>
								<a class="dropdown-item" href="#" id="toPersonal">
									<i class="fa-solid fa-user-gear"></i> 個人スペース
									<input type="hidden" value="${originalAdmin.id()}">
								</a>
							</li>
							<li>
								<a class="dropdown-item" href="#" id="toMessage">
									<i class="fa-solid fa-comments"></i> メッセージ
								</a>
							</li>
							<form id="logoutForm" method="post" action="/category/do-logout"
								style="display: none;">
							</form>
							<#assign authNames = principalAdmin.getAuthorities()>
							<#list authNames as authName>
								<#if authName.getAuthority() == 'employee%delete' || authName.getAuthority() == 'employee%edition'>
									<#assign chkFlg = 'true'>
								</#if>
							</#list>
							<#if chkFlg?exists>
								<input type="hidden" value="${chkFlg}" id="authChkFlgContainer">
							<#else>
								<input type="hidden" value="false" id="authChkFlgContainer">
							</#if>
						</ul>
					</li>
				</ul>
				<div class="d-flex">
					<button id="logoutBtn" type="button" class="btn btn-danger me-2">
						<i class="fa-solid fa-right-from-bracket"></i>
					</button>
				</div>
			</div>
		</div>
	</nav>
</body>
</html>