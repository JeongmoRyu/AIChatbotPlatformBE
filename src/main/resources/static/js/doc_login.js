document.getElementsByTagName("form")[0]
    .addEventListener("submit", e => {
        e.preventDefault();
        const data = new FormData(e.target);

        fetch("/doc/login", {
            method: "POST",
            headers: { 'Content-Type': 'application/json' },/**/
            body: JSON.stringify({
                account: data.get("account"),
                password: CryptoJS.SHA512(data.get("password")).toString()
            })
        })
        .then(res => res.json())
        .then(res => {
            if(res.result && res.code === "F000") {
                if (loginType === 'admin') {
                    // type=admin일 경우 A 페이지로 리다이렉트
                    location.href = "/maum-admin/monitor";
                } else {
                    // type=admin이 아닐 경우 B 페이지로 리다이렉트
                    location.href = "/doc";
                }
            }
            else alert(res.message);
        });
    });