package com.example.leanne.getsms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EnterNumberActivity extends Activity implements View.OnClickListener {
    private EditText bankNumberEditText;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_number);

        initView();
    }

    private void initView() {
        bankNumberEditText = (EditText) findViewById(R.id.bank_number_edit_text);
        Button saveNumberButton = (Button) findViewById(R.id.save_button);
        saveNumberButton.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_button:
                String phoneNumber = bankNumberEditText.getText().toString();
                if (phoneNumber.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "string empty", Toast.LENGTH_SHORT).show();
                    break;
                }
                // save number to "BankNumbers"
                BankNumbers.getInstance().setBankNumberArrayList(phoneNumber);
                Intent getSms = new Intent(EnterNumberActivity.this, GetSmsActivity.class);
                startActivity(getSms);
        }
    }
}
