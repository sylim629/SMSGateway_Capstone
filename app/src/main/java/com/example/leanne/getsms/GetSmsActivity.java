package com.example.leanne.getsms;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import java.net.MalformedURLException;
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
                    // Thread로 웹서버에 접속
                    new Thread() {
                        public void run() {
                            sendToServer();
                        }
                    }.start();
                }
                break;
        }
    }

    private void parseSMS() {
        if (bankSMSArrayList.isEmpty())
            Toast.makeText(getBaseContext(), "은행번호를 잘못입력했거나 내용이 없네요", Toast.LENGTH_SHORT).show();
        for (int i = 0; i < bankSMSArrayList.size(); i++) {
            // parse sms
            String[] splitSMS = bankSMSArrayList.get(i).split("\\s+");

            // splitSMS의 2번째 항목이 임*연(3*7*)인지 확인
            // 두 번째 글자가 "*"인지
            if (Character.toString(splitSMS[1].charAt(1)).equals("*"))
                Log.d("TAG - checking", "두 번째 글자 '*' 맞음");
            else {
                Log.d("SMS PARSE ERROR", "splitSMS[1].substring(1).equals(\"*\") 여기서 에러");
                Toast.makeText(this, "Error! Check log", Toast.LENGTH_SHORT).show();
                return;
            }
            // invert한 첫 두 글자가 ")*"인지
            String invertString = new StringBuilder(splitSMS[1]).reverse().toString();
            if (invertString.substring(0, 2).equals(")*"))
                Log.d("TAG - checking", "임*연(3*7*) 확인 완료");
            else {
                Log.d("SMS PARSE ERROR", "invertsplitSMS.substring(0,1).equals(\")*\") 여기서 에러");
                Toast.makeText(this, "Error! Check log", Toast.LENGTH_SHORT).show();
                return;
            }

            // spiltBody의 3번째 항목이 날짜인지 확인
            // 3번째 글자가 "/"인지
            if (Character.toString(splitSMS[2].charAt(2)).equals("/"))
                Log.d("TAG - checking", "날짜 확인 완료");
            else {
                Log.d("SMS PARSE ERROR", "splitSMS[2].substring(2).equals(\"/\") 여기서 에러");
                Toast.makeText(this, "Error! Check log", Toast.LENGTH_SHORT).show();
                return;
            }

            // splitSMS의 5번째 항목이 금액인지 확인
            // invert한 첫 글자가 "원"인지
            invertString = new StringBuilder(splitSMS[4]).reverse().toString();
            if (Character.toString(invertString.charAt(0)).equals("원"))
                Log.d("TAG - checking", "금액 확인 완료");
            else {
                Log.d("SMS PARSE ERROR", "invertString.substring(0).equals(\"원\") 여기서 에러");
                Toast.makeText(this, "Error! Check log", Toast.LENGTH_SHORT).show();
                return;
            }
            // invert 한 5번째 글자가 ","인지
            if (Character.toString(invertString.charAt(4)).equals(","))
                Log.d("TAG - checking", "문자 파싱 the end");
            else {
                Log.d("SMS PARSE ERROR", "invertString.substring(4).equals(\",\") 여기서 에러");
                Toast.makeText(this, "Error! Check log", Toast.LENGTH_SHORT).show();
                return;
            }

            String date = splitSMS[2];
            String money = splitSMS[4];
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
            writer.close();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
