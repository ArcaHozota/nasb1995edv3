// === DOM Element References ===
const torokuBox = document.getElementById("torokuBox");
const loginBox = document.getElementById("loginBox");
const eyeIcons = document.getElementById("eyeIcons");
const passwordIpt = document.getElementById("passwordIpt");
const torokuMsgContainer = document.getElementById("torokuMsgContainer");
const emailIpt = document.getElementById("emailIpt");
const loginBtn = document.getElementById("loginBtn");
const loginForm = document.getElementById("loginForm");
const torokuBtn = document.getElementById("torokuBtn");
const passwordIpt1 = document.getElementById("passwordIpt1");
const passwordIpt2 = document.getElementById("passwordIpt2");
const accountIpt = document.getElementById("accountIpt");


document.addEventListener("DOMContentLoaded", () => {
    torokuBox.querySelector(".toroku-title").addEventListener("click", () => {
        if (torokuBox.classList.contains("slide-up")) {
            loginBox.classList.add("slide-up");
            torokuBox.classList.remove("slide-up");
        }
    });

    loginBox.querySelector(".login-title").addEventListener("click", () => {
        if (loginBox.classList.contains("slide-up")) {
            torokuBox.classList.add("slide-up");
            loginBox.classList.remove("slide-up");
        }
    });

    let flag = 0;
    eyeIcons.addEventListener("click", (e) => {
        if (flag === 0) {
            passwordIpt.setAttribute('type', 'text');
            e.currentTarget.setAttribute('name', 'eye-off-outline');
            flag = 1;
        } else {
            passwordIpt.setAttribute('type', 'password');
            e.currentTarget.setAttribute('name', 'eye-outline');
            flag = 0;
        }
    });

    const message1 = torokuMsgContainer?.value;
    if (message1 !== emptyString && message1 !== null && message1 !== undefined) {
        layer.msg(message1);
    }
});

emailIpt.addEventListener("change", function () {
    const inputEmail = this.value;
    const regularEmail = /^[a-zA-Z\d._%+-]+@[a-zA-Z\d.-]+\.[a-zA-Z]{2,}$/;
    if (inputEmail.includes("@") && !regularEmail.test(inputEmail)) {
        layer.msg('入力したメールアドレスが正しくありません。');
    }
});

loginBtn.addEventListener("click", () => {
    const account = accountIpt.value.trim();
    const password = passwordIpt.value.trim();

    if (account === emptyString && password === emptyString) {
        layer.msg('アカウントとパスワードを入力してください。');
    } else if (account === emptyString) {
        layer.msg('アカウントを入力してください。');
    } else if (password === emptyString) {
        layer.msg('パスワードを入力してください。');
    } else {
        loginForm.submit();
    }
});

torokuBtn.addEventListener("click", () => {
    const inputElements = [emailIpt, passwordIpt1, passwordIpt2];

    for (const el of inputElements) {
        if (el.value.trim() === emptyString) {
            layer.msg('入力しなかった情報があります。');
            return;
        }
    }

    const password01 = passwordIpt1.value;
    const password02 = passwordIpt2.value;

    if (password01 !== password02) {
        layer.msg('入力したパスワードが不一致です。');
    } else {
        layer.msg('すみませんが、当機能はまだ実装されていません');
    }
});