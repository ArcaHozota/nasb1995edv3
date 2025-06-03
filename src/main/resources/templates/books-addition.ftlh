<!DOCTYPE html>
<html lang="ja-JP">
<#include "include-header.ftlh">

<body>
	<#include "include-navbar.ftlh">
	<#include "include-sidebar.ftlh">
	<div class="container-fluid">
		<div class="row">
			<div class="col-sm-9 offset-sm-3 col-md-10 offset-md-2 main">
				<div class="card border-tunagray mb-3" style="margin-top: 1rem;">
					<div class="card-header text-bg-tunagray mb-3">
						<h5 class="card-title" style="padding-top: 8px;">
							<i class="fa-solid fa-bars-staggered"></i> 聖書章節入力
						</h5>
					</div>
					<div class="card-body">
						<form class="row" id="inputForm" style="height: 5.7vh;">
							<div class="col-md-3">
					            <select id="bookInput" class="form-select">
					                <#list bookDtos as bookDto>
										<option value="${bookDto.id}">${bookDto.name}</option>
									</#list>
					            </select>
					        </div>
					        <div class="col-md-4">
					            <select id="chapterInput" class="form-select">
					                <#list chapterDtos as chapterDto>
										<option value="${chapterDto.id}">${chapterDto.name}</option>
									</#list>
					            </select>
					        </div>
					        <div class="col-md-3">
					            <input id="phraseIdInput" class="form-control" placeholder="節の数を入力しましょう">
					        </div>
					        <div class="col-md-2">
					            <button type="button" class="btn btn-danger my-2 my-sm-0" id="infoStorageBtn">
									<i class="fa-solid fa-book"></i> 追加
								</button>
					        </div>
						</form>
						<form id="inputForm2">
                            <div class="form-group row" style="margin-top: 2.2rem;">
                                <label for="phraseTextEnInput" class="col-md-1 col-form-label text-end">英語文書</label>
                                <div class="col-md-11" style="height: 12vh;">
                                    <textarea class="form-control" style="height: 100%"
                                        id="phraseTextEnInput"></textarea>
                                </div>
                            </div>
                            <div class="form-group row" style="margin-top: 0.4rem;">
                                <label for="phraseTextJpInput" class="col-md-1 col-form-label text-end">日本語文書</label>
                                <div class="col-md-11" style="height: 12vh;">
                                    <textarea class="form-control" style="height: 100%"
                                        id="phraseTextJpInput"></textarea>
                                </div>
                            </div>
                        </form>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript" src="/static/customizes/books-addition.js" nonce="${nonce}"></script>
</body>

</html>