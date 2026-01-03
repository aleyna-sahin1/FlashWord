package com.aleynasahin.flashword;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aleynasahin.flashword.databinding.ActivityEditWordBinding;

public class EditWordActivity extends AppCompatActivity {

    ActivityEditWordBinding binding;
    SQLiteDatabase database;
    int wordId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditWordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = this.openOrCreateDatabase("Words", MODE_PRIVATE, null);

        wordId = getIntent().getIntExtra("wordId", -1);
        String word = getIntent().getStringExtra("word");
        String meaning = getIntent().getStringExtra("meaning");

        binding.etWord.setText(word);
        binding.etMeaning.setText(meaning);

        binding.btnSave.setOnClickListener(v -> {
            String newWord = binding.etWord.getText().toString();
            String newMeaning = binding.etMeaning.getText().toString();

            database.execSQL(
                    "UPDATE words SET word = ?, meaning = ? WHERE id = ?",
                    new Object[]{newWord, newMeaning, wordId}
            );

            finish();
            Toast.makeText(this, "Word updated", Toast.LENGTH_LONG).show();

        });
    }
}
