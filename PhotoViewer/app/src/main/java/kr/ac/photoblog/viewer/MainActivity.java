package kr.ac.photoblog.viewer;

import java.io.ByteArrayOutputStream;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    // 에뮬레이터에서는 10.0.2.2가 개발 PC를 가리킵니다.
    private static final String SERVER_URL = "http://10.0.2.2:8000";
    // seed_demo 실행 결과의 실제 토큰으로 교체하세요.
    private static final String API_TOKEN = "5baed2207c8494036aaceca1a981bf0e2a887148";
    private static final int IMAGE_PICK_CODE = 101;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private LinearLayout 목록;
    private TextView 상태;
    private Uri 선택한사진;
    private EditText 제목입력, 본문입력;
    private ProgressBar 진행표시;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        화면구성();
    }

    private void 화면구성() {
        ScrollView 스크롤 = new ScrollView(this);
        LinearLayout 전체 = new LinearLayout(this);
        전체.setOrientation(LinearLayout.VERTICAL);
        전체.setPadding(20, 20, 20, 20);
        스크롤.addView(전체);

        TextView 제목 = new TextView(this);
        제목.setText("포토 블로그");
        제목.setTextSize(26);
        제목.setGravity(Gravity.CENTER);
        전체.addView(제목);

        상태 = new TextView(this);
        상태.setText("서버에 연결한 뒤 동기화를 눌러 주세요.");
        전체.addView(상태);

        LinearLayout 버튼줄 = new LinearLayout(this);
        Button 동기화 = new Button(this); 동기화.setText("동기화");
        동기화.setOnClickListener(v -> 게시물불러오기());
        Button 새글 = new Button(this); 새글.setText("새 게시물 작성");
        새글.setOnClickListener(v -> 작성창열기());
        버튼줄.addView(동기화, new LinearLayout.LayoutParams(0, -2, 1));
        버튼줄.addView(새글, new LinearLayout.LayoutParams(0, -2, 1));
        전체.addView(버튼줄);
        진행표시 = new ProgressBar(this); 진행표시.setVisibility(View.GONE); 전체.addView(진행표시);
        목록 = new LinearLayout(this); 목록.setOrientation(LinearLayout.VERTICAL); 전체.addView(목록);
        setContentView(스크롤);
    }

    private void 게시물불러오기() {
        진행표시.setVisibility(View.VISIBLE);
        상태.setText("서버 게시물을 가져오는 중입니다...");
        executor.execute(() -> {
            try {
                HttpURLConnection 연결 = (HttpURLConnection) new URL(SERVER_URL + "/api_root/Post/").openConnection();
                연결.setRequestMethod("GET"); 연결.setConnectTimeout(8000); 연결.setReadTimeout(12000);
                연결.setRequestProperty("Authorization", "Token " + API_TOKEN);
                int 코드 = 연결.getResponseCode();
                if (코드 != 200) throw new Exception("서버 응답 코드: " + 코드);
                String 응답 = new String(읽기(연결.getInputStream()), "UTF-8");
                JSONArray 게시물 = new JSONArray(응답);
                runOnUiThread(() -> {
                    목록.removeAllViews();
                    for (int i = 0; i < 게시물.length(); i++) {
                        try { 게시물카드추가(게시물.getJSONObject(i)); } catch (Exception 무시) {}
                    }
                    상태.setText("동기화 완료 · 게시물 " + 게시물.length() + "개");
                    진행표시.setVisibility(View.GONE);
                    Toast.makeText(this, "동기화가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception 오류) {
                runOnUiThread(() -> { 진행표시.setVisibility(View.GONE); 상태.setText("연결 실패: " + 오류.getMessage()); });
            }
        });
    }

    private void 게시물카드추가(JSONObject 게시물) throws Exception {
        LinearLayout 카드 = new LinearLayout(this); 카드.setOrientation(LinearLayout.VERTICAL); 카드.setPadding(8, 18, 8, 18);
        TextView 제목 = new TextView(this); 제목.setText(게시물.optString("title", "제목 없음")); 제목.setTextSize(20); 카드.addView(제목);
        TextView 본문 = new TextView(this); 본문.setText(게시물.optString("text", "")); 카드.addView(본문);
        String 이미지주소 = 게시물.optString("image", "");
        if (!이미지주소.isEmpty() && !이미지주소.equals("null")) {
            ImageView 이미지 = new ImageView(this); 이미지.setAdjustViewBounds(true); 이미지.setMaxHeight(700); 카드.addView(이미지);
            executor.execute(() -> {
                try {
                    URL 주소 = new URL(이미지주소);
                    HttpURLConnection 연결 = (HttpURLConnection) 주소.openConnection();
                    연결.setConnectTimeout(8000); 연결.setReadTimeout(12000);
                    Bitmap 비트맵 = BitmapFactory.decodeStream(연결.getInputStream());
                    runOnUiThread(() -> 이미지.setImageBitmap(비트맵));
                } catch (Exception 무시) {}
            });
        }
        TextView 구분선 = new TextView(this); 구분선.setText("────────────────────────");
        카드.addView(구분선); 목록.addView(카드);
    }

    private void 작성창열기() {
        LinearLayout 폼 = new LinearLayout(this); 폼.setOrientation(LinearLayout.VERTICAL); 폼.setPadding(24, 8, 24, 8);
        제목입력 = new EditText(this); 제목입력.setHint("게시물 제목"); 폼.addView(제목입력);
        본문입력 = new EditText(this); 본문입력.setHint("게시물 본문"); 폼.addView(본문입력);
        Button 사진선택 = new Button(this); 사진선택.setText("사진 선택"); 사진선택.setOnClickListener(v -> {
            Intent 의도 = new Intent(Intent.ACTION_OPEN_DOCUMENT); 의도.setType("image/*"); 의도.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(의도, IMAGE_PICK_CODE);
        }); 폼.addView(사진선택);
        new AlertDialog.Builder(this).setTitle("새 게시물 작성").setView(폼)
            .setPositiveButton("업로드", (대화상자, 번호) -> 게시물업로드())
            .setNegativeButton("취소", null).show();
    }

    @Override protected void onActivityResult(int 요청코드, int 결과코드, Intent 데이터) {
        super.onActivityResult(요청코드, 결과코드, 데이터);
        if (요청코드 == IMAGE_PICK_CODE && 결과코드 == RESULT_OK && 데이터 != null) {
            선택한사진 = 데이터.getData();
            Toast.makeText(this, "사진을 선택했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void 게시물업로드() {
        String 제목 = 제목입력.getText().toString().trim();
        String 본문 = 본문입력.getText().toString().trim();
        if (제목.isEmpty()) { Toast.makeText(this, "제목을 입력하세요.", Toast.LENGTH_SHORT).show(); return; }
        executor.execute(() -> {
            String 경계 = "----PhotoBlogBoundary7MA4YWxk";
            try {
                HttpURLConnection 연결 = (HttpURLConnection) new URL(SERVER_URL + "/api_root/Post/").openConnection();
                연결.setRequestMethod("POST"); 연결.setDoOutput(true); 연결.setConnectTimeout(8000); 연결.setReadTimeout(15000);
                연결.setRequestProperty("Authorization", "Token " + API_TOKEN);
                연결.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + 경계);
                DataOutputStream 출력 = new DataOutputStream(연결.getOutputStream());
                필드쓰기(출력, 경계, "title", 제목);
                필드쓰기(출력, 경계, "text", 본문);
                if (선택한사진 != null) {
                    String 파일명 = 파일이름(선택한사진);
                    출력.writeBytes("--" + 경계 + "\r\nContent-Disposition: form-data; name=\"image\"; filename=\"" + 파일명 + "\"\r\nContent-Type: image/jpeg\r\n\r\n");
                    try (InputStream 입력 = getContentResolver().openInputStream(선택한사진)) {
                        byte[] 버퍼 = new byte[8192]; int 길이;
                        while ((길이 = 입력.read(버퍼)) != -1) 출력.write(버퍼, 0, 길이);
                    }
                    출력.writeBytes("\r\n");
                }
                출력.writeBytes("--" + 경계 + "--\r\n"); 출력.flush(); 출력.close();
                int 코드 = 연결.getResponseCode();
                if (코드 != 201) throw new Exception("업로드 실패: HTTP " + 코드 + " (토큰을 확인하세요)");
                runOnUiThread(() -> { Toast.makeText(this, "게시물이 업로드되었습니다.", Toast.LENGTH_LONG).show(); 게시물불러오기(); });
            } catch (Exception 오류) {
                runOnUiThread(() -> Toast.makeText(this, 오류.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void 필드쓰기(DataOutputStream 출력, String 경계, String 이름, String 값) throws Exception {
        출력.writeBytes("--" + 경계 + "\r\nContent-Disposition: form-data; name=\"" + 이름 + "\"\r\n\r\n");
        출력.write(값.getBytes("UTF-8")); 출력.writeBytes("\r\n");
    }

    private String 파일이름(Uri uri) {
        String 이름 = "photo.jpg";
        try (Cursor 커서 = getContentResolver().query(uri, null, null, null, null)) {
            if (커서 != null && 커서.moveToFirst()) {
                int 열 = 커서.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (열 >= 0) 이름 = 커서.getString(열);
            }
        } catch (Exception 무시) {}
        return 이름.replace("\"", "");
    }

    @Override protected void onDestroy() { super.onDestroy(); executor.shutdownNow(); }
    private byte[] 읽기(InputStream 입력) throws IOException {
        ByteArrayOutputStream 출력 = new ByteArrayOutputStream();
        byte[] 임시 = new byte[4096];
        int 길이;

        while ((길이 = 입력.read(임시)) != -1) {
            출력.write(임시, 0, 길이);
        }

        return 출력.toByteArray();
    }
}
