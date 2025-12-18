package com.aleynasahin.flashword;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

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
}
