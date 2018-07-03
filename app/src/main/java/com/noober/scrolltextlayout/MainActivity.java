package com.noober.scrolltextlayout;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leochuan.ScaleLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new ScaleLayoutManager.Builder(this, 10).setOrientation(1).build());
        List<String> list = new ArrayList<>();
        list.add("1八卦神算子 今日收益 +99.99%");
        list.add("2八卦神算子 今日收益 +99.99%");
        list.add("3八卦神算子 今日收益 +99.99%");
        list.add("4八卦神算子 今日收益 +99.99%");
        recyclerView.setAdapter(new MyAdapter(list));
    }


    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

        private List<String> list;

        public MyAdapter(List<String> list) {
            this.list = list;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            MyAdapter.ViewHolder viewHolder = new MyAdapter.ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
            int index = position % 3;
            holder.mText.setText(list.get(index));
        }

        @Override
        public int getItemCount() {
            return Integer.MAX_VALUE;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView mText;
            ViewHolder(View itemView) {
                super(itemView);
                mText = itemView.findViewById(R.id.tv);
            }
        }

    }
}
