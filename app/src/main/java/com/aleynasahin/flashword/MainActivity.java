package com.aleynasahin.flashword;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean isFrontVisible = true;
    ArrayList<Word> wordArrayList;
    WordAdapter adapter;
    SQLiteDatabase database;
    int currentWordId;
    int lastWordId = -1;
    int wrongTryCount = 0;
    boolean wrongCountWritten = false;


    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
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
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS user_progress (" +
                        "id INTEGER PRIMARY KEY, " +
                        "last_active_date TEXT, " +
                        "current_streak INTEGER, " +
                        "longest_streak INTEGER)"
        );

        database.execSQL(
                "CREATE TABLE IF NOT EXISTS daily_progress (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "date TEXT UNIQUE, " +
                        "solved_count INTEGER)"
        );


        database.execSQL(
                "INSERT OR IGNORE INTO user_progress (id, current_streak, longest_streak) VALUES (1, 0, 0)"
        );
        showStreak();
        showRandomWord();


        wordArrayList = new ArrayList<>();
        adapter = new WordAdapter(wordArrayList);
        binding.cardFront.setOnClickListener(v -> {
            flipCard();
            binding.btnCheck.setVisibility(View.INVISIBLE);
            binding.btnNext.setVisibility(View.INVISIBLE);
            binding.editTextAnswer.setVisibility(View.INVISIBLE);
        });

        binding.cardBack.setOnClickListener(v -> {
            flipCard();
            binding.btnNext.setVisibility(View.VISIBLE);
            binding.btnCheck.setVisibility(View.VISIBLE);
            binding.editTextAnswer.setVisibility(View.VISIBLE);

        });

        PressAnimListener listener = new PressAnimListener(this);
        binding.btnNext.setOnTouchListener(new PressAnimListener(this));
        binding.btnCheck.setOnTouchListener(new PressAnimListener(this));

        binding.editTextAnswer.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        });

    }

    private void hideKeyboard(View view) {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    public void check(View view) {
        if (binding.editTextAnswer.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter an answer", Toast.LENGTH_LONG).show();
            return;
        }

        String userAnswer = binding.editTextAnswer.getText().toString().trim();
        String correctAnswer = binding.meaningTextView.getText().toString().trim();
        if (userAnswer.equalsIgnoreCase(correctAnswer)) {

            Toast.makeText(this, "Correct!", Toast.LENGTH_LONG).show();

            database.execSQL("UPDATE words SET correct_count = correct_count + 1 WHERE id = ?",
                    new Object[]{currentWordId}
            );
            updateDailyProgress();
            showStreak();

            binding.cardFront.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        binding.cardFront.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .withEndAction(() -> {
                                    nextWord(null);

                                })
                                .start();
                    })
                    .start();


        } else {
            wrongTryCount++;
            Toast.makeText(this, "Wrong!", Toast.LENGTH_LONG).show();

            if (!wrongCountWritten) {
                database.execSQL(
                        "UPDATE words SET wrong_count = wrong_count + 1 WHERE id = ?",
                        new Object[]{currentWordId}
                );
                wrongCountWritten = true;
            }

            shakeCard();


        }
        binding.editTextAnswer.setText("");

    }

    private void showRandomWord() {

        wrongTryCount = 0;
        wrongCountWritten = false;
        try {
            Cursor cursor = database.rawQuery(
                    "SELECT id, word, meaning FROM words WHERE id != ? ORDER BY RANDOM() LIMIT 1",
                    new String[]{String.valueOf(lastWordId)}
            );
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int idIdx = cursor.getColumnIndex("id");
                    int wordIdx = cursor.getColumnIndex("word");
                    int meaningIdx = cursor.getColumnIndex("meaning");

                    if (idIdx != -1 && wordIdx != -1 && meaningIdx != -1) {
                        currentWordId = cursor.getInt(idIdx);
                        lastWordId = currentWordId;

                        String word = cursor.getString(wordIdx);
                        String meaning = cursor.getString(meaningIdx);

                        binding.wordTextView.setText(word);
                        binding.meaningTextView.setText(meaning);

                    }
                    if (cursor == null || !cursor.moveToFirst()) {
                        cursor = database.rawQuery(
                                "SELECT id, word, meaning FROM words ORDER BY RANDOM() LIMIT 1",
                                null
                        );
                        cursor.moveToFirst();
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
        } else if (item.getItemId() == R.id.item_statistics) {
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

    private void shakeCard() {
        ObjectAnimator animatorFront = ObjectAnimator.ofFloat(
                binding.cardFront,
                "rotation",
                0f, -10f, 8f, -6f, 4f, -2f, 2f, 0f
        );
        animatorFront.setDuration(800);
        animatorFront.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        animatorFront.start();

        ObjectAnimator animatorBack = ObjectAnimator.ofFloat(
                binding.cardBack,
                "rotation",
                0f, -10f, 8f, -6f, 4f, -2f, 2f, 0f
        );
        animatorBack.setDuration(800);
        animatorBack.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        animatorBack.start();
    }

    private void updateDailyProgress() {

        String today;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            today = java.time.LocalDate.now().toString();
        } else {

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            today = sdf.format(java.util.Calendar.getInstance().getTime());
        }

        Cursor cursor = database.rawQuery(
                "SELECT solved_count FROM daily_progress WHERE date = ?",
                new String[]{today}
        );

        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0) + 1;
            database.execSQL(
                    "UPDATE daily_progress SET solved_count = ? WHERE date = ?",
                    new Object[]{count, today}
            );
        } else {
            database.execSQL(
                    "INSERT INTO daily_progress (date, solved_count) VALUES (?, ?)",
                    new Object[]{today, 1}
            );
        }
        cursor.close();

        updateStreak(today);
    }

    private void updateStreak(String today) {

        Cursor c = database.rawQuery(
                "SELECT last_active_date, current_streak, longest_streak FROM user_progress WHERE id = 1",
                null
        );

        String lastDate = null;
        int current = 0;
        int longest = 0;

        if (c.moveToFirst()) {
            lastDate = c.getString(0);
            current = c.getInt(1);
            longest = c.getInt(2);
        }
        c.close();

        if (lastDate == null) {
            current = 1;
        } else if (lastDate.equals(today)) {
            return;
        } else if (isYesterday(lastDate)) {
            current++;
        } else {
            current = 1;
        }

        if (current > longest) longest = current;

        database.execSQL(
                "UPDATE user_progress SET last_active_date = ?, current_streak = ?, longest_streak = ? WHERE id = 1",
                new Object[]{today, current, longest}
        );
    }

    private boolean isYesterday(String date) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            java.time.LocalDate last = java.time.LocalDate.parse(date);
            return last.plusDays(1).equals(java.time.LocalDate.now());
        } else {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            try {
                java.util.Calendar lastCal = java.util.Calendar.getInstance();
                lastCal.setTime(sdf.parse(date));

                java.util.Calendar todayCal = java.util.Calendar.getInstance();
                todayCal.add(java.util.Calendar.DAY_OF_YEAR, -1);

                return lastCal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR)
                        && lastCal.get(java.util.Calendar.DAY_OF_YEAR) == todayCal.get(java.util.Calendar.DAY_OF_YEAR);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

    }

    private void showStreak() {
        Cursor c = database.rawQuery(
                "SELECT current_streak, longest_streak FROM user_progress WHERE id = 1",
                null
        );

        int current = 0;
        int longest = 0;

        if (c.moveToFirst()) {
            current = c.getInt(0);
            longest = c.getInt(1);
        }
        c.close();

        binding.tvCurrentStreak.setText("🔥 " + current + " day streak");
        binding.tvLongestStreak.setText("Longest: " + longest);
    }

}