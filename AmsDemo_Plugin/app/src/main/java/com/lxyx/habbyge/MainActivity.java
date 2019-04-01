package com.lxyx.habbyge;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by habbyge 2019/3/5.
 */
public class MainActivity extends AppCompatActivity {
//    private boolean mShowFragment1Now = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        findViewById(R.id.textView1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("HABBYGE-MALI, textView1 onClick");
                Intent intent = new Intent(MainActivity.this, TestActivity.class);
                int y = 100; // 这些都是测试用的，随便添加的无用代码！！！
                /*System.out.println("y: " + y);*/
                startActivity(intent); // 测试startActivity注入
            }
        });

        findViewById(R.id.textView2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = MainActivity.this.getSupportFragmentManager();
                if (fm == null) {
                    return;
                }
                FragmentTransaction transaction = fm.beginTransaction();
                Fragment fragment;
//                if (mShowFragment1Now) {
//                    mShowFragment1Now = false;
//                    fragment = new Test2Fragment();
//                    transaction.replace(R.id.fragment, fragment);
//                } else {
//                    mShowFragment1Now = true;
                    fragment = new Test2Fragment();
                    transaction.replace(R.id.fragment, fragment);
//                }
                transaction.commit();
            }
        });

        initListView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("MainActivity onResume-1");
    }

    private void initListView() {
        final ListView listView = findViewById(R.id.listview);
        List<String> srcList = new ArrayList<>();
        srcList.add("Mali");
        srcList.add("Habbyge");
        srcList.add("GeYi");
        srcList.add("GeXiao");
        listView.setAdapter(new TestAdapter(srcList));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("HABBYGE-MALI, onItemClick: " + position);

                Toast.makeText(MainActivity.this,
                        "onItemClick: " + position,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private class TestAdapter extends BaseAdapter {
        private List<String> mSrcList;

        TestAdapter(List<String> srcList) {
            super();
            mSrcList = srcList;
        }

        @Override
        public int getCount() {
            return mSrcList == null ? 0 : mSrcList.size();
        }

        @Override
        public Object getItem(int position) {
            return mSrcList == null ? null : mSrcList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String name = (String) getItem(position);
            View view;
            ViewHolder viewHolder;

            if (convertView == null) {
                view = LayoutInflater.from(MainActivity.this).inflate(R.layout.test_layout, null);
                viewHolder = new ViewHolder();
                viewHolder.nameTv = view.findViewById(R.id.listview_item);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.nameTv.setText(name);

            return view;
        }
    }

    private class ViewHolder {
        TextView nameTv;
    }
}
