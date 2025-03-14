<!DOCTYPE html>
<html lang="kr">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
        <title>
            <#-- type 파라미터가 없을 경우 기본값을 사용 -->
            <#if type?exists && type == "admin">
                ADMIN 로그인
            <#else>
                API 문서 로그인
            </#if>
        </title>
        <!--
        <link href="/amore/resource/css/bootstrap.min.css" rel="stylesheet">
        <link href="/amore/resource/css/doc_login.css" rel="stylesheet">
        <script src="/amore/resource/node_modules/crypto-js/crypto-js.js" defer></script>
        <script src="/amore/resource/js/doc_login.js" defer></script>
        -->
        <link href="/resource/css/bootstrap.min.css" rel="stylesheet">
        <link href="/resource/css/doc_login.css" rel="stylesheet">
        <script src="/resource/node_modules/crypto-js/crypto-js.js" defer></script>
        <script src="/resource/js/doc_login.js" defer></script>
        <script type="text/javascript">
            <#-- 파라미터가 존재할 때 JavaScript 변수 설정 -->
            var loginType = "${type!"default"}";
        </script>
    </head>
    <body>
         <div class="container">
            <form class="form-signin">
                <h2 class="form-signin-heading">
                    <#if type?exists && type == "admin">
                        ADMIN 로그인
                    <#else>
                        API 문서 로그인
                    </#if>
                </h2>
                <p>
                    <label for="account" class="sr-only"></label>
                    <input type="text" id="account" name="account" class="form-control" placeholder="계정" required="" autofocus="">
                </p>
                <p>
                    <label for="password" class="sr-only"></label>
                    <input type="password" id="password" name="password" class="form-control" placeholder="비밀번호" required="">
                </p>
                <button class="btn btn-lg btn-primary btn-block" type="submit">로그인</button>
            </form>
        </div>
    </body>
</html>