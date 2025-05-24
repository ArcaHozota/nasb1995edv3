let $tableBody = $("#tableBody");
let pageNum = $("#pageNumContainer").val();
let totalRecords, totalPages, keyword;
$(document).ready(() => {
    let $toCollection = $("#toCollection");
    $toCollection.css('color', '#006b3c');
    $toCollection.addClass('animate__animated animate__flipInY');
    if (keyword === undefined) {
        keyword = emptyString;
    }
    let message = localStorage.getItem('redirectMessage');
    if (message) {
        layer.msg(message);
        localStorage.removeItem('redirectMessage');
    }
    toSelectedPg(pageNum, keyword);
});
$("#searchBtn2").on("click", () => {
    keyword = $("#keywordInput").val();
    toSelectedPg(1, keyword);
});
$tableBody.on("click", '.delete-btn', (e) => {
    let deleteId = $(e.currentTarget).attr("data-delete-id");
    let nameJp = $(e.currentTarget).parents("tr").find("th").text().trim();
    normalDeleteBtnFunction('/hymns/', 'この「' + nameJp + '」という歌の情報を削除するとよろしいでしょうか。', deleteId);
});
$("#infoAdditionBtn").on("click", (e) => {
    e.preventDefault();
    let url = '/hymns/to-addition?pageNum=' + pageNum;
    checkPermissionAndTransfer(url);
});
$tableBody.on("click", '.edit-btn', (e) => {
    let editId = $(e.currentTarget).attr("data-edit-id");
    let url = '/hymns/to-edition?editId=' + editId + '&pageNum=' + pageNum;
    checkPermissionAndTransfer(url);
});
$tableBody.on("click", '.score-btn', (e) => {
    let scoreId = $(e.currentTarget).attr('data-score-id');
    let url = '/hymns/to-score-upload?scoreId=' + scoreId + '&pageNum=' + pageNum;
    checkPermissionAndTransfer(url);
});
$tableBody.on("click", '.link-btn', (e) => {
    e.preventDefault();
    let transferVal = $(e.currentTarget).attr('data-transfer-val');
    window.open(transferVal);
});
$tableBody.on("click", '.score-download-btn', (e) => {
    e.preventDefault();
    let scoreId = $(e.currentTarget).attr('data-score-id');
    window.location.href = '/hymns/score-download?scoreId=' + scoreId;
});

function toSelectedPg(pageNum, keyword) {
    $.ajax({
        url: '/hymns/pagination',
        data: {
            'pageNum': pageNum,
            'keyword': keyword
        },
        success: (response) => {
            buildTableBody(response);
            buildPageInfos(response);
            buildPageNavi(response);
        },
        error: (xhr) => {
            let message = trimQuote(xhr.responseText);
            layer.msg(message);
        }
    });
}

function buildTableBody(response) {
    $tableBody.empty();
    let index = response.records;
    $.each(index, (index, item) => {
        let nameJpTd = $("<th class='text-left' style='width: 130px;vertical-align: middle;'></th>").append(item.nameJp);
        let nameKrTd = $("<td class='text-left' style='width: 100px;vertical-align: middle;'></td>").append(item.nameKr);
        let linkTd = $("<td class='text-center' style='width: 20px;vertical-align: middle;'></td>")
            .append($("<a href='#' class='link-btn' data-transfer-val='" + item.link + "'>Link</a>"));
        let scoreTd = $("<td class='text-center' style='width: 20px;vertical-align: middle;'></td>")
            .append($("<a href='#' class='score-download-btn' data-score-id='" + item.id + "'>&#x1D11E;</a>"));
        let scoreBtn = $("<button></button>").addClass("btn btn-success btn-sm score-btn")
            .append($("<i class='fa-solid fa-music'></i>")).append("楽譜");
        scoreBtn.attr("data-score-id", item.id);
        let editBtn = $("<button></button>").addClass("btn btn-primary btn-sm edit-btn")
            .append($("<i class='fa-solid fa-pencil'></i>")).append("編集");
        editBtn.attr("data-edit-id", item.id);
        let deleteBtn = $("<button></button>").addClass("btn btn-danger btn-sm delete-btn")
            .append($("<i class='fa-solid fa-trash'></i>")).append("削除");
        deleteBtn.attr("data-delete-id", item.id);
        let btnTd = $("<td class='text-center' style='width: 80px;vertical-align: middle;'></td>")
            .append(scoreBtn).append(" ").append(editBtn).append(" ").append(deleteBtn);
        $("<tr></tr>").append(nameJpTd).append(nameKrTd).append(linkTd).append(scoreTd).append(btnTd).appendTo("#tableBody");
    });
}
