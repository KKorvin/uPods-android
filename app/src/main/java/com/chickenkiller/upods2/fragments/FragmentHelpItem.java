package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chickenkiller.upods2.R;

import java.util.Calendar;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class FragmentHelpItem extends Fragment {

    public static final String TAG;

    static {
        long time = Calendar.getInstance().get(Calendar.MILLISECOND);
        TAG = "f_help_item" + String.valueOf(time);
    }

    private ImageView imgHelpTip;
    private Button btnCloseHelp;
    private View.OnClickListener closeClickListener;

    private int index;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_item, container, false);
        imgHelpTip = (ImageView) view.findViewById(R.id.imgHelpTip);
        btnCloseHelp = (Button) view.findViewById(R.id.btnCloseHelp);
        if (closeClickListener != null) {
            btnCloseHelp.setVisibility(View.VISIBLE);
            btnCloseHelp.setOnClickListener(closeClickListener);
        } else {
            btnCloseHelp.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCurrentTipImage();
    }


    private void setCurrentTipImage() {
        switch (index) {
            case 0:
                imgHelpTip.setImageResource(R.drawable.tw__share_email_header);
                break;
            case 1:
                imgHelpTip.setImageResource(R.drawable.tw__share_email_header);
                break;
            case 2:
                imgHelpTip.setImageResource(R.drawable.tw__share_email_header);
                break;
            case 3:
                imgHelpTip.setImageResource(R.drawable.tw__share_email_header);
                break;
        }
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setCloseClickListener(View.OnClickListener closeClickListener) {
        this.closeClickListener = closeClickListener;
    }
}
