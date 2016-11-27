package com.example.leanne.getsms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

/**
 * Created by avoca on 11/27/2016.
 */

public class PairActivity extends Activity implements View.OnClickListener {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_comp);
        initView();
        }

    private void initView() {
        TextView pairNum = (TextView) findViewById(R.id.pair_number);
        pairNum.setText(getRandomNum());    // set random 6-digit num
        Button nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(this);
        }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next_button:
                // do something
                Intent enterNum = new Intent(PairActivity.this, EnterNumberActivity.class);
                startActivity(enterNum);
        }
    }

    private String getRandomNum() {
        String num;
        Random rand = new Random();
        num = Integer.toString(rand.nextInt((999999 - 100000) + 1) + 100000);
        return " "+num+" ";
    }
}
