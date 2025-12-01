package com.aleynasahin.flashword;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aleynasahin.flashword.databinding.ActivityWordListBinding;

import java.util.ArrayList;

public class WordList extends AppCompatActivity {

    private ActivityWordListBinding binding;
    ArrayList<Word> wordArrayList;
    SQLiteDatabase database;
    WordAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityWordListBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        wordArrayList=new ArrayList<>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new WordAdapter(wordArrayList);
        binding.recyclerView.setAdapter(adapter);

        database=this.openOrCreateDatabase("Words",MODE_PRIVATE,null);
        database.execSQL("CREATE TABLE IF NOT EXISTS words (id INTEGER PRIMARY KEY, word VARCHAR, meaning VARCHAR)");

        getData();




    }
    @SuppressLint("NotifyDataSetChanged")
    private void getData(){
        Cursor cursor=database.rawQuery("SELECT * FROM words",null);

        int idIndex=cursor.getColumnIndex("id");
        int wordIndex=cursor.getColumnIndex("word");
        int meaningIndex=cursor.getColumnIndex("meaning");

        while (cursor.moveToNext()){

            int id=cursor.getInt(idIndex);
            String word=cursor.getString(wordIndex).trim();
            String meaning=cursor.getString(meaningIndex).trim();

            Word word1=new Word(id,word,meaning);
            wordArrayList.add(word1);

        }

        adapter.notifyDataSetChanged();
        cursor.close();


    }
    @SuppressLint("NotifyDataSetChanged")
    public void clear(View view) {
        database.execSQL("DELETE FROM words");
        wordArrayList.clear();
        adapter.notifyDataSetChanged();
        Toast.makeText(this,"All Words Deleted",Toast.LENGTH_SHORT).show();

    }
}