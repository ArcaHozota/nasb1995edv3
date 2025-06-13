// === DOM Ready Handler ===
document.addEventListener("DOMContentLoaded", () => {

	const logoutBtn = document.getElementById("logoutBtn");
	const logoutBtn2 = document.getElementById("logoutBtn2");
	const toMainmenu = document.getElementById("toMainmenu");
	const toMainmenu2 = document.getElementById("toMainmenu2");
	const toPersonal = document.getElementById("toPersonal");
	const toMessage = document.getElementById("toMessage");
	const toBookSearch = document.getElementById("toBookSearch");
	const toTemporary = document.getElementById("toTemporary");
	const toCollection = document.getElementById("toCollection");
	const toRandomFive = document.getElementById("toRandomFive");
	const logoutForm = document.getElementById("logoutForm");

	logoutBtn?.addEventListener("click", () => {
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
				logoutForm.submit();
			}
		});
	});

	logoutBtn2?.addEventListener("click", (e) => {
		e.preventDefault();
		logoutForm.submit();
	});

	[toMainmenu, toMainmenu2].forEach(el => el?.addEventListener("click", (e) => {
		e.preventDefault();
		window.location.replace('/category/to-mainmenu');
	}));

	toPersonal?.addEventListener("click", (e) => {
		e.preventDefault();
		const userId = toPersonal.querySelector("input")?.value;
		if (userId) window.location.replace('/students/to-edition?userId=' + userId);
	});

	toMessage?.addEventListener("click", (e) => {
		e.preventDefault();
		layer.msg(delayApology);
	});

	toBookSearch?.addEventListener("click", (e) => {
		e.preventDefault();
		//checkLink(e);
		layer.msg(delayApology);
	});

	toTemporary?.addEventListener("click", (e) => {
		e.preventDefault();
		//checkLink(e);
		checkPermissionAndTransfer('/books/to-addition');
	});

	toCollection?.addEventListener("click", (e) => {
		e.preventDefault();
		//checkLink(e);
		window.location.replace('/hymns/to-pages?pageNum=1');
	});

	toRandomFive?.addEventListener("click", (e) => {
		e.preventDefault();
		//checkLink(e);
		window.location.replace('/hymns/to-random-five');
	});

});

function checkPermissionAndTransfer(stringUrl) {
	fetch(stringUrl, { method: 'GET' })
		.then(res => {
			if (res.ok) {
				window.location.replace(stringUrl);
			} else {
				return res.text().then(msg => layer.msg(msg));
			}
		})
		.catch(() => layer.msg("通信エラー"));
}

function formReset(selector) {
	const form = document.getElementById(selector);
	if (!form) return;
	form.reset();
	form.querySelectorAll(".form-control, .form-select").forEach(el => el.classList.remove('is-valid', 'is-invalid'));
	form.querySelectorAll(".form-text").forEach(el => {
		el.classList.remove('valid-feedback', 'invalid-feedback');
		el.textContent = emptyString;
	});
}

function showValidationMsg(element, status, msg) {
	const el = typeof element === 'string' ? document.querySelector(element) : element;
	const span = el.nextElementSibling;
	el.classList.remove('is-valid', 'is-invalid');
	span?.classList.remove('valid-feedback', 'invalid-feedback');
	span && (span.textContent = emptyString);
	if (status === responseSuccess) {
		el.classList.add('is-valid');
		span?.classList.add('valid-feedback');
	} else {
		el.classList.add('is-invalid');
		span?.classList.add('invalid-feedback');
		if (span) span.textContent = msg;
	}
}

function projectAjaxModify(url, type, data, successFunction) {
	const header = document.querySelector("meta[name=_csrf_header]")?.content;
	const token = document.querySelector("meta[name=_csrf_token]")?.content;
	fetch(url, {
		method: type,
		headers: {
			'Content-Type': 'application/json;charset=UTF-8',
			...(header && token ? { [header]: token } : {})
		},
		body: data
	})
		.then(res => res.json())
		.then(successFunction)
		.catch(async (xhr) => {
			const message = trimQuote(await xhr.text());
			layer.msg(message);
		});
}

function projectNullInputBoxDiscern(inputArrays) {
	inputArrays.forEach(selector => {
		const el = document.querySelector(selector);
		if (el.value.trim() === emptyString) {
			showValidationMsg(el, responseFailure, '上記の入力ボックスを空になってはいけません。');
		}
	});
}

function projectInputContextGet(inputArrays) {
	const listArray = [];
	inputArrays.forEach(selector => {
		const el = document.querySelector(selector);
		const inputContext = el.value.trim();
		if (!el.classList.contains('is-invalid')) {
			listArray.push(inputContext);
			showValidationMsg(el, responseSuccess, emptyString);
		}
	});
	return listArray;
}

function normalDeleteSuccessFunction(result) {
	layer.msg(result.message);
	if (result.status === responseSuccess) {
		toSelectedPg(pageNum, keyword);
	}
}

function normalDeleteBtnFunction(url, message, deleteId) {
	fetch(url + 'deletion-check')
		.then(res => {
			if (!res.ok) throw res;
			return res.text();
		})
		.then(() => {
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
		})
		.catch(async (xhr) => {
			const message = trimQuote(await xhr.text());
			layer.msg(message);
		});
}

function usernameInitial() {
	fetch('/category/get-username')
		.then(res => res.text())
		.then(response => {
			document.getElementById("userNameContainer").textContent = response;
		})
		.catch(async (xhr) => {
			const message = trimQuote(await xhr.text());
			layer.msg(message);
		});
}

function checkLink(element) {
	const allLinkedNavs = document.querySelectorAll('.nav-link');
	allLinkedNavs.forEach(nl => {
		if (nl !== element.currentTarget) {
			nl.classList.remove('active');
			nl.classList.add('text-white');
		} else {
			nl.classList.remove('text-white');
			nl.classList.add('active');
		}
	})
}