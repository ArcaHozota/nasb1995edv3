let $tableBody = $("#tableBody");
let $kanumiSearchBtn = $("#kanamiSearchBtn");
let $nameDisplay = $("#nameDisplay");
let $loadingBackground2 = $("#loadingBackground2");
let keyword = null
$(document).ready(() => {
    adjustWidth();
    toSelectedPg(1, keyword);
    $kanumiSearchBtn.on("mousemove", (e) => {
        const $btn = $(e.currentTarget);           // 常にイベント発生元
        const offset = $btn.offset();
        const x = e.pageX - offset.left;
        const y = e.pageY - offset.top;
        $btn.css('--x', `${x}px`)
            .css('--y', `${y}px`);
    });
});
$kanumiSearchBtn.on("click", (e) => {
    e.preventDefault();
    let hymnId = $nameDisplay.attr('data-id-val');
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
                $loadingBackground2.show();
                $kanumiSearchBtn.css('pointer-events', "none");
                kanumiRetrieve(hymnId);
                setTimeout(() => {
                    $loadingBackground2.hide();
                    $kanumiSearchBtn.css('pointer-events', "auto");
                    let nameJp = $('.table-danger').find("td:eq(1)").children("a").text();
                    let slashIndex = nameJp.indexOf('/');
                    $nameDisplay.text('検索完了---' + nameJp.substring(0, slashIndex));
                    $nameDisplay.attr('data-id-val', 0);
                }, 132000);
            }
        });
    }
});
$tableBody.on("change", '.form-check-input', (e) => {
    $('.form-check-input').not(e.currentTarget).prop('checked', false);
});
$tableBody.on("click", '.form-check-input', (e) => {
    if ($(e.currentTarget).prop('checked')) {
        let idVal = $(e.currentTarget).val();
        $.ajax({
            url: '/hymns/get-info-id',
            data: 'hymnId=' + idVal,
            success: (response) => {
                $nameDisplay.text(response.nameJp);
                $nameDisplay.attr('data-id-val', response.id);
            },
            error: (xhr) => {
                let message = trimQuote(xhr.responseText);
                layer.msg(message);
            }
        });
    } else {
        $nameDisplay.attr('data-id-val', 0);
        $.ajax({
            url: '/hymns/get-records',
            success: (response) => {
                $nameDisplay.text('賛美歌' + response + '曲レコード済み');
            },
            error: (xhr) => {
                let message = trimQuote(xhr.responseText);
                layer.msg(message);
            }
        });
    }
});
$tableBody.on("click", '.link-btn', (e) => {
    e.preventDefault();
    let transferVal = $(e.currentTarget).attr('data-transfer-val');
    window.open(transferVal);
});

function toSelectedPg(pageNum, keyword) {
    $.ajax({
        url: '/hymns/pagination',
        data: 'pageNum=' + pageNum,
        success: (response) => {
            buildTableBody1(response);
            buildPageInfos(response);
            buildPageNavi(response);
        },
        error: (xhr) => {
            let message = trimQuote(xhr.responseText);
            layer.msg(message);
        }
    });
}

function buildTableBody1(response) {
    $tableBody.empty();
    let index = response.records;
    $.each(index, (_, item) => {
        let checkBoxTd = $("<td class='text-center' style='width: 10%;vertical-align: middle;'></td>")
            .append($("<input class='form-check-input mt-0' style='vertical-align: middle;' type='checkbox' value='" + item.id + "'>"));
        let nameMixTd = $("<td class='text-left' style='width: 70%;vertical-align: middle;'></td>")
            .append($("<a href='#' class='link-btn' data-transfer-val='" + item.link + "'>" + item.nameJp + delimiter + item.nameKr + "</a>"));
        let scoreTd = $("<td class='text-center' style='width: 20%;vertical-align: middle;'></td>")
            .append($("<a href='#' class='score-download-btn' data-score-id='" + item.id + "'>&#x1D11E;</a>"));
        $("<tr></tr>").append(checkBoxTd).append(nameMixTd).append(scoreTd).appendTo("#tableBody");
    });
}

function kanumiRetrieve(hymnId) {
    $.ajax({
        url: '/hymns/kanumi-retrieve',
        data: 'hymnId=' + hymnId,
        success: (response) => {
            buildTableBody2(response);
        },
        error: (result) => {
            layer.msg(result.responseJSON.message);
        }
    });
}

function buildTableBody2(response) {
    $tableBody.empty();
    $.each(response, (_, item) => {
        let checkBoxTd = $("<td class='text-center' style='width: 10%;vertical-align: middle;'></td>")
            .append($("<input class='form-check-input mt-0' style='vertical-align: middle;' type='checkbox' value='" + item.id + "'>"));
        let nameMixTd = $("<td class='text-left' style='width: 70%;vertical-align: middle;'></td>")
            .append($("<a href='#' class='link-btn' data-transfer-val='" + item.link + "'>" + item.nameJp + delimiter + item.nameKr + "</a>"));
        let scoreTd = $("<td class='text-center' style='width: 20%;vertical-align: middle;'></td>")
            .append($("<a href='#' class='score-download-btn' data-score-id='" + item.id + "'>&#x1D11E;</a>"));
        if (item.lineNumber === 'BURGUNDY') {
            $("<tr class='table-danger'></tr>").append(checkBoxTd).append(nameMixTd).append(scoreTd).appendTo("#tableBody");
        } else if (item.lineNumber === 'NAPLES') {
            $("<tr class='table-warning'></tr>").append(checkBoxTd).append(nameMixTd).append(scoreTd).appendTo("#tableBody");
        } else if (item.lineNumber === 'CADMIUM') {
            $("<tr class='table-success'></tr>").append(checkBoxTd).append(nameMixTd).append(scoreTd).appendTo("#tableBody");
        } else {
            $("<tr class='table-light'></tr>").append(checkBoxTd).append(nameMixTd).append(scoreTd).appendTo("#tableBody");
        }
    });
}

function adjustWidth() {
    const $indexTable = $("#indexTable");
    if ($indexTable.length) {
        $('.background2').css('width', $indexTable.outerWidth() + 'px');
    }
}