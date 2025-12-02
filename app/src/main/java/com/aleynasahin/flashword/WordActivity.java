package com.aleynasahin.flashword;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aleynasahin.flashword.databinding.ActivityWordBinding;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class WordActivity extends AppCompatActivity {

    private ActivityWordBinding binding;
    private boolean isFrontVisible = true;
    SQLiteDatabase database;
    ArrayList<Word> wordArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityWordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        wordArrayList=new ArrayList<>();

        binding.cardFront2.setOnClickListener(v -> {
            flipCard();
        });

        binding.cardBack2.setOnClickListener(v -> {
            flipCard();

        });
        database=this.openOrCreateDatabase("Words",MODE_PRIVATE,null);
        database.execSQL("CREATE TABLE IF NOT EXISTS words (id INTEGER PRIMARY KEY, word VARCHAR, meaning VARCHAR)");
        PressAnimListener listener = new PressAnimListener(this); // this = Activity context
        binding.btnSave.setOnTouchListener(listener);

    }

    public void save(View view){
        String word=binding.editTextAddWord.getText().toString().trim();
        String meaning=binding.editTextAddMeaning.getText().toString().trim();

        try{
            String sql="INSERT INTO words (word,meaning) VALUES (?,?)";
            SQLiteStatement statement=database.compileStatement(sql);
            statement.bindString(1,word);
            statement.bindString(2,meaning);
            statement.execute();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        binding.editTextAddWord.setText("");
        binding.editTextAddMeaning.setText("");

        Toast.makeText(this,"Word Added",Toast.LENGTH_SHORT).show();
        if(!isFrontVisible){
            flipCard();
        }

    }
    private void flipCard() {
        if (isFrontVisible) {
            animateCard(binding.cardFront2, binding.cardBack2);
        } else {
            animateCard(binding.cardBack2, binding.cardFront2);
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