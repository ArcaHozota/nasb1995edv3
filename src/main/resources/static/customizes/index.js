const tableBody = document.getElementById("tableBody");
const randomSearchBtn = document.getElementById("randomSearchBtn");
const loadingContainer = document.getElementById("loadingContainer");
const loadingBackground = document.getElementById("loadingBackground");
const hymnSearchBtn = document.getElementById("hymnSearchBtn");
let keyword;

document.addEventListener("DOMContentLoaded", () => {
	adjustWidth();
	// keyword = document.getElementById("keywordInput")?.value;
	if (keyword === undefined) {
		keyword = emptyString;
	}
	toSelectedPg(1, keyword);
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
	randomRetrieve(keyword);
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

function randomRetrieve(keyword) {
	fetch('/hymns/random-retrieve?keyword=' + encodeURIComponent(keyword))
		.then(async response => {
			if (!response.ok) {
				const err = await response.json();
				throw err;
			}
			return response.json();
		})
		.then(buildTableBody2)
		.catch(result => {
			layer.msg(result.message);
		});
}

function toSelectedPg(pageNum, keyword) {
	fetch(`/hymns/pagination?pageNum=${encodeURIComponent(pageNum)}&keyword=${encodeURIComponent(keyword)}`)
		.then(res => res.json())
		.then(response => {
			buildTableBody2(response);
			buildPageInfos(response);
			buildPageNavi(response);
		})
		.catch(async (xhr) => {
			const message = trimQuote(await xhr.text());
			layer.msg(message);
		});
}

function buildTableBody2(response) {
	tableBody.innerHTML = emptyString;
	response.records.forEach(item => {
		const tr = document.createElement("tr");
		const td = document.createElement("td");
		td.className = "text-center";
		td.style.verticalAlign = "middle";
		const nameTd = document.createElement("td");
		nameTd.className = "text-left";
		nameTd.style.cssText = "width: 70%; vertical-align: middle;";
		const link = document.createElement("a");
		link.href = "#";
		link.className = "link-btn";
		link.setAttribute("data-transfer-val", item.link);
		link.textContent = item.nameJp + delimiter + item.nameKr;
		nameTd.appendChild(link);
		tr.appendChild(nameTd);
		const scoreTd = document.createElement("td");
		scoreTd.className = "text-center";
		scoreTd.style.cssText = "width: 30%; vertical-align: middle;";
		const scoreLink = document.createElement("a");
		scoreLink.href = "#";
		scoreLink.className = "score-download-btn";
		scoreLink.setAttribute("data-score-id", item.id);
		scoreLink.innerHTML = "&#x1D11E;";
		scoreTd.appendChild(scoreLink);
		tr.appendChild(scoreTd);
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