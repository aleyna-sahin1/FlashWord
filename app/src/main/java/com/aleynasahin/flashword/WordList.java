package com.aleynasahin.flashword;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aleynasahin.flashword.databinding.ActivityWordListBinding;
import com.google.android.material.snackbar.Snackbar;

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
        binding = ActivityWordListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        wordArrayList = new ArrayList<>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WordAdapter(wordArrayList);
        binding.recyclerView.setAdapter(adapter);

        database = this.openOrCreateDatabase("Words", MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS words (id INTEGER PRIMARY KEY, word VARCHAR, meaning VARCHAR)");

        getData();
        binding.btnClear.setOnClickListener(v -> {
            showClearConfirmationDialog();
        });



        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                        int position = viewHolder.getBindingAdapterPosition();
                        Word deletedWord = wordArrayList.get(position);

                        wordArrayList.remove(position);
                        adapter.notifyItemRemoved(position);

                        Snackbar.make(binding.recyclerView,
                                        "Word deleted",
                                        Snackbar.LENGTH_LONG)
                                .setAction("UNDO", v -> {

                                    wordArrayList.add(position, deletedWord);
                                    adapter.notifyItemInserted(position);

                                })
                                .addCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar snackbar, int event) {

                                        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                            deleteWordFromDatabase(deletedWord.id_);
                                        }
                                    }
                                })
                                .show();
                    }
                };

        new ItemTouchHelper(simpleCallback)
                .attachToRecyclerView(binding.recyclerView);


    }
    private void showClearConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear all?")
                .setMessage("All data will be deleted. Are you sure?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    clear();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }



    private void deleteWordFromDatabase(int wordId) {
        database.execSQL(
                "DELETE FROM words WHERE id = ?",
                new String[]{String.valueOf(wordId)}
        );
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getData() {
        Cursor cursor = database.rawQuery("SELECT * FROM words", null);

        int idIndex = cursor.getColumnIndex("id");
        int wordIndex = cursor.getColumnIndex("word");
        int meaningIndex = cursor.getColumnIndex("meaning");

        while (cursor.moveToNext()) {

            int id = cursor.getInt(idIndex);
            String word = cursor.getString(wordIndex).trim();
            String meaning = cursor.getString(meaningIndex).trim();

            Word word1 = new Word(id, word, meaning);
            wordArrayList.add(word1);

        }

        cursor.close();
        adapter.notifyDataSetChanged();


        updateEmptyState();

    }

    private void updateEmptyState() {
        if (wordArrayList.isEmpty()) {
            binding.recyclerView.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.btnClear.setVisibility(View.GONE);
        } else {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.btnClear.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        database.execSQL("DELETE FROM words");
        wordArrayList.clear();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "All Words Deleted", Toast.LENGTH_SHORT).show();
        updateEmptyState();

    }
}