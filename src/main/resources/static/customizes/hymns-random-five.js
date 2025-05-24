let $tableBody = $("#tableBody");
$(document).ready(() => {
    let $toRandomFive = $("#toRandomFive");
    $toRandomFive.css('color', '#006b3c');
    $toRandomFive.addClass('animate__animated animate__flipInY');
});
$("#randomSearchBtn").on("click", () => {
    keyword = $("#keywordInput").val();
    retrieveRandomFive(keyword);
});
$tableBody.on("click", '.link-btn', (e) => {
    e.preventDefault();
    let transferVal = $(e.currentTarget).attr('data-transfer-val');
    window.open(transferVal);
});

function retrieveRandomFive(keyword) {
    $.ajax({
        url: '/hymns/random-five-retrieve',
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
            .append($("<a href='#' class='link-btn' data-transfer-val='" + item.link + "'>" + item.nameJp + delimiter + item.nameKr + "</a>"));
        $("<tr></tr>").append(nameMixTd).appendTo("#tableBody");
    });
}
