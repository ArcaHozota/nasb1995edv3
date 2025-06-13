// === DOM Ready Handler ===
document.addEventListener("DOMContentLoaded", () => {
	const toTemporary = document.getElementById("toTemporary");
	if (toTemporary) {
		toTemporary.classList.remove('text-white');
		toTemporary.classList.add('active');
	}
});

const bookInput = document.getElementById("bookInput");
const chapterInput = document.getElementById("chapterInput");
const infoStorageBtn = document.getElementById("infoStorageBtn");
const inputForm = document.getElementById("inputForm");

bookInput?.addEventListener("change", (e) => {
	chapterInput.innerHTML = '';
	const bookId = e.target.value;
	fetch('/books/get-chapters?bookId=' + encodeURIComponent(bookId))
		.then(res => res.json())
		.then(response => {
			response.forEach(item => {
				const option = document.createElement("option");
				option.value = item.id;
				option.textContent = item.name;
				chapterInput.appendChild(option);
			});
		});
});

infoStorageBtn?.addEventListener("click", () => {
	const inputSelectors = ["#phraseIdInput", "#phraseTextEnInput", "#phraseTextJpInput"];

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
	} else if (inputForm.querySelector(".is-invalid")) {
		layer.msg(inputWarning);
	} else {
		const postData = JSON.stringify({
			chapterId: chapterInput.value,
			id: document.getElementById("phraseIdInput").value.trim(),
			textEn: document.getElementById("phraseTextEnInput").value.trim(),
			textJp: document.getElementById("phraseTextJpInput").value.trim()
		});

		projectAjaxModify('/books/info-storage', POST, postData, booksPostSuccessFunction);
	}
});

function booksPostSuccessFunction(response) {
	formReset("inputForm");
	formReset("inputForm2");
	layer.msg(response);
}