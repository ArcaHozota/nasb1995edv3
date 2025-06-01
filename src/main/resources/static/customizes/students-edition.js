// === DOM Element References ===
const toStudentsPages = document.getElementById("toStudentsPages");
const accountEdit = document.getElementById("accountEdit");
const infoUpdateBtn = document.getElementById("infoUpdateBtn");
const restoreBtn = document.getElementById("restoreBtn");
const editForm = document.getElementById("editForm");
const idContainer = document.getElementById("idContainer");
const nameEdit = document.getElementById("nameEdit");
const passwordEdit = document.getElementById("passwordEdit");
const birthdayEdit = document.getElementById("birthdayEdit");
const emailEdit = document.getElementById("emailEdit");


document.addEventListener("DOMContentLoaded", () => {
	initialStudent();
});

toStudentsPages.addEventListener("click", (e) => {
	e.preventDefault();
	layer.msg(delayApology);
});

accountEdit.addEventListener("change", (e) => {
	checkStudentName(e.currentTarget, idContainer.value);
});

infoUpdateBtn.addEventListener("click", () => {
	const inputArrays = [accountEdit, nameEdit, passwordEdit, birthdayEdit, emailEdit];

	inputArrays.forEach(el => {
		el.classList.remove("is-valid", "is-invalid");
		const feedback = el.nextElementSibling;
		if (feedback?.tagName === "SPAN") {
			feedback.classList.remove("valid-feedback", "invalid-feedback");
			feedback.textContent = emptyString;
		}
	});

	const listArray = projectInputContextGet(inputArrays.map(el => `#${el.id}`));

	if (listArray.includes(emptyString)) {
		projectNullInputBoxDiscern(inputArrays.map(el => `#${el.id}`));
	} else if (editForm.querySelector(".is-invalid")) {
		layer.msg(inputWarning);
	} else {
		const putData = JSON.stringify({
			id: idContainer.value,
			loginAccount: accountEdit.value.trim(),
			username: nameEdit.value.trim(),
			password: passwordEdit.value,
			email: emailEdit.value,
			dateOfBirth: birthdayEdit.value
		});

		projectAjaxModify('/students/info-update', PUT, putData, studentsPutSuccessFunction);
	}
});

restoreBtn.addEventListener("click", () => {
	formReset("#editForm");
	initialStudent();
});

function studentsPutSuccessFunction(response) {
	const message = trimQuote(response);
	localStorage.setItem('redirectMessage', message);
	window.location.replace('/category/to-mainmenu');
}

function checkStudentName(studentNameInput, idVal) {
	const nameVal = studentNameInput.value.trim();
	if (nameVal === emptyString) {
		showValidationMsg(studentNameInput, responseFailure, "名称を空になってはいけません。");
	} else {
		fetch(`/students/check-duplicated?id=${encodeURIComponent(idVal)}&loginAccount=${encodeURIComponent(nameVal)}`)
			.then(async res => {
				const text = await res.text();
				if (res.ok) {
					showValidationMsg(studentNameInput, responseSuccess, text);
				} else {
					showValidationMsg(studentNameInput, responseFailure, text);
				}
			})
			.catch(() => {
				showValidationMsg(studentNameInput, responseFailure, "通信エラー");
			});
	}
}

function initialStudent() {
	const editId = idContainer.value;
	fetch(`/students/initial?editId=${encodeURIComponent(editId)}`)
		.then(res => res.json())
		.then(response => {
			accountEdit.value = response.loginAccount;
			nameEdit.value = response.username;
			passwordEdit.value = response.password;
			birthdayEdit.value = toDateInputValue(response.dateOfBirth);
			emailEdit.value = response.email;
		})
		.catch(async (xhr) => {
			const message = trimQuote(await xhr.text());
			layer.msg(message);
		});
}

function toDateInputValue(src) {
	if (!src) return emptyString;
	if (/^\d{4}-\d{2}-\d{2}$/.test(src)) return src;
	const d = new Date(src);
	const yyyy = d.getFullYear();
	const mm = String(d.getMonth() + 1).padStart(2, '0');
	const dd = String(d.getDate()).padStart(2, '0');
	return `${yyyy}-${mm}-${dd}`;
}