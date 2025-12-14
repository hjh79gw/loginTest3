<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>카카오 로그인 테스트</title>
    <style>
        body {
            font-family: sans-serif;
            padding: 40px;
        }
        #kakaoLoginBtn {
            border: none;
            background: none;
            padding: 0;
            cursor: pointer;
        }
        #kakaoLoginBtn img {
            height: 45px;
        }
    </style>
</head>
<body>

<h2>카카오 로그인 페이지</h2>

<button id="kakaoLoginBtn" type="button" onclick="kakaoLogin()">
    <img src="<%= request.getContextPath() %>/img/kakao_login_medium_narrow.png"
         alt="카카오 로그인">
</button>

<script>
    function kakaoLogin() {

        const REST_API_KEY = '078237a6064b88ebe9d80d24f54e5054';
        const REDIRECT_URI = 'http://localhost:8089<%= request.getContextPath() %>/kakaoCallback';

        const kakaoAuthUrl =
            'https://kauth.kakao.com/oauth/authorize' +
            '?response_type=code' +
            '&client_id=' + encodeURIComponent(REST_API_KEY) +
            '&redirect_uri=' + encodeURIComponent(REDIRECT_URI);

        console.log('카카오 인가 요청 URL =', kakaoAuthUrl);
        window.location.href = kakaoAuthUrl;
    }
</script>

</body>
</html>
