<!DOCTYPE html>
<html>
<head>
    <title>Hi.</title>
</head>
<body>
    <textarea id="question">
    </textarea>
    <input type="button" value="test" onclick="ask"/>
</body>
<script>
    function ask() {
        alert('test');

        const question = document.getElementById("question");

        const url = '/maum-admin/api/llmask';
        fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                question: question
            }),
        })
            .then((response) => console.log(response));
    }
</script>
</html>