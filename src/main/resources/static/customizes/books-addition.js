$(document).ready(() => {
    let $toTemporary = $("#toTemporary");
    $toTemporary.css('color', '#006b3c');
    $toTemporary.addClass('animate__animated animate__flipInY');
});
$("#bookInput").on("change", (e) => {
    $("#chapterInput").empty();
    let bookId = $(e.currentTarget).val();
    $.ajax({
        url: '/books/get-chapters',
        data: 'bookId=' + bookId,
        success: (response) => {
            $.each(response, (_, item) => {
                let optionElement = $("<option></option>").val(item.id).text(item.name);
                optionElement.appendTo("#chapterInput");
            });
        }
    });
});
$("#infoStorageBtn").on("click", () => {
    let inputArrays = ["#phraseIdInput", "#phraseTextEnInput", "#phraseTextJpInput"];
    for (const array of inputArrays) {
        $(array).removeClass("is-valid is-invalid");
        $(array).next("span").removeClass("valid-feedback invalid-feedback");
        $(array).next("span").text(emptyString);
    }
    let listArray = projectInputContextGet(inputArrays);
    if (listArray.includes(emptyString)) {
        projectNullInputBoxDiscern(inputArrays);
    } else if ($("#inputForm").find('*').hasClass('is-invalid')) {
        layer.msg(inputWarning);
    } else {
        let postData = JSON.stringify({
            'chapterId': $("#chapterInput").val(),
            'id': $("#phraseIdInput").val().trim(),
            'textEn': $("#phraseTextEnInput").val().trim(),
            'textJp': $("#phraseTextJpInput").val().trim()
        });
        projectAjaxModify('/books/info-storage', POST, postData, booksPostSuccessFunction);
    }
});

function booksPostSuccessFunction(response) {
    formReset("#inputForm");
    formReset("#inputForm2");
    layer.msg(response);
}
