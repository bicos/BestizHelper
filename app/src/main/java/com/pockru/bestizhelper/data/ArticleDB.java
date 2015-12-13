package com.pockru.bestizhelper.data;

import android.net.Uri;
import android.text.TextUtils;

public class ArticleDB {
	
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

		return articleDB;
	}

	public static ArticleDB createInstance(ArticleDetailData articleDetailData, String articleUrl){
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
