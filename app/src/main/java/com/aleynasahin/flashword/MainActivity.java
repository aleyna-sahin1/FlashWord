package com.aleynasahin.flashword;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aleynasahin.flashword.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean isFrontVisible = true;
    ArrayList<Word> wordArrayList;
    WordAdapter adapter;
    SQLiteDatabase database;
    int currentWordId;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        database = this.openOrCreateDatabase("Words", MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS words (id INTEGER PRIMARY KEY, word VARCHAR, meaning VARCHAR,correct_count INTEGER DEFAULT 0,wrong_count INTEGER DEFAULT 0)");
        wordArrayList = new ArrayList<>();
        adapter = new WordAdapter(wordArrayList);
        binding.cardFront.setOnClickListener(v -> {
            flipCard();
            binding.btnNext.setVisibility(View.INVISIBLE);
            binding.btnCheck.setVisibility(View.INVISIBLE);
            binding.editTextAnswer.setVisibility(View.INVISIBLE);
        });

        binding.cardBack.setOnClickListener(v -> {
            flipCard();
            binding.btnNext.setVisibility(View.VISIBLE);
            binding.btnCheck.setVisibility(View.VISIBLE);
            binding.editTextAnswer.setVisibility(View.VISIBLE);
            // Kartı çevirme animasyonunu başlat
        });

        showRandomWord();
        PressAnimListener listener = new PressAnimListener(this); // this = Activity context
        binding.btnNext.setOnTouchListener(new PressAnimListener(this));
        binding.btnCheck.setOnTouchListener(new PressAnimListener(this));
    }
    public void check(View view) {

        String userAnswer = binding.editTextAnswer.getText().toString().trim();
        String correctAnswer = binding.meaningTextView.getText().toString().trim();
        if (userAnswer.equals(correctAnswer)) {

            Toast.makeText(this, "Correct!", Toast.LENGTH_LONG).show();

            database.execSQL("UPDATE words SET correct_count = correct_count + 1 WHERE id = ?",
                    new Object[]{ currentWordId }
            );

        } else {
            Toast.makeText(this, "Wrong!", Toast.LENGTH_LONG).show();

            database.execSQL(
                    "UPDATE words SET wrong_count = wrong_count + 1 WHERE id = ?",
                    new Object[]{ currentWordId }
            );
        }
        binding.btnCheck.setEnabled(false);

    }
    private void showRandomWord() {
        try {
            Cursor cursor = database.rawQuery("SELECT id,word, meaning FROM words ORDER BY RANDOM() LIMIT 1", null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int idIdx = cursor.getColumnIndex("id");
                    int wordIdx = cursor.getColumnIndex("word");
                    int meaningIdx = cursor.getColumnIndex("meaning");

                    if (idIdx != -1 && wordIdx != -1 && meaningIdx != -1) {
                        currentWordId = cursor.getInt(idIdx);
                        String word = cursor.getString(wordIdx);
                        String meaning = cursor.getString(meaningIdx);

                        binding.wordTextView.setText(word);
                        binding.meaningTextView.setText(meaning);

                    }
                }
                cursor.close();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void nextWord(View view) {
        showRandomWord();
        binding.editTextAnswer.setText("");
        binding.btnCheck.setEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.word_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.item_add_word) {
            Intent intent = new Intent(this, WordActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.item_word_list) {
            Intent intent = new Intent(this, WordList.class);
            startActivity(intent);
        }else if (item.getItemId() == R.id.item_statistics) {
            startActivity(new Intent(this, StatisticsActivity.class));
        }


        return super.onOptionsItemSelected(item);
    }

    private void flipCard() {
        if (isFrontVisible) {
            animateCard(binding.cardFront, binding.cardBack);
        } else {
            animateCard(binding.cardBack, binding.cardFront);
        }
        isFrontVisible = !isFrontVisible;
    }

    private void animateCard(View visibleView, View hiddenView) {
        visibleView.animate().rotationY(90).setDuration(200).withEndAction(() -> {
            visibleView.setVisibility(View.GONE);
            hiddenView.setVisibility(View.VISIBLE);
            hiddenView.setRotationY(-90);
            hiddenView.animate().rotationY(0).setDuration(200).start();
        }).start();
    }

}