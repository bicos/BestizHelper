package com.pockru.bestizhelper.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.pockru.bestizhelper.BaseActivity;
import com.pockru.bestizhelper.R;
import com.pockru.bestizhelper.data.ImageData;
import com.pockru.bestizhelper.tumblr.TumblrOAuthActivity;
import com.pockru.bestizhelper.view.NetworkImageView;
import com.pockru.network.BestizParamsUtil;
import com.pockru.network.BestizUrlUtil;
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

    private String host;
    private String boardId;
    private String articleNo = "";

    private Context context;
    private BaseActivity.CommentAlarmAddProvider provider;
    
    public WriteDialog(Context context, String host, String boardId) {
        this(context, host, boardId, "");
    }

    public WriteDialog(Context context, String host, String boardId, String articleNo) {
        super(context);
        if (context instanceof BaseActivity.CommentAlarmAddProvider) {
            this.provider = (BaseActivity.CommentAlarmAddProvider) context;
        }
        this.context = context;
        this.host = host;
        this.boardId = boardId;
        this.articleNo = articleNo;
        init();
    }

    private void init() {
        // 타이틀 셋팅
        setTitle(R.string.menu_write);

        // 이미지 리스트 셋팅
        imgList = new ArrayList<>();

        // 버튼 셋팅
        setButton(WriteDialog.BUTTON_POSITIVE, "확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TextUtils.isEmpty(host) && !TextUtils.isEmpty(boardId)) {
                    if (taskUploadImg != null && taskUploadImg.startImgUpload) {
                        Utils.showAlternateAlertDialog(context, context.getString(R.string.menu_write),
                                context.getString(R.string.alert_msg_still_img_upload), new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == DialogInterface.BUTTON_POSITIVE) {
                                            ((BaseActivity)context).
                                                    requestNetwork(BaseActivity.FLAG_REQ_WRITE,
                                                            BestizUrlUtil.createArticleWriteUrl(host),
                                                            BestizParamsUtil.createWriteParams(boardId,getTitle(), getTotalContents(), articleNo));
                                        }
                                    }
                                });

                    } else {

                        ((BaseActivity)context).
                                requestNetwork(BaseActivity.FLAG_REQ_WRITE,
                                        BestizUrlUtil.createArticleWriteUrl(host),
                                        BestizParamsUtil.createWriteParams(boardId, getTitle(), getTotalContents(), articleNo));
                    }

                    clearImgList();
                }
            }
        });
        setButton(WriteDialog.BUTTON_NEGATIVE, "취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

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
                if (TextUtils.isEmpty(Preference.getTumblrToken(context.getApplicationContext()))
                        || TextUtils.isEmpty(Preference.getTumblrSecret(context.getApplicationContext()))) {
                    ((Activity)context).startActivityForResult(new Intent(context, TumblrOAuthActivity.class), BaseActivity.REQ_CODE_TUMBLR_AUTH);
                } else {
                    if (Utils.isOverCurrentAndroidVersion(Build.VERSION_CODES.KITKAT) >= 0) {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        ((Activity)context).startActivityForResult(intent, BaseActivity.REQ_CODE_GET_PHOTO);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        ((Activity)context).startActivityForResult(intent, BaseActivity.REQ_CODE_GET_PHOTO);
                    }
                }
            }
        });

        setView(view);
    }

    public String getTitle() {
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

        final NetworkImageView iv = new NetworkImageView(context);

        if (!TextUtils.isEmpty(imgUrl)) {
            final ImageData data = new ImageData(imgUrl, false);
            imgList.add(data);
            iv.loadImage(imgUrl);
            iv.getBtnDelele().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imgList.remove(data);
                    containerImg.removeView(iv);
                }
            });
            containerImg.addView(iv);
        }
    }

    public void clearImgList() {
        imgList.clear();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    public void clearData(){
        hsvImage.removeAllViews();
        subject.setText("");
        contents.setText("");
        imgList.clear();
    }

    /**
     * 액티비티 리절트 처리
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BaseActivity.REQ_CODE_GET_PHOTO:
                if (isShowing()) {
                    uploadPictures(Preference.getTumblrToken(context.getApplicationContext()),
                            Preference.getTumblrSecret(context.getApplicationContext()),
                            data.getData());
                }
                break;
            case BaseActivity.REQ_CODE_TUMBLR_AUTH:
                if (Utils.isOverCurrentAndroidVersion(Build.VERSION_CODES.KITKAT) >= 0) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    ((Activity)context).startActivityForResult(intent, BaseActivity.REQ_CODE_GET_PHOTO);
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    ((Activity)context).startActivityForResult(intent, BaseActivity.REQ_CODE_GET_PHOTO);
                }
                break;
        }
    }

    private BaseActivity.TumblrImgUpload taskUploadImg;

    private void uploadPictures(String token, String secret, Uri uploadUri) {
        String path = Utils.getRealPathFromURI(uploadUri, context);
        taskUploadImg = new BaseActivity.TumblrImgUpload(context, this);
        taskUploadImg.execute(token, secret, path);
    }

    public void setWriteTitle(String title) {
        if (subject != null) {
            subject.setText(title);
        }
    }

    public void setWriteBody(String body) {
        if (body != null) {
            if (body.contains("<br>")) {
                body = body.replace("<br>", "\n");
            }

            if (body.contains("<br/>")) {
                body = body.replace("<br/>", "\n");
            }

            contents.setText(body);
        }
    }
}
