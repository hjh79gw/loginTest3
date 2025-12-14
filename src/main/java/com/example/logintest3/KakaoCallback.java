package com.example.logintest3;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@WebServlet("/kakaoCallback")
public class KakaoCallback extends HttpServlet {


    private static final String REST_API_KEY   = "REST_API_KEY";
    private static final String REDIRECT_URI   = "http://localhost:8089/loginTest3_war_exploded/kakaoCallback";
    private static final String CLIENT_SECRET  = "CLINET_SECRET_KEY";

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String code = request.getParameter("code");
        if (code == null || code.trim().isEmpty()) {
            response.getWriter().println("카카오 인가 코드(code)가 없습니다.");
            return;
        }

        /** 메서드 ========================================= */
        // 인가 코드로 엑세스 토큰 요청 메서드
        String accessToken = getAccessToken(code);
        if (accessToken == null) {
            response.getWriter().println("카카오 액세스 토큰 발급 실패");
            return;
        }

        // 2. 엑세스 토큰으로 사용자 정보 요청 메서드
        KakaoUser kakaoUser = getUserInfo(accessToken);
        if (kakaoUser == null) {
            response.getWriter().println("카카오 사용자 정보 조회 실패");
            return;
        }

        //3.세션에 저장 메서드
        HttpSession session = request.getSession();
        session.setAttribute("loginId",  "kakao_" + kakaoUser.id);
        session.setAttribute("userId", kakaoUser.nickname);
        session.setAttribute("nickname", kakaoUser.nickname);
        session.setAttribute("profileImage", kakaoUser.profileImageUrl);

        // 채팅 페이지 이동
        response.sendRedirect(request.getContextPath() + "/chatroom.jsp");
    }
        /** ========================================= */


    /** 동작 구조 ======================= */

    // 1. 카카오 토큰 발급
    private String getAccessToken(String code) {
        try {
            // POST 요청 파라미터 만들기
            String tokenUrl = "https://kauth.kakao.com/oauth/token";  //url
            StringBuilder postData = new StringBuilder();
            //body
            postData.append("grant_type=authorization_code");  //인가코드
            postData.append("&client_id=").append(URLEncoder.encode(REST_API_KEY, "UTF-8"));
            postData.append("&redirect_uri=").append(URLEncoder.encode(REDIRECT_URI, "UTF-8"));
            postData.append("&code=").append(URLEncoder.encode(code, "UTF-8"));

            if (CLIENT_SECRET != null && !CLIENT_SECRET.isEmpty()) {
                postData.append("&client_secret=").append(URLEncoder.encode(CLIENT_SECRET, "UTF-8"));
            }

            // HTTP 연결 설정
            URL url = new URL(tokenUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"); //카카오에서 요구한느 값

            // 요청 바디 쓰기
            try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                dos.writeBytes(postData.toString());
                dos.flush();
            }

            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();

            String result = readStream(is);
            System.out.println("토큰 응답 JSON = " + result);

            // json에서 무자열 파싱
            JsonObject root = JsonParser.parseString(result).getAsJsonObject();

            if (!root.has("access_token")) {
                System.out.println("access_token 없음 \n" + result);
                return null;
            }
            // 엑세스 토큰만 받아옴 (oauth 방식때문)
            return root.get("access_token").getAsString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // 2. 카카오에서 로그인한 유저 정보 가져오기
    private KakaoUser getUserInfo(String accessToken) {
        try {
            String apiUrl = "https://kapi.kakao.com/v2/user/me";
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            String result = readStream(conn.getInputStream());
            System.out.println("사용자 JSON = " + result);

            JsonObject json = JsonParser.parseString(result).getAsJsonObject();
            KakaoUser user = new KakaoUser();

            user.id = json.get("id").getAsString();

            //====== 유저 정보 json에서 가져오는 부분========
            JsonObject properties = json.getAsJsonObject("properties");
            if (properties != null) {
                if (properties.get("nickname") != null) {
                    user.nickname = properties.get("nickname").getAsString();
                }
                if (properties.get("profile_image") != null) {
                    user.profileImageUrl = properties.get("profile_image").getAsString();
                }
            }

            /**
             2) kakao_account.profile 에서 정보 가져오기, properteis에서 없을 때, 지금은 필요 x
          if (json.get("kakao_account") != null) {
                JsonObject account = json.getAsJsonObject("kakao_account");
                if (account != null && account.get("profile") != null) {
                    JsonObject profile = account.getAsJsonObject("profile");
                    if (profile != null) {
                        if ((user.nickname == null || user.nickname.isEmpty())
                                && profile.get("nickname") != null) {
                            user.nickname = profile.get("nickname").getAsString();
                        }
                        if (user.profileImageUrl == null && profile.get("profile_image_url") != null) {
                            user.profileImageUrl = profile.get("profile_image_url").getAsString();
                        }
                    }
                }
            }
            */

            if (user.nickname == null || user.nickname.isEmpty()) {
                user.nickname = "카카오사용자";
            }


            return user;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


     // Stream -> String 으로 변환 (네트워크 단)
    private String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
    /** ========================================= */

    //DTo
    private static class KakaoUser {
        String id;
        String nickname;
        String profileImageUrl;
    }
}
