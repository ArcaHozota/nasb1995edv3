const tableBody = document.getElementById("tableBody");
let pageNum = document.getElementById("pageNumContainer")?.value;
let totalRecords, totalPages, keyword;

document.addEventListener("DOMContentLoaded", () => {
	const toCollection = document.getElementById("toCollection");
	if (toCollection) {
		toCollection.classList.remove('text-white');
		toCollection.classList.add('active');
	}
	if (keyword === undefined) {
		keyword = emptyString;
	}
	const message = localStorage.getItem('redirectMessage');
	if (message) {
		layer.msg(message);
		localStorage.removeItem('redirectMessage');
	}
	toSelectedPg(pageNum, keyword);
});

document.getElementById("searchBtn2")?.addEventListener("click", () => {
	keyword = document.getElementById("keywordInput")?.value;
	toSelectedPg(1, keyword);
});

tableBody?.addEventListener("click", (e) => {
	const target = e.target.closest(".delete-btn, .edit-btn, .score-btn, .link-btn, .score-download-btn");
	if (!target) return;

	if (target.classList.contains("delete-btn")) {
		const deleteId = target.getAttribute("data-delete-id");
		const nameJp = target.closest("tr")?.querySelector("th")?.textContent.trim();
		normalDeleteBtnFunction('/hymns/', `この「${nameJp}」という歌の情報を削除するとよろしいでしょうか。`, deleteId);
	} else if (target.classList.contains("edit-btn")) {
		const editId = target.getAttribute("data-edit-id");
		const url = `/hymns/to-edition?editId=${editId}&pageNum=${pageNum}`;
		checkPermissionAndTransfer(url);
	} else if (target.classList.contains("score-btn")) {
		const scoreId = target.getAttribute("data-score-id");
		const url = `/hymns/to-score-upload?scoreId=${scoreId}&pageNum=${pageNum}`;
		checkPermissionAndTransfer(url);
	} else if (target.classList.contains("link-btn")) {
		e.preventDefault();
		const transferVal = target.getAttribute("data-transfer-val");
		window.open(transferVal);
	} else if (target.classList.contains("score-download-btn")) {
		e.preventDefault();
		const scoreId = target.getAttribute("data-score-id");
		window.location.href = `/hymns/score-download?scoreId=${scoreId}`;
	}
});

document.getElementById("infoAdditionBtn")?.addEventListener("click", (e) => {
	e.preventDefault();
	const url = `/hymns/to-addition?pageNum=${pageNum}`;
	checkPermissionAndTransfer(url);
});

function toSelectedPg(pageNum, keyword) {
	fetch(`/hymns/pagination?pageNum=${encodeURIComponent(pageNum)}&keyword=${encodeURIComponent(keyword)}`)
		.then(res => res.json())
		.then(response => {
			buildTableBody(response);
			buildPageInfos(response);
			buildPageNavi(response);
		})
		.catch(async (xhr) => {
			const message = trimQuote(await xhr.text());
			layer.msg(message);
		});
}

function buildTableBody(response) {
	tableBody.innerHTML = emptyString;
	const index = response.records;
	index.forEach(item => {
		const nameJpTd = document.createElement("th");
		nameJpTd.className = "text-left";
		nameJpTd.style = "width: 130px; vertical-align: middle;";
		nameJpTd.textContent = item.nameJp;

		const nameKrTd = document.createElement("td");
		nameKrTd.className = "text-left";
		nameKrTd.style = "width: 100px; vertical-align: middle;";
		nameKrTd.textContent = item.nameKr;

		const linkTd = document.createElement("td");
		linkTd.className = "text-center";
		linkTd.style = "width: 20px; vertical-align: middle;";
		const linkA = document.createElement("a");
		linkA.href = "#";
		linkA.className = "link-btn";
		linkA.setAttribute("data-transfer-val", item.link);
		linkA.textContent = "Link";
		linkTd.appendChild(linkA);

		const scoreTd = document.createElement("td");
		scoreTd.className = "text-center";
		scoreTd.style = "width: 20px; vertical-align: middle;";
		const scoreA = document.createElement("a");
		scoreA.href = "#";
		scoreA.className = "score-download-btn";
		scoreA.setAttribute("data-score-id", item.id);
		scoreA.innerHTML = "&#x1D11E;";
		scoreTd.appendChild(scoreA);

		const btnTd = document.createElement("td");
		btnTd.className = "text-center";
		btnTd.style = "width: 80px; vertical-align: middle;";

		const scoreBtn = document.createElement("button");
		scoreBtn.className = "btn btn-success btn-sm score-btn";
		scoreBtn.setAttribute("data-score-id", item.id);
		scoreBtn.innerHTML = '<i class="fa-solid fa-music"></i> 楽譜';

		const editBtn = document.createElement("button");
		editBtn.className = "btn btn-primary btn-sm edit-btn";
		editBtn.setAttribute("data-edit-id", item.id);
		editBtn.innerHTML = '<i class="fa-solid fa-pencil"></i> 編集';

		const deleteBtn = document.createElement("button");
		deleteBtn.className = "btn btn-danger btn-sm delete-btn";
		deleteBtn.setAttribute("data-delete-id", item.id);
		deleteBtn.innerHTML = '<i class="fa-solid fa-trash"></i> 削除';

		btnTd.appendChild(scoreBtn);
		btnTd.append(" ");
		btnTd.appendChild(editBtn);
		btnTd.append(" ");
		btnTd.appendChild(deleteBtn);

		const tr = document.createElement("tr");
		tr.appendChild(nameJpTd);
		tr.appendChild(nameKrTd);
		tr.appendChild(linkTd);
		tr.appendChild(scoreTd);
		tr.appendChild(btnTd);

		tableBody.appendChild(tr);
	});
}