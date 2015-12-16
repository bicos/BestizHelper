package com.pockru.bestizhelper.data;

import android.net.Uri;
import android.text.TextUtils;

public class ArticleDB {

	public static final int TYPE_VIEW 	= 0;
	public static final int TYPE_WRITE 	= 1;

	public int articleNum;
	public String articleTitle;
	public String articleUser;
	public String articleDate;
	public int articleHit;
	public int articleVote;
	public int articleComment;
	public String articleUserHomepage;
	public String articleContents;
	public String articleUrl;
	public String articleModifyUrl;
	public String articleDeleteUrl;
	public int articleFavorite;
	public int articleType;

	public static ArticleDB createInstance(ArticleData data , String articleUrl){
		if (data == null || TextUtils.isEmpty(articleUrl)) {
			return null;
		}

		Uri uri = Uri.parse(articleUrl);

		ArticleDB articleDB = new ArticleDB();
		articleDB.articleNum = parseInt(uri.getQueryParameter("no"));
		articleDB.articleUrl = articleUrl;
		articleDB.articleTitle = data.atcTitle;
		articleDB.articleUser = data.atcUser;
		articleDB.articleDate = data.atcDate;
		articleDB.articleHit = parseInt(data.atcHit);
		articleDB.articleVote = parseInt(data.atcVote);
		articleDB.articleComment = parseInt(data.atcComment);
		articleDB.articleType = ArticleDB.TYPE_WRITE;

		return articleDB;
	}

	public static ArticleDB createInstance(ArticleDetailData articleDetailData, String articleUrl, boolean isWriteArticle){
		if (articleDetailData == null || TextUtils.isEmpty(articleUrl)) {
			return null;
		}

		Uri uri = Uri.parse(articleUrl);

		ArticleDB articleDB = new ArticleDB();

		articleDB.articleNum = Integer.parseInt(uri.getQueryParameter("no"));
		articleDB.articleUrl = articleUrl;
		articleDB.articleTitle = articleDetailData.getAtcSubject();
		articleDB.articleUser = articleDetailData.getUserName();
		articleDB.articleDate = "";
		articleDB.articleHit = Integer.parseInt(articleDetailData.getAtcHit().replaceAll("[^0-9]", ""));
		articleDB.articleVote = 0;
		articleDB.articleComment = articleDetailData.getCommentCnt();
		articleDB.articleUserHomepage = articleDetailData.getUserHomepage();
		articleDB.articleContents = articleDetailData.getAtcContents();
		articleDB.articleModifyUrl = articleDetailData.getModifyUrl();
		articleDB.articleDeleteUrl = articleDetailData.getDeleteUrl();
		articleDB.articleType = isWriteArticle ? ArticleDB.TYPE_WRITE : ArticleDB.TYPE_VIEW;

		return articleDB;
	}

	private static int parseInt(String str){
		if (str == null || TextUtils.isEmpty(str)) {
			return 0;
		} else {
			return Integer.parseInt(str.replaceAll("[^0-9]", ""));
		}
	}
}
