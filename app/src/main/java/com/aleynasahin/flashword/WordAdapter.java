package com.aleynasahin.flashword;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aleynasahin.flashword.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordHolder> {

    ArrayList<Word> wordArrayList;

    public WordAdapter(ArrayList<Word> wordArrayList) {
        this.wordArrayList = wordArrayList;
    }

    @NonNull
    @Override
    public WordHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new WordHolder(recyclerRowBinding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull WordHolder holder, int position) {

        holder.binding.tvWord.setText(wordArrayList.get(position).word_);
        holder.binding.tvMeaning.setText(wordArrayList.get(position).meaning_);

        holder.itemView.setOnLongClickListener(view -> {

            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.getMenuInflater()
                    .inflate(R.menu.word_item_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(menuItem -> {

                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) return false;

                Word selectedWord = wordArrayList.get(adapterPosition);

                if (menuItem.getItemId() == R.id.action_delete) {

                    wordArrayList.remove(adapterPosition);
                    notifyItemRemoved(adapterPosition);
                    return true;

                } else if (menuItem.getItemId() == R.id.action_edit) {

                    Intent intent = new Intent(
                            holder.itemView.getContext(),
                            EditWordActivity.class
                    );

                    intent.putExtra("wordId", selectedWord.id_);
                    intent.putExtra("word", selectedWord.word_);
                    intent.putExtra("meaning", selectedWord.meaning_);

                    holder.itemView.getContext().startActivity(intent);
                    return true;
                }

                return false;
            });

            popupMenu.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return wordArrayList.size();
    }

    public class WordHolder extends RecyclerView.ViewHolder {

        private RecyclerRowBinding binding;

        public WordHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }
}
