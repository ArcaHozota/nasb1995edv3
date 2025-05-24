$(document).ready(() => {
	let treeData = [
		{
			text: "聖書奉読",
			icon: "fa-solid fa-book-bible",
			expanded: true,
			nodes: [
				{
					id: "toBookSearch",
					text: "章節選択",
					icon: "fa-solid fa-anchor"
				},
				{
					id: "toTemporary",
					text: "章節入力",
					icon: "fa-solid fa-box-archive"
				}
			]
		},
		{
			text: "賛美歌集め",
			icon: "fa-solid fa-music",
			expanded: true,
			nodes: [
				{
					id: "toCollection",
					text: "コレクション一覧",
					icon: "fa-solid fa-rss"
				},
				{
					id: "toRandomFive",
					text: "ランダム五つ",
					icon: "fa-regular fa-copyright"
				}
			]
		}
	];
	$("#mainmenuTree").bstreeview({
		data: treeData,
		expandIcon: 'fa fa-angle-down fa-fw',
		collapseIcon: 'fa fa-angle-right fa-fw',
		indent: 1.5,
		parentsMarginLeft: '1.25rem',
		openNodeLinkOnNewTab: true
	});
	$("#logoutBtn").on("click", () => {
		Swal.fire({
			title: '警告',
			text: 'ログアウトしてよろしいでしょうか。',
			icon: 'warning',
			showDenyButton: true,
			denyButtonText: 'いいえ',
			confirmButtonText: 'はい',
			confirmButtonColor: '#7f0020',
			denyButtonColor: '#002fa7'
		}).then((result) => {
			if (result.isConfirmed) {
				$("#logoutForm").submit();
			}
		});
	});
	// usernameInitial();
	$("#toMainmenu").on("click", (e) => {
		e.preventDefault();
		window.location.replace('/category/to-mainmenu');
	});
	$("#toMainmenu2").on("click", (e) => {
		e.preventDefault();
		window.location.replace('/category/to-mainmenu');
	});
	$("#toPersonal").on("click", (e) => {
		e.preventDefault();
		let userId = $(e.currentTarget).find("input").val();
		window.location.replace('/students/to-edition?userId=' + userId);
	});
	$("#toMessage").on("click", (e) => {
		e.preventDefault();
		layer.msg(delayApology);
	});
	$("#toBookSearch").on("click", (e) => {
		e.preventDefault();
		layer.msg(delayApology);
		// let url = '/books/to-pages?pageNum=1';
		// checkPermissionAndTransfer(url);
	});
	$("#toTemporary").on("click", (e) => {
		e.preventDefault();
		let url = '/books/to-addition';
		checkPermissionAndTransfer(url);
	});
	$("#toCollection").on("click", (e) => {
		e.preventDefault();
		window.location.replace('/hymns/to-pages?pageNum=1');
	});
	$("#toRandomFive").on("click", (e) => {
		e.preventDefault();
		window.location.replace('/hymns/to-random-five');
	});
});

function checkPermissionAndTransfer(stringUrl) {
	let ajaxResponse = $.ajax({
		url: stringUrl,
		type: GET,
		async: false
	});
	if (ajaxResponse.status === 200) {
		window.location.replace(stringUrl);
	} else {
		layer.msg(ajaxResponse.message);
	}
}

function formReset(element) {
	$(element)[0].reset();
	$(element).find(".form-control").removeClass('is-valid is-invalid');
	$(element).find(".form-select").removeClass('is-valid is-invalid');
	$(element).find(".form-text").removeClass('valid-feedback invalid-feedback');
	$(element).find(".form-text").text(emptyString);
}

function showValidationMsg(element, status, msg) {
	$(element).removeClass('is-valid is-invalid');
	$(element).next("span").removeClass('valid-feedback invalid-feedback');
	$(element).next("span").text(emptyString);
	if (status === responseSuccess) {
		$(element).addClass('is-valid');
		$(element).next("span").addClass('valid-feedback');
	} else {
		$(element).addClass('is-invalid');
		$(element).next("span").addClass('invalid-feedback').text(msg);
	}
}

function projectAjaxModify(url, type, data, successFunction) {
	let header = $("meta[name=_csrf_header]").attr("content");
	let token = $("meta[name=_csrf_token]").attr("content");
	$.ajax({
		url: url,
		type: type,
		data: data,
		headers: {
			[header]: token
		},
		dataType: 'json',
		contentType: 'application/json;charset=UTF-8',
		success: successFunction,
		error: (xhr) => {
			let message = trimQuote(xhr.responseText);
			layer.msg(message);
		}
	});
}

function projectNullInputBoxDiscern(inputArrays) {
	for (const element of inputArrays) {
		if ($(element).val().trim() === emptyString) {
			showValidationMsg(element, responseFailure, '上記の入力ボックスを空になってはいけません。');
		}
	}
}

function projectInputContextGet(inputArrays) {
	let listArray = [];
	for (const element of inputArrays) {
		let inputContext = $(element).val().trim();
		if (!$(element).hasClass('is-invalid')) {
			listArray.push(inputContext);
			showValidationMsg(element, responseSuccess, emptyString);
		}
	}
	return listArray;
}

function normalDeleteSuccessFunction(result) {
	if (result.status === responseSuccess) {
		layer.msg(result.message);
		toSelectedPg(pageNum, keyword);
	} else {
		layer.msg(result.message);
	}
}

function normalDeleteBtnFunction(url, message, deleteId) {
	$.ajax({
		url: url + 'deletion-check',
		type: GET,
		success: () => {
			Swal.fire({
				title: 'メッセージ',
				text: message,
				icon: 'question',
				showCloseButton: true,
				confirmButtonText: 'はい',
				confirmButtonColor: '#7f0020'
			}).then((result) => {
				if (result.isConfirmed) {
					projectAjaxModify(url + 'info-delete?id=' + deleteId, 'DELETE', null, normalDeleteSuccessFunction);
				}
			});
		},
		error: (xhr) => {
			let message = trimQuote(xhr.responseText);
			layer.msg(message);
		}
	});
}

function usernameInitial() {
	$.ajax({
		url: '/category/get-username',
		success: (response) => {
			$("#userNameContainer").text(response);
		},
		error: (xhr) => {
			let message = trimQuote(xhr.responseText);
			layer.msg(message);
		}
	});
}