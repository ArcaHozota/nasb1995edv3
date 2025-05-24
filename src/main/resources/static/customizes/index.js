const tableBody = document.getElementById("tableBody");
const randomSearchBtn = document.getElementById("randomSearchBtn");
const loadingContainer = document.getElementById("loadingContainer");
const loadingBackground = document.getElementById("loadingBackground");
const toIchiranHyoBtn = document.getElementById("toIchiranHyoBtn");

document.addEventListener("DOMContentLoaded", () => {
	adjustWidth();
	tableBody.style.display = "none";

	const message2 = document.getElementById("torokuMsgContainer")?.value;
	if (message2 !== emptyString && message2 !== null && message2 !== undefined) {
		layer.msg(message2);
	}
});

randomSearchBtn.addEventListener("click", () => {
	adjustWidth();
	loadingBackground.style.display = "block";
	loadingContainer.style.display = "block";
	tableBody.style.display = "table-row-group";
	randomSearchBtn.disabled = true;

	const keyword = document.getElementById("keywordInput")?.value;
	commonRetrieve(keyword);

	setTimeout(() => {
		loadingContainer.style.display = "none";
		loadingBackground.style.display = "none";
		randomSearchBtn.disabled = false;
	}, 3300);
});

tableBody.addEventListener("click", (e) => {
	if (e.target.classList.contains("link-btn")) {
		e.preventDefault();
		const transferVal = e.target.getAttribute("data-transfer-val");
		if (transferVal) window.open(transferVal);
	}
});

toIchiranHyoBtn.addEventListener("click", () => {
	Swal.fire({
		title: 'メッセージ',
		text: '賛美歌一覧表画面へ移動してよろしいでしょうか。',
		icon: 'question',
		showCloseButton: true,
		confirmButtonText: 'はい',
		confirmButtonColor: '#7F0020'
	}).then((result) => {
		if (result.isConfirmed) {
			window.location.href = '/home/to-list';
		}
	});
});

function commonRetrieve(keyword) {
	fetch('/hymns/common-retrieve?keyword=' + encodeURIComponent(keyword))
		.then(response => {
			if (!response.ok) {
				return response.json().then(err => { throw err; });
			}
			return response.json();
		})
		.then(buildTableBody)
		.catch(result => {
			layer.msg(result.message);
		});
}

function buildTableBody(response) {
	tableBody.innerHTML = emptyString;

	response.forEach(item => {
		const tr = document.createElement("tr");
		const td = document.createElement("td");
		td.className = "text-center";
		td.style.verticalAlign = "middle";

		const a = document.createElement("a");
		a.href = "#";
		a.className = "link-btn";
		a.setAttribute("data-transfer-val", item.link);
		a.textContent = `${item.nameJp}/${item.nameKr}`;

		td.appendChild(a);
		tr.appendChild(td);

		switch (item.lineNumber) {
			case 'BURGUNDY':
				tr.className = "table-danger";
				break;
			case 'NAPLES':
				tr.className = "table-warning";
				break;
			case 'CADMIUM':
				tr.className = "table-success";
				break;
			default:
				tr.className = "table-light";
		}

		tableBody.appendChild(tr);
	});
}

function adjustWidth() {
	const indexTable = document.getElementById("indexTable");
	if (indexTable) {
		const bgElements = document.querySelectorAll(".background");
		const width = indexTable.offsetWidth + "px";
		bgElements.forEach(el => {
			el.style.width = width;
		});
	}
}