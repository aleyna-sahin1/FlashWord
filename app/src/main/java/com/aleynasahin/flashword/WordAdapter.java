package com.aleynasahin.flashword;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

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

        RecyclerRowBinding recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new WordHolder(recyclerRowBinding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull WordHolder holder, int position) {

        holder.binding.recyclerView.setText(wordArrayList.get(position).word_ +"-"+ wordArrayList.get(position).meaning_);

    }

    @Override
    public int getItemCount() {
        return wordArrayList.size();
    }

    public class WordHolder extends RecyclerView.ViewHolder{

        private RecyclerRowBinding binding;
        public WordHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding=binding;

        }
    }
}
