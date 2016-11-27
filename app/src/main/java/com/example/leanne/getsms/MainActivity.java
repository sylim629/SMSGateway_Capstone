package com.example.leanne.getsms;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);

        initView();
    }

    private void initView() {
        Button buttonAgree = (Button) findViewById(R.id.agree_button);
        Button buttonNoThanks = (Button) findViewById(R.id.no_thanks_button);
        assert buttonAgree != null;
        buttonAgree.setOnClickListener(this);
        assert buttonNoThanks != null;

        ///수정 테스트
        buttonNoThanks.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.agree_button:
                Intent enterNumber = new Intent(MainActivity.this, PairActivity.class);
                startActivity(enterNumber);
            case R.id.no_thanks_button:
                this.finishAffinity();
        }
    }
}
