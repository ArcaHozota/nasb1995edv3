// === DOM Element References ===
const tableBody = document.getElementById("tableBody");
const kanumiSearchBtn = document.getElementById("kanamiSearchBtn");
const nameDisplay = document.getElementById("nameDisplay");
const loadingBackground2 = document.getElementById("loadingBackground2");
let keyword = null;

document.addEventListener("DOMContentLoaded", () => {
    adjustWidth();
    toSelectedPg(1, keyword);

    kanumiSearchBtn.addEventListener("mousemove", (e) => {
        const offset = kanumiSearchBtn.getBoundingClientRect();
        const x = e.clientX - offset.left;
        const y = e.clientY - offset.top;
        kanumiSearchBtn.style.setProperty('--x', `${x}px`);
        kanumiSearchBtn.style.setProperty('--y', `${y}px`);
    });
});

kanumiSearchBtn.addEventListener("click", (e) => {
    e.preventDefault();
    const hymnId = nameDisplay.getAttribute('data-id-val');
    if (hymnId === "0" || hymnId === 0 || hymnId === null || hymnId === undefined) {
        layer.msg('賛美歌を選択してください');
    } else {
        Swal.fire({
            title: "HINT",
            text: "選択された曲に基づく歌詞が似てる三つの曲を検索します。検索が約3分間ぐらいかかりますので行ってよろしいでしょうか。",
            footer: '<p style="font-size: 13px;">※この画面及び検索は金海嶺氏のアイディアによって作成されたものです。</p>',
            icon: "info",
            showDenyButton: true,
            denyButtonText: 'いいえ',
            confirmButtonText: 'はい',
            confirmButtonColor: '#7f0020',
            denyButtonColor: '#002fa7'
        }).then((result) => {
            if (result.isConfirmed) {
                adjustWidth();
                loadingBackground2.style.display = "block";
                kanumiSearchBtn.style.pointerEvents = "none";
                kanumiRetrieve(hymnId);

                setTimeout(() => {
                    loadingBackground2.style.display = "none";
                    kanumiSearchBtn.style.pointerEvents = "auto";
                    const activeRow = tableBody.querySelector(".table-danger");
                    const nameJp = activeRow?.querySelector("td:nth-child(2) a")?.textContent || "";
                    const slashIndex = nameJp.indexOf('/');
                    nameDisplay.textContent = '検索完了---' + nameJp.substring(0, slashIndex);
                    nameDisplay.setAttribute('data-id-val', 0);
                }, 132000);
            }
        });
    }
});

tableBody.addEventListener("change", (e) => {
    if (e.target.classList.contains("form-check-input")) {
        const allCheckboxes = tableBody.querySelectorAll(".form-check-input");
        allCheckboxes.forEach(cb => {
            if (cb !== e.target) cb.checked = false;
        });
    }
});

tableBody.addEventListener("click", (e) => {
    if (e.target.classList.contains("form-check-input")) {
        const checked = e.target.checked;
        if (checked) {
            const idVal = e.target.value;
            fetch('/hymns/get-info-id?hymnId=' + encodeURIComponent(idVal))
                .then(res => res.json())
                .then(response => {
                    nameDisplay.textContent = response.nameJp;
                    nameDisplay.setAttribute('data-id-val', response.id);
                })
                .catch(async (xhr) => {
                    const message = trimQuote(await xhr.text());
                    layer.msg(message);
                });
        } else {
            nameDisplay.setAttribute('data-id-val', 0);
            fetch('/hymns/get-records')
                .then(res => res.json())
                .then(response => {
                    nameDisplay.textContent = '賛美歌' + response + '曲レコード済み';
                })
                .catch(async (xhr) => {
                    const message = trimQuote(await xhr.text());
                    layer.msg(message);
                });
        }
    }

    if (e.target.classList.contains("link-btn")) {
        e.preventDefault();
        const transferVal = e.target.getAttribute('data-transfer-val');
        window.open(transferVal);
    }
});

function toSelectedPg(pageNum, _) {
    fetch(`/hymns/pagination?pageNum=${pageNum}`)
        .then(res => res.json())
        .then(response => {
            buildTableBody1(response);
            buildPageInfos(response);
            buildPageNavi(response);
        })
        .catch(async (xhr) => {
            const message = trimQuote(await xhr.text());
            layer.msg(message);
        });
}

function buildTableBody1(response) {
    tableBody.innerHTML = '';
    response.records.forEach(item => {
        const tr = document.createElement("tr");

        const checkboxTd = document.createElement("td");
        checkboxTd.className = "text-center";
        checkboxTd.style.cssText = "width: 10%; vertical-align: middle;";
        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.className = "form-check-input mt-0";
        checkbox.value = item.id;
        checkbox.style.verticalAlign = "middle";
        checkboxTd.appendChild(checkbox);

        const nameTd = document.createElement("td");
        nameTd.className = "text-left";
        nameTd.style.cssText = "width: 70%; vertical-align: middle;";
        const link = document.createElement("a");
        link.href = "#";
        link.className = "link-btn";
        link.setAttribute("data-transfer-val", item.link);
        link.textContent = item.nameJp + delimiter + item.nameKr;
        nameTd.appendChild(link);

        const scoreTd = document.createElement("td");
        scoreTd.className = "text-center";
        scoreTd.style.cssText = "width: 20%; vertical-align: middle;";
        const scoreLink = document.createElement("a");
        scoreLink.href = "#";
        scoreLink.className = "score-download-btn";
        scoreLink.setAttribute("data-score-id", item.id);
        scoreLink.innerHTML = "&#x1D11E;";
        scoreTd.appendChild(scoreLink);

        tr.appendChild(checkboxTd);
        tr.appendChild(nameTd);
        tr.appendChild(scoreTd);

        tableBody.appendChild(tr);
    });
}

function kanumiRetrieve(hymnId) {
    fetch(`/hymns/kanumi-retrieve?hymnId=${encodeURIComponent(hymnId)}`)
        .then(res => res.json())
        .then(buildTableBody2)
        .catch(err => {
            layer.msg(err.responseJSON?.message || "通信エラー");
        });
}

function buildTableBody2(response) {
    tableBody.innerHTML = emptyString;
    response.forEach(item => {
        const tr = document.createElement("tr");

        const checkboxTd = document.createElement("td");
        checkboxTd.className = "text-center";
        checkboxTd.style.cssText = "width: 10%; vertical-align: middle;";
        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.className = "form-check-input mt-0";
        checkbox.value = item.id;
        checkbox.style.verticalAlign = "middle";
        checkboxTd.appendChild(checkbox);

        const nameTd = document.createElement("td");
        nameTd.className = "text-left";
        nameTd.style.cssText = "width: 70%; vertical-align: middle;";
        const link = document.createElement("a");
        link.href = "#";
        link.className = "link-btn";
        link.setAttribute("data-transfer-val", item.link);
        link.textContent = item.nameJp + delimiter + item.nameKr;
        nameTd.appendChild(link);

        const scoreTd = document.createElement("td");
        scoreTd.className = "text-center";
        scoreTd.style.cssText = "width: 20%; vertical-align: middle;";
        const scoreLink = document.createElement("a");
        scoreLink.href = "#";
        scoreLink.className = "score-download-btn";
        scoreLink.setAttribute("data-score-id", item.id);
        scoreLink.innerHTML = "&#x1D11E;";
        scoreTd.appendChild(scoreLink);

        tr.appendChild(checkboxTd);
        tr.appendChild(nameTd);
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
        const width = indexTable.offsetWidth + "px";
        document.querySelectorAll(".background2").forEach(bg => {
            bg.style.width = width;
        });
    }
} 
