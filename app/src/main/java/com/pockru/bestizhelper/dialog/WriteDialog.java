package com.pockru.bestizhelper.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.pockru.bestizhelper.BestizBoxMainListActivity;
import com.pockru.bestizhelper.R;
import com.pockru.bestizhelper.data.ImageData;
import com.pockru.bestizhelper.tumblr.TumblrOAuthActivity;
import com.pockru.preference.Preference;
import com.pockru.utils.Utils;

import java.util.ArrayList;

/**
 * Created by 래형 on 2015-12-12.
 */
public class WriteDialog extends AlertDialog {

    private ArrayList<ImageData> imgList;

    private EditText subject;
    private EditText contents;
    private HorizontalScrollView hsvImage;
    private LinearLayout containerImg;

    private Activity activity;

    public WriteDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        activity = (Activity) context;
        init();
    }

    public WriteDialog(Context context) {
        super(context);
        activity =  (Activity) context;
        init();
    }

    private void init() {
        // 타이틀 셋팅
        setTitle(R.string.menu_write);

        // 이미지 리스트 셋팅
        imgList = new ArrayList<>();

        // 기본 레이아웃 셋팅
        View view = View.inflate(getContext(), R.layout.layout_write, null);
        subject = (EditText) view.findViewById(R.id.editText_subject);
        contents = (EditText) view.findViewById(R.id.editText_contents);

        hsvImage = (HorizontalScrollView) view.findViewById(R.id.hsvImage);
        containerImg = (LinearLayout) view.findViewById(R.id.containerImg);

        Button btnImgAdd = (Button) view.findViewById(R.id.button_img_add);
        btnImgAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(Preference.getTumblrToken(activity.getApplicationContext()))
                        || TextUtils.isEmpty(Preference.getTumblrSecret(activity.getApplicationContext()))) {
                    activity.startActivityForResult(new Intent(activity, TumblrOAuthActivity.class), BestizBoxMainListActivity.REQ_CODE_TUMBLR_AUTH);
                } else {
                    if (Utils.isOverCurrentAndroidVersion(Build.VERSION_CODES.KITKAT) >= 0) {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        activity.startActivityForResult(intent, BestizBoxMainListActivity.REQ_CODE_GET_PHOTO);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        activity.startActivityForResult(intent, BestizBoxMainListActivity.REQ_CODE_GET_PHOTO);
                    }
                }
            }
        });

        setView(view);
    }

    public String getTitle(){
        return subject != null ? subject.getText().toString() : "";
    }

    public String getTotalContents() {
        String tmp = "";

        for (int i = 0; i < imgList.size(); i++) {
            if (imgList.get(i).is1024over) {
                tmp += "<img src=\"" + imgList.get(i).imgUrl + "\"" + " width=\"1024\"><br><br>";
            } else {
                tmp += "<img src=\"" + imgList.get(i).imgUrl + "\"><br><br>";
            }
        }

        String totalContents = (!tmp.equals("")) ? tmp + contents.getText().toString() : contents.getText().toString();
        return totalContents;
    }

    public void addImageToContainer(final String imgUrl) {
        hsvImage.setVisibility(View.VISIBLE);

        final ImageView iv = new ImageView(getContext());
        iv.setAdjustViewBounds(true);

        int size = (int) getContext().getResources().getDimension(R.dimen.img_default_size);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size, size);
        iv.setLayoutParams(params);
        iv.setPadding(5, 0, 5, 0);

        if (!TextUtils.isEmpty(imgUrl)) {
            ImageData data = new ImageData(imgUrl, false);
            imgList.add(data);
        }

        Glide.with(activity).load(imgUrl).into(iv);
        containerImg.addView(iv);
    }

    public void clearImgList(){
        imgList.clear();
    }

    @Override
    public void dismiss() {
        subject.setText("");
        contents.setText("");
        imgList.clear();
        super.dismiss();
    }
}
