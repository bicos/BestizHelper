package com.pockru.bestizhelper.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.pockru.bestizhelper.R;

/**
 * Created by rhpark on 2015. 12. 22..
 * JIRA: MWP-
 */
public class NetworkImageView extends RelativeLayout{

    ImageView ivContainer;
    ImageButton btnDelele;

    public NetworkImageView(Context context) {
        super(context);
        init();
    }

    public NetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        inflate(getContext(), R.layout.view_network_img, this);

        ivContainer = (ImageView) findViewById(R.id.iv_content);
        btnDelele = (ImageButton) findViewById(R.id.btn_delete);
    }

    public void loadImage(String url) {
        Glide.with(getContext()).load(url).centerCrop().into(ivContainer);
    }

    public ImageView getIvContainer() {
        return ivContainer;
    }

    public ImageButton getBtnDelele() {
        return btnDelele;
    }
}
