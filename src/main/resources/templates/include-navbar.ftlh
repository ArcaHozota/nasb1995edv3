<!DOCTYPE html>
<html lang="ja-JP">
<body>
	<nav class="navbar navbar-expand-lg bg-body-tertiary fixed-top" data-bs-theme="dark">
		<div class="container-fluid">
			<div class="collapse navbar-collapse" id="navbarSupportedContents">
				<div class="input-group d-flex justify-content-end">
					<input type="text" class="form-control" id="searchInput" placeholder="検索"
						style="max-width: 180px;">
					<button class="input-group-text bg-transparent p-0 px-2" id="searchBtn">
						<i class="fa-solid fa-magnifying-glass"></i>
					</button>
				</div>
				<form id="logoutForm" method="post" action="/category/do-logout" style="display: none;">
				</form>
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