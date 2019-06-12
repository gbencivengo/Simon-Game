package com.example.george.simongame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class GameOver extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);
        Intent intent = getIntent();
        int score = intent.getIntExtra("scoreCounter",0);

        TextView scoreCounterString = (TextView)findViewById(R.id.finalScoreString);
        scoreCounterString.setText("Score: " + score);
    }

    //starts the game again
    public void playAgainButton(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
