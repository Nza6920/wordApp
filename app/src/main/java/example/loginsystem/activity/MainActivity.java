package example.loginsystem.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import example.loginsystem.R;
import example.loginsystem.fragment.HomeFragment;
import example.loginsystem.fragment.ReviewFragment;
import example.loginsystem.my.config.Global;
import example.loginsystem.untils.TabBarAdapter;

import static example.loginsystem.my.config.Config.RELANDING;

public class MainActivity extends AppCompatActivity {

    TabBarAdapter tabBarAdapter;    // 标签栏
    Fragment[] pages;               // 存储每一页
    GridView gridView;              // 标签栏

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridView = (GridView) findViewById(R.id.id_gridView);                           // 标签栏
        final ViewPager viewPager = (ViewPager) findViewById(R.id.id_viewPager);        // viewPager

        // 初始化标签栏
        initTabBar(gridView);

        // 初始化ViewPager
        pages = new Fragment[2];          // 赋值时注意要与gridView对应
        pages[0] = new HomeFragment();
        pages[1] = new ReviewFragment();
        FragmentPagerAdapter pagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        TextView textView = (TextView) findViewById(R.id.id_textView_user);
        textView.setText("欢迎, " + Global.i.user.getName() + ".");

        // 监听器: 当滑动切换时, 设置对应的标签高亮
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                // 让gridView切换到对应的选项
                tabBarAdapter.setActive(position, true);
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // 监听器: 当点击标签页时, 显示对应页
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewPager.setCurrentItem(position);
                tabBarAdapter.setActive(position, true);
            }
        });

    }

    // 初始化标签栏
    public void initTabBar(GridView gridView) {
        // 标签栏内容
        TabBarAdapter.Item[] labels = new TabBarAdapter.Item[2];
        labels[0] = new TabBarAdapter.Item("主页", "home");
        labels[0].iconNormal = getDrawable(R.drawable.im_home_normal);
        labels[0].iconActive = getDrawable(R.drawable.im_home_active);
        labels[1] = new TabBarAdapter.Item("复习", "review");
        labels[1].iconNormal = getDrawable(R.drawable.im_review_normal);
        labels[1].iconActive = getDrawable(R.drawable.im_review_active);
        tabBarAdapter = new TabBarAdapter(this);
        tabBarAdapter.addItems(labels);

        gridView.setAdapter(tabBarAdapter);
        gridView.setNumColumns( labels.length );   // 设置 gridView 数量
        tabBarAdapter.setActive(0, true);          // 默认选中第一项
    }

    // Fragment 适配器
    private class MyViewPagerAdapter extends FragmentPagerAdapter {

        public MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // 得到特定的一项
        @Override
        public Fragment getItem(int position) {
            return pages[position];
        }

        // 一共有几页
        @Override
        public int getCount() {
            return pages.length;
        }
    }

    // 我的设置
    public void option(View view)
    {
        Intent intent = new Intent(this, OptionActivity.class);
        startActivityForResult(intent, RELANDING);
    }

    // 我的词库
    public void myLibrary(View view)
    {
        Intent intent = new Intent(this, MyLibraryActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RELANDING:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(this, UserLoginActivity.class);
                    intent.putExtra("result", data.getStringExtra("result"));
                    startActivity(intent);

                    finish();
                }
                break;
            default:
                break;
        }
    }

    // 换本书背
    public void selectLibrary(View view)
    {
        Intent intent = new Intent(this, MyLibraryActivity.class);
        startActivity(intent);
    }

    // 开始练习
    public void startPractice(View view) {
        Intent intent = new Intent(this, PracticeActivity.class);
        startActivity(intent);
    }

    // 中文选词
    public void reviewByCN(View view)
    {
        Intent intent = new Intent(this, ReviewByCNActivity.class);
        startActivity(intent);
    }

    // 英文选词
    public void reviewByEN(View view)
    {
        Intent intent = new Intent(this, ReviewByENActivity.class);
        startActivity(intent);
    }

    // 单词速测
    public void wordTest(View view)
    {
        Intent intent = new Intent(this, WordTestActivity.class);
        startActivity(intent);
    }

    // 生词本
    public void unskillNote(View view)
    {
        Intent intent = new Intent(this, UnskillWordActivity.class);
        startActivity(intent);
    }
}
