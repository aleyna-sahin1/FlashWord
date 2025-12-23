package com.aleynasahin.flashword;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aleynasahin.flashword.databinding.ActivityStatisticsBinding;

public class StatisticsActivity extends AppCompatActivity {

    private ActivityStatisticsBinding binding;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityStatisticsBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = this.openOrCreateDatabase("Words", MODE_PRIVATE, null);

        loadStatistics();
        loadHardWords();

    }
    private void loadStatistics() {

        Cursor cursor = database.rawQuery(
                "SELECT SUM(correct_count) AS totalCorrect, SUM(wrong_count) AS totalWrong FROM words",
                null
        );

        int correct = 0;
        int wrong = 0;

        if (cursor.moveToFirst()) {
            correct = cursor.getInt(cursor.getColumnIndexOrThrow("totalCorrect"));
            wrong = cursor.getInt(cursor.getColumnIndexOrThrow("totalWrong"));
        }
        cursor.close();

        int total = correct + wrong;
        int rate = total == 0 ? 0 : (correct * 100 / total);

        // UI set
        binding.tvCorrectCount.setText(String.valueOf(correct));
        binding.tvWrongCount.setText(String.valueOf(wrong));
        binding.tvRatePercent.setText(rate + "%");
        binding.progressRate.setProgress(rate);

        setProgressColor(rate);
    }

    private void setProgressColor(int rate) {
        if (rate < 40) {
            binding.progressRate.getProgressDrawable()
                    .setColorFilter(getColor(android.R.color.holo_red_dark),
                            android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (rate < 70) {
            binding.progressRate.getProgressDrawable()
                    .setColorFilter(getColor(android.R.color.holo_orange_dark),
                            android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            binding.progressRate.getProgressDrawable()
                    .setColorFilter(getColor(android.R.color.holo_green_dark),
                            android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }
    private void loadHardWords() {

        binding.gridHardWords.removeAllViews();

        Cursor cursor = database.rawQuery(
                "SELECT word, wrong_count FROM words " +
                        "WHERE wrong_count > 0 " +
                        "ORDER BY wrong_count DESC " +
                        "LIMIT 4",
                null
        );

        while (cursor.moveToNext()) {
            String word = cursor.getString(0);
            int wrong = cursor.getInt(1);

            // Kart
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(24, 24, 24, 24);
            card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.height = 260; // 🔥 SABİT YÜKSEKLİK (çok önemli)
            params.setMargins(16, 16, 16, 16);
            card.setLayoutParams(params);

            // 🔹 ÜST BOŞLUK (ORTALAMA İÇİN)
            View spacerTop = new View(this);
            spacerTop.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0,
                            1f
                    )
            );

            // Kelime
            TextView tvWord = new TextView(this);
            tvWord.setText(word);
            tvWord.setTextSize(16f);
            tvWord.setTypeface(null, android.graphics.Typeface.BOLD);
            tvWord.setGravity(Gravity.CENTER);
            tvWord.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            // Wrong
            TextView tvWrong = new TextView(this);
            tvWrong.setText(wrong + " wrong");
            tvWrong.setTextSize(14f);
            tvWrong.setGravity(Gravity.CENTER);
            tvWrong.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            // 🔹 ALT BOŞLUK
            View spacerBottom = new View(this);
            spacerBottom.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0,
                            1f
                    )
            );

            card.addView(spacerTop);
            card.addView(tvWord);
            card.addView(tvWrong);
            card.addView(spacerBottom);

            binding.gridHardWords.addView(card);
        }

        cursor.close();
    }

}
