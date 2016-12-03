package com.example.leanne.getsms;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GetSmsActivity extends AppCompatActivity implements View.OnClickListener {
    private ArrayList<String> bankSMSArrayList;
    private String devicePhoneNumber;
    private ArrayList<SMSParsed> smsParsedArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bankSMSArrayList = new ArrayList<>();
        smsParsedArrayList = new ArrayList<>();

        Button button = (Button) findViewById(R.id.button);
        assert button != null;
        button.setOnClickListener(this);

        // 유저 핸드폰 번호 GET
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        devicePhoneNumber = telephonyManager.getLine1Number();
    }

    @Override
    public void onClick(View v) {
        String bankNumber = BankNumbers.getInstance().getBankNumberArrayList().get(0);
        switch (v.getId()) {
            case R.id.button:
                Log.d("TAG - 유저핸드폰번호", devicePhoneNumber);
                Log.d("TAG - 은행번호", bankNumber);
                Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
                assert cursor != null;
                if (cursor.moveToFirst()) {
                    do {
                        // 앞 Activity에서 입력한 은행 번호와 일치하면 리스트에 저장
                        if (cursor.getString(cursor.getColumnIndex("address")).equals(bankNumber))
                            bankSMSArrayList.add(cursor.getString(cursor.getColumnIndex("body")));
                    } while (cursor.moveToNext());
                }
                cursor.close();
                parseSMS();
                if (!smsParsedArrayList.isEmpty()) {
                    Log.d("TAG", "SMS not empty, SUCCESS");
                    // Thread로 웹서버에 접속
                    new Thread() {
                        public void run() {
                            Log.d("TAG", "Start Sending to Server");
                            sendToServer();
                        }
                    }.start();
                }
                Toast.makeText(this, "전송 완료", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void parseSMS() {
        if (bankSMSArrayList.isEmpty())
            Toast.makeText(getBaseContext(), "은행번호를 잘못입력했거나 내용이 없네요", Toast.LENGTH_SHORT).show();
        for (int i = 0; i < bankSMSArrayList.size(); i++) {
            // parse sms
            String[] splitSMS = bankSMSArrayList.get(i).split("\\s+");
            String money = "";
            String date = "";

            for(int j=0; j<splitSMS.length; j++) {

                // 날짜 찾기
                if (Character.toString(splitSMS[j].charAt(2)).equals("/")) {    // 3번째 문자가 "/"인지 확인
                    if (splitSMS[j].length() == 5) {                            // 길이가 5인지 확인
                        Log.d("TAG - checking", "날짜 가져옴");
                        date = splitSMS[j];                                     // 날짜 저장
                        continue;
                    }
                }

                // 금액 찾기
                String invertString = new StringBuilder(splitSMS[j]).reverse().toString();
                if (Character.toString(invertString.charAt(0)).equals("원")) {           // 끝에"원" 있는지 확인

                    // 앞에 "잔액" 혹은 "누적"이 있으면 건너뛰기
                    if(splitSMS[j].substring(0,2).equals("누적") | splitSMS[j].substring(0,2).equals("잔액"))
                        continue;

                    // 금액이 천 단위 이상일 경우
                    if(splitSMS[j].length()>=6) {
                        if (Character.toString(invertString.charAt(4)).equals(",")) {     // 4번째 문자가 쉼표인지
                            Log.d("TAG - checking", "문자 파싱 the end");
                        }else{
                            continue;
                        }
                    }

                    // 앞에 "출금" 붙어있으면 떼기
                    String test = splitSMS[j].substring(0,2);
                    if(splitSMS[j].substring(0,2).equals("출금")) {
                        splitSMS[j] = splitSMS[j].replace("출금", "");
                    }
                    Log.d("TAG - checking", "금액 확인 완료");
                    money = splitSMS[j];                                    // 금액 저장
                }
            }

            // 금액 or 날짜 없으면 토스트 띄우기
            if(date.equals("") | money.equals(""))
                Toast.makeText(this, "ERROR : no money or date", Toast.LENGTH_SHORT).show();

            smsParsedArrayList.add(new SMSParsed(date, money));
            Log.d("TAG", "날짜 : " + date + ", 금액 : " + money);

        }
    }

    private void sendToServer() {
        JSONArray jsonArray = new JSONArray();
        for (SMSParsed sms : smsParsedArrayList) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("date", sms.getDate());
                jsonObject.put("money", sms.getMoney());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        StringBuffer buffer = new StringBuffer(jsonArray.toString());
        Log.d("TAG", buffer.toString());

        connectToWebServer(buffer);
    }

    public void connectToWebServer(StringBuffer buffer) {
        try {
            URL url = new URL("http://leannelim0629.cafe24.com/sms/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("charset", "utf-8");

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write("phone_number=" + devicePhoneNumber + "&sms_list=" + buffer.toString());
            writer.flush();
            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            buffer = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                buffer.append(line);
            }
            Log.d("TAG", String.valueOf(buffer));
            writer.close();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
