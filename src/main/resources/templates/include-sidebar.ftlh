<!DOCTYPE html>
<html lang="ja-JP">
<body>
	<#assign principalAdmin = Session.SPRING_SECURITY_CONTEXT.authentication.principal
	        originalAdmin = principalAdmin.originalAdmin>
	<div class="col-md-2 position-fixed top-0 start-0 text-bg-dark" style="height: 100vh; z-index: 1040;">
		<div class="d-flex flex-column flex-shrink-0 p-3 text-bg-dark" style="width: 100%;height: 100%;"> 
			<div class="d-flex justify-content-start">
				<img src="/static/image/cross.svg" alt="img01" width="45" height="54">
				<a class="navbar-brand effect-shine" style="font-size: 33px;" href="#" id="toMainmenu">
					NASB1995
				</a>
			</div>
		    <hr>
		    <ul class="nav nav-pills flex-column mb-auto">
		        <li class="nav-item"> 
		        	<a href="#" class="nav-link text-white" id="toBookSearch"> 
		        		<svg class="bi pe-none me-2" width="16" height="16" aria-hidden="true">
		        			<use xlink:href="#"></use>
		                </svg>
		                <i class="fa-solid fa-anchor"></i> 聖書章節選択
		            </a>
		        </li>
		        <li> 
		        	<a href="#" class="nav-link text-white" id="toTemporary"> 
		        		<svg class="bi pe-none me-2" width="16" height="16" aria-hidden="true">
		        			<use xlink:href="#"></use>
		                </svg>
		                <i class="fa-solid fa-box-archive"></i> 聖書章節入力
		            </a> 
		        </li>
		        <li> 
		        	<a href="#" class="nav-link text-white" id="toCollection"> 
		        		<svg class="bi pe-none me-2" width="16" height="16" aria-hidden="true">
		        			<use xlink:href="#"></use>
		                </svg>
		                <i class="fa-solid fa-rss"></i> 賛美歌集め
		            </a> 
		        </li>
		        <li> 
		        	<a href="#" class="nav-link text-white" id="toRandomFive"> 
		        		<svg class="bi pe-none me-2" width="16" height="16" aria-hidden="true">
		        			<use xlink:href="#"></use>
		                </svg>
		                <i class="fa-solid fa-copyright"></i> ランダム五つ
		            </a> 
		        </li>
		    </ul>
		    <hr>
		    <div class="dropdown"> <a href="#" class="d-flex align-items-center text-white text-decoration-none dropdown-toggle"
		            data-bs-toggle="dropdown" aria-expanded="false"> <img src="https://github.com/mdo.png" alt="" width="32"
		                height="32" class="rounded-circle me-2"> <strong>${originalAdmin.username()}</strong> </a>
		        <ul class="dropdown-menu dropdown-menu-dark text-small shadow">
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
		            <li>
		                <hr class="dropdown-divider">
		            </li>
		            <li>
		            	<a class="dropdown-item" href="#" id="logoutBtn2">
		            		<i class="fa-solid fa-right-from-bracket"></i> ログアウト
		            	</a>
		            </li>
		        </ul>
		    </div>
		</div>
	</div>
</body>
</html>