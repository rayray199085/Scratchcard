package com.project.stephencao.scratchcard.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.project.stephencao.scratchcard.R;
import com.project.stephencao.scratchcard.view.ScratchcardView;

public class MainActivity extends AppCompatActivity {
    private ScratchcardView mScratchcardView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScratchcardView = findViewById(R.id.id_scratchcard);
        mScratchcardView.setOnScratchCardEndListener(new ScratchcardView.OnScratchCardEndListener() {
            @Override
            public void isEnd() {
                Toast.makeText(MainActivity.this,"Complete",Toast.LENGTH_SHORT).show();
            }
        });
        mScratchcardView.setTextContent("Thank you");
    }
}
