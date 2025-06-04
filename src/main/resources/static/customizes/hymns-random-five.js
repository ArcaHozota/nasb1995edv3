const tableBody = document.getElementById("tableBody");

document.addEventListener("DOMContentLoaded", () => {
	const toRandomFive = document.getElementById("toRandomFive");
	if (toRandomFive) {
		toRandomFive.classList.remove('text-white');
		toRandomFive.classList.add('active');
	}
});

document.getElementById("randomSearchBtn")?.addEventListener("click", () => {
	keyword = document.getElementById("keywordInput")?.value;
	retrieveRandomFive(keyword);
});

tableBody?.addEventListener("click", (e) => {
	const target = e.target.closest(".link-btn");
	if (target) {
		e.preventDefault();
		const transferVal = target.getAttribute("data-transfer-val");
		window.open(transferVal);
	}
});

function retrieveRandomFive(keyword) {
	fetch(`/hymns/random-five-retrieve?keyword=${encodeURIComponent(keyword)}`)
		.then(res => res.json())
		.then(response => buildTableBody(response))
		.catch(err => {
			layer.msg(err.responseJSON?.message || "通信エラー");
		});
}

function buildTableBody(response) {
	tableBody.innerHTML = emptyString;
	response.forEach(item => {
		const td = document.createElement("td");
		td.className = "text-center";
		td.style.verticalAlign = "middle";

		const a = document.createElement("a");
		a.href = "#";
		a.className = "link-btn";
		a.setAttribute("data-transfer-val", item.link);
		a.textContent = `${item.nameJp}${delimiter}${item.nameKr}`;

		td.appendChild(a);

		const tr = document.createElement("tr");
		tr.appendChild(td);
		tableBody.appendChild(tr);
	});
}