package com.noober.scrolltextlayout;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.leochuan.ScaleLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    int i = 0;
    RecyclerView recyclerView;
    List<String> list;
    Handler handler;

    MyAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(1);
            }
        });

        recyclerView = findViewById(R.id.rv);
//        recyclerView.setLayoutManager(new ScaleLayoutManager.Builder(this, 10).setOrientation(1).build());
        recyclerView.setLayoutManager(new MyLayoutManagerWithKolin());
        list = new ArrayList<>();
//        list.add("");
        list.add("1八卦神算子 今日收益 +99.99%");
        list.add("2八卦神算子 今日收益 +99.99%");
        list.add("3八卦神算子 今日收益 +99.99%");
        list.add("4八卦神算子 今日收益 +99.99%");
        list.add("5八卦神算子 今日收益 +99.99%");
        list.add("6八卦神算子 今日收益 +99.99%");
        list.add("7八卦神算子 今日收益 +99.99%");
        list.add("8八卦神算子 今日收益 +99.99%");
        list.add("9八卦神算子 今日收益 +99.99%");
        list.add("10八卦神算子 今日收益 +99.99%");
//        list.add("11八卦神算子 今日收益 +99.99%");
//        list.add("12八卦神算子 今日收益 +99.99%");
//        list.add("13八卦神算子 今日收益 +99.99%");
//        list.add("14八卦神算子 今日收益 +99.99%");
//        list.add("15八卦神算子 今日收益 +99.99%");
//        list.add("16八卦神算子 今日收益 +99.99%");
//        list.add("17八卦神算子 今日收益 +99.99%");
//        list.add("18八卦神算子 今日收益 +99.99%");
        adapter = new MyAdapter(list);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                recyclerView.getChild
            }
        });

        handler = new Handler();
        handler.postDelayed(runnable, 3000);



    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(i < list.size()){
                Log.e("MainActivity", i+"");
                recyclerView.smoothScrollToPosition(++ i);
                if(i == list.size() - 1){
                    handler.post(runnable);
                }else {
                    handler.postDelayed(runnable, 1000);
                }

            }else {
                i = 0;
                adapter.notifyDataSetChanged();
                handler.postDelayed(runnable, 1000);
            }
        }
    };

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
//            int index = position % 3;
            holder.mText.setText(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list.size();
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
