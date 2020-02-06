package com.example.booklistingapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    //vars
    private Context mCtx;
    private ArrayList<Book> mBookList;

    public CustomAdapter(Context mCtx, ArrayList<Book> mBookList) {
        this.mCtx = mCtx;
        this.mBookList = mBookList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_book ,
                parent , false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Book book = mBookList.get(position);
        holder.title.setText(book.getTitle());
        holder.author.setText(book.getAuthor());
        holder.desc.setText(book.getDescription());

        holder.buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Info link
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(book.getInfoLink()));
                mCtx.startActivity(i);
            }
        });

        holder.preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //to download link
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(book.getDownloadLink()));
                mCtx.startActivity(i);
            }
        });

        //set the imageView
        /*
        try {
            URL url = new URL(book.getImageLink());
            Bitmap btm = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            holder.thumbnail.setImageBitmap(btm);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

         */


        Picasso.get().load(book.getImageLink()).
                resize(90,120).
                centerCrop().into(holder.thumbnail);


    }

    @Override
    public int getItemCount() {
        return mBookList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //widgets
        private TextView title , author, desc;
        private Button buy,preview;
        private ImageView thumbnail;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            author = itemView.findViewById(R.id.author);
            desc = itemView.findViewById(R.id.des);
            buy = itemView.findViewById(R.id.buy);
            preview = itemView.findViewById(R.id.preview);
            thumbnail = itemView.findViewById(R.id.img);

        }
    }
}
