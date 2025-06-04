const pageNum = document.getElementById("pageNumContainer")?.value;

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

document.getElementById("scoreUploadBtn")?.addEventListener("click", () => {
	const inputSelectors = ["#scoreEdit"];

	inputSelectors.forEach(sel => {
		const el = document.querySelector(sel);
		el.classList.remove("is-valid", "is-invalid");
		const span = el.nextElementSibling;
		if (span?.tagName === "SPAN") {
			span.classList.remove("valid-feedback", "invalid-feedback");
			span.textContent = emptyString;
		}
	});

	const listArray = projectInputContextGet(inputSelectors);

	if (listArray.includes(emptyString)) {
		projectNullInputBoxDiscern(inputSelectors);
		return;
	}

	const editId = document.getElementById("idContainer")?.value;
	const fileInput = document.getElementById("scoreEdit");
	const file = fileInput?.files[0];
	if (!file) return;

	const reader = new FileReader();
	reader.onload = (e) => {
		const base64File = e.target.result.split(",")[1];
		const jsonData = JSON.stringify({
			id: editId,
			score: base64File
		});

		fetch('/hymns/score-upload', {
			method: POST,
			headers: {
				'Content-Type': 'application/json'
			},
			body: jsonData
		})
			.then(res => res.text())
			.then(response => {
				const message = trimQuote(response);
				localStorage.setItem('redirectMessage', message);
				window.location.replace('/hymns/to-pages?pageNum=' + pageNum);
			})
			.catch(async (xhr) => {
				const message = trimQuote(await xhr.text());
				layer.msg(message);
			});
	};

	reader.readAsDataURL(file);
});