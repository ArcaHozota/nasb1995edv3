let pageNum = document.getElementById("pageNumContainer")?.value;

document.addEventListener("DOMContentLoaded", () => {
	const toCollection = document.getElementById("toCollection");
	if (toCollection) {
		toCollection.classList.remove('text-white');
		toCollection.classList.add('active');
	}
});

document.getElementById("toHymnPages")?.addEventListener("click", (e) => {
	e.preventDefault();
	const url = '/hymns/to-pages?pageNum=' + pageNum;
	checkPermissionAndTransfer(url);
});

document.getElementById("nameJpInput")?.addEventListener("change", (e) => {
	checkHymnName(e.currentTarget, null);
});

document.getElementById("nameKrInput")?.addEventListener("change", (e) => {
	checkHymnName2(e.currentTarget, null);
});

document.getElementById("infoStorageBtn")?.addEventListener("click", () => {
	const inputArrays = ["#nameJpInput", "#nameKrInput", "#linkInput", "#serifInput"];

	inputArrays.forEach(sel => {
		const el = document.querySelector(sel);
		el.classList.remove("is-valid", "is-invalid");
		const span = el.nextElementSibling;
		if (span?.tagName === "SPAN") {
			span.classList.remove("valid-feedback", "invalid-feedback");
			span.textContent = emptyString;
		}
	});

	const listArray = projectInputContextGet(inputArrays);

	if (listArray.includes(emptyString)) {
		projectNullInputBoxDiscern(inputArrays);
	} else if (document.getElementById("inputForm").querySelector(".is-invalid")) {
		layer.msg(inputWarning);
	} else {
		const postData = JSON.stringify({
			nameJp: document.getElementById("nameJpInput").value.trim(),
			nameKr: document.getElementById("nameKrInput").value.trim(),
			link: document.getElementById("linkInput").value,
			serif: document.getElementById("serifInput").value
		});

		projectAjaxModify('/hymns/info-storage', POST, postData, hymnsPostSuccessFunction);
	}
});

document.getElementById("nameJpEdit")?.addEventListener("change", (e) => {
	checkHymnName(e.currentTarget, document.getElementById("idContainer").value);
});

document.getElementById("nameKrEdit")?.addEventListener("change", (e) => {
	checkHymnName2(e.currentTarget, document.getElementById("idContainer").value);
});

document.getElementById("infoUpdateBtn")?.addEventListener("click", () => {
	const inputArrays = ["#nameJpEdit", "#nameKrEdit", "#linkEdit", "#serifEdit"];

	inputArrays.forEach(sel => {
		const el = document.querySelector(sel);
		el.classList.remove("is-valid", "is-invalid");
		const span = el.nextElementSibling;
		if (span?.tagName === "SPAN") {
			span.classList.remove("valid-feedback", "invalid-feedback");
			span.textContent = emptyString;
		}
	});

	const listArray = projectInputContextGet(inputArrays);

	if (listArray.includes(emptyString)) {
		projectNullInputBoxDiscern(inputArrays);
	} else if (document.getElementById("editForm").querySelector(".is-invalid")) {
		layer.msg(inputWarning);
	} else {
		const putData = JSON.stringify({
			id: document.getElementById("idContainer").value,
			nameJp: document.getElementById("nameJpEdit").value.trim(),
			nameKr: document.getElementById("nameKrEdit").value.trim(),
			link: document.getElementById("linkEdit").value,
			serif: document.getElementById("serifEdit").value,
			updatedTime: document.getElementById("datestampContainer").value
		});

		projectAjaxModify('/hymns/info-update', PUT, putData, hymnsPutSuccessFunction);
	}
});

document.getElementById("resetBtn")?.addEventListener("click", () => {
	formReset("#inputForm");
});

document.getElementById("restoreBtn")?.addEventListener("click", () => {
	formReset("#editForm");
});

function hymnsPostSuccessFunction(response) {
	localStorage.setItem('redirectMessage', inputString);
	window.location.replace('/hymns/to-pages?pageNum=' + response);
}

function hymnsPutSuccessFunction(response) {
	localStorage.setItem('redirectMessage', trimQuote(response));
	window.location.replace('/hymns/to-pages?pageNum=' + pageNum);
}

function checkHymnName(hymnNameInput, idVal) {
	const nameVal = hymnNameInput.value.trim();
	if (nameVal === emptyString) {
		showValidationMsg(hymnNameInput, responseFailure, showVadMsgError);
	} else {
		fetch(`/hymns/check-duplicated?id=${encodeURIComponent(idVal)}&nameJp=${encodeURIComponent(nameVal)}`)
			.then(async res => {
				const text = await res.text();
				if (res.ok) {
					showValidationMsg(hymnNameInput, responseSuccess, text);
				} else {
					showValidationMsg(hymnNameInput, responseFailure, text);
				}
			});
	}
}

function checkHymnName2(hymnNameInput, idVal) {
	const nameVal = hymnNameInput.value.trim();
	if (nameVal === emptyString) {
		showValidationMsg(hymnNameInput, responseFailure, showVadMsgError);
	} else {
		fetch(`/hymns/check-duplicated2?id=${encodeURIComponent(idVal)}&nameKr=${encodeURIComponent(nameVal)}`)
			.then(async res => {
				const text = await res.text();
				if (res.ok) {
					showValidationMsg(hymnNameInput, responseSuccess, text);
				} else {
					showValidationMsg(hymnNameInput, responseFailure, text);
				}
			});
	}
}