let $tableBody = $("#tableBody");
let $randomSearchBtn = $("#randomSearchBtn");
let $loadingContainer = $("#loadingContainer");
let $loadingBackground = $("#loadingBackground");
$(document).ready(() => {
    adjustWidth();
    $tableBody.hide();
    let message2 = $("#torokuMsgContainer").val();
    if (message2 !== emptyString && message2 !== null && message2 !== undefined) {
        layer.msg(message2);
    }
});
$randomSearchBtn.on("click", () => {
    adjustWidth();
    $loadingBackground.show();
    $loadingContainer.show();
    $tableBody.show();
    $randomSearchBtn.prop("disabled", true);
    let keyword = $("#keywordInput").val();
    commonRetrieve(keyword);
    setTimeout(() => {
        $loadingContainer.hide();
        $loadingBackground.hide();
        $randomSearchBtn.prop("disabled", false);
    }, 3300);
});
$tableBody.on("click", '.link-btn', (e) => {
    e.preventDefault();
    let transferVal = $(e.currentTarget).attr('data-transfer-val');
    window.open(transferVal);
});
$("#toIchiranHyoBtn").on("click", () => {
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
    $.ajax({
        url: '/hymns/common-retrieve',
        data: 'keyword=' + keyword,
        success: (response) => {
            buildTableBody(response);
        },
        error: (result) => {
            layer.msg(result.responseJSON.message);
        }
    });
}

function buildTableBody(response) {
    $tableBody.empty();
    $.each(response, (_, item) => {
        let nameMixTd = $("<td class='text-center' style='vertical-align: middle;'></td>")
            .append($("<a href='#' class='link-btn' data-transfer-val='" + item.link + "'>" + item.nameJp + "/" + item.nameKr + "</a>"));
        if (item.lineNumber === 'BURGUNDY') {
            $("<tr class='table-danger'></tr>").append(nameMixTd).appendTo("#tableBody");
        } else if (item.lineNumber === 'NAPLES') {
            $("<tr class='table-warning'></tr>").append(nameMixTd).appendTo("#tableBody");
        } else if (item.lineNumber === 'CADMIUM') {
            $("<tr class='table-success'></tr>").append(nameMixTd).appendTo("#tableBody");
        } else {
            $("<tr class='table-light'></tr>").append(nameMixTd).appendTo("#tableBody");
        }
    });
}

function adjustWidth() {
    const $indexTable = $("#indexTable");
    if ($indexTable.length) {
        $('.background').css('width', $indexTable.outerWidth() + 'px');
    }
}