let $torokuBox = $("#torokuBox");
let $loginBox = $("#loginBox");
$(document).ready(() => {
    $torokuBox.on("click", '.toroku-title', () => {
        if ($torokuBox.hasClass('slide-up')) {
            $loginBox.addClass('slide-up');
            $torokuBox.removeClass('slide-up');
        }
    });
    $loginBox.on("click", '.login-title', () => {
        if ($loginBox.hasClass('slide-up')) {
            $torokuBox.addClass('slide-up');
            $loginBox.removeClass('slide-up');
        }
    });
    let flag = 0;
    $("#eyeIcons").on("click", (e) => {
        if (flag === 0) {
            $("#passwordIpt").attr('type', 'text');
            $(e.currentTarget).attr('name', 'eye-off-outline');
            flag = 1;
        } else {
            $("#passwordIpt").attr('type', 'password');
            $(e.currentTarget).attr('name', 'eye-outline');
            flag = 0;
        }
    });
    let message1 = $("#torokuMsgContainer").val();
    if (message1 !== emptyString && message1 !== null && message1 !== undefined) {
        layer.msg(message1);
    }
});
$("#emailIpt").on("change", () => {
    let inputEmail = this.value;
    let regularEmail = /^[a-zA-Z\d._%+-]+@[a-zA-Z\d.-]+\.[a-zA-Z]{2,}$/;
    if (inputEmail.includes("@")) {
        if (!regularEmail.test(inputEmail)) {
            layer.msg('入力したメールアドレスが正しくありません。');
        }
    }
});
$("#loginBtn").on("click", () => {
    let account = $("#accountIpt").val().trim();
    let password = $("#passwordIpt").val().trim();
    if (account === emptyString && password === emptyString) {
        layer.msg('アカウントとパスワードを入力してください。');
    } else if (account === emptyString) {
        layer.msg('アカウントを入力してください。');
    } else if (password === emptyString) {
        layer.msg('パスワードを入力してください。');
    } else {
        $("#loginForm").submit();
    }
});
$("#torokuBtn").on("click", () => {
    let inputArrays = ["#emailIpt", "#passwordIpt1", "#passwordIpt2"];
    for (const element of inputArrays) {
        if ($(element).val().trim() === emptyString) {
            layer.msg('入力しなかった情報があります。');
            return;
        }
    }
    let password01 = $("#passwordIpt1").val();
    let password02 = $("#passwordIpt2").val();
    if (password01 !== password02) {
        layer.msg('入力したパスワードが不一致です。');
    } else {
        layer.msg('すみませんが、当機能はまだ実装されていません');
    }
});
