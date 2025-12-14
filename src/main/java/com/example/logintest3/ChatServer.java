package com.example.logintest3;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ServerEndpoint("/chat/{userId}")
public class ChatServer {

    private static final List<Session> sessions = new ArrayList<>();

    @OnOpen
    public void onOpen(Session newUser, @PathParam("userId") String userId) {
        System.out.println("접속: " + newUser.getId() + " (" + userId + ")");
        sessions.add(newUser);

        for (Session s : sessions) {
            try {
                s.getBasicRemote().sendText(userId + "님이 입장하였습니다.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnMessage
    public void onMessage(Session user, String message, @PathParam("userId") String userId) {
        System.out.println(userId + " 메시지: " + message);

        // 모든 사용자에게 메시지 전송
        for (Session s : sessions) {
            try {
                s.getBasicRemote().sendText(userId + " : " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClose
    public void onClose(Session user, @PathParam("userId") String userId) {
        sessions.remove(user);

        for (Session s : sessions) {
            try {
                s.getBasicRemote().sendText(userId + "님이 퇴장하였습니다.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(userId + " 연결 종료");
    }
}