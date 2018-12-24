package example.loginsystem.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import example.loginsystem.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewFragment extends Fragment {

    View contentView;
    public ReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        contentView = inflater.inflate(R.layout.fragment_review, container, false);


        Log.d("测试", "ReviewFragment 创建");

        return contentView;
    }


    @Override
    public void onStart() {
//        Log.d("测试", "ReviewFragment start");
        super.onStart();
    }
}
