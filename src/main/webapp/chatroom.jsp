<%@ page contentType="text/html; charset=UTF-8" %>
<%
    String loginId  = (String) session.getAttribute("loginId");   // kakao_4475...
    String nickname = (String) session.getAttribute("nickname");  // 한재훈
    String userId = (String) session.getAttribute("nickname");
    if (userId == null || userId.trim().isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    // 캐시 방지 (옵션)
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Chat Room</title>
    <style>
        .msgArea {
            border: 1px solid #ccc;
            width: 400px;
            height: 300px;
            overflow-y: auto;
            padding: 8px;
            margin-bottom: 10px;
        }
    </style>
</head>
<body>

<h2>웹소켓 채팅</h2>
<p>로그인한 사용자: <strong><%= userId %></strong></p>

<div class="msgArea"></div>

<input type="text" class="content" placeholder="메시지를 입력하세요">
<button type="button" onclick="sendMsg()">보내기</button>
<button type="button" onclick="disconnect()">종료</button>

<script>
    const contextPath = "<%= request.getContextPath() %>";
    const userId = "<%= userId %>";

    const wsUrl = "ws://" + location.hostname + ":8089"
        + contextPath + "/chat/"
        + encodeURIComponent(userId);

    console.log("WebSocket URL =", wsUrl);

    let socket = new WebSocket(wsUrl);

    socket.onopen = function () {
        console.log("WebSocket 연결 성공");
    };

    socket.onerror = function (e) {
        console.error("WebSocket 오류:", e);
    };

    socket.onmessage = function (e) {
        let box = document.querySelector(".msgArea");
        let msg = document.createElement("div");
        msg.innerText = e.data;
        box.append(msg);
        box.scrollTop = box.scrollHeight;
    };

    function sendMsg() {
        if (!socket || socket.readyState !== WebSocket.OPEN) {
            alert("웹소켓 연결이 아직 열리지 않았습니다.");
            return;
        }

        let input = document.querySelector('.content');
        let msg = input.value.trim();
        if (msg !== "") {
            socket.send(msg);
            input.value = "";
        }
    }

    function disconnect() {
        if (socket && socket.readyState === WebSocket.OPEN) {
            socket.close();
            alert("채팅을 종료합니다.");
        }
    }
</script>

</body>
</html>

