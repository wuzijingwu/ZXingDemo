package com.example.wuzijing20170904;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private LinearLayout liner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        liner = (LinearLayout) findViewById(R.id.liner);
        ArrowMoveView arrowMoveView = new ArrowMoveView(this);
        arrowMoveView.init();
        liner.addView(arrowMoveView);
    }
}
