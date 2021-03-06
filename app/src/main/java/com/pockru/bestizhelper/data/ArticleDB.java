package com.pockru.bestizhelper.data;

import android.net.Uri;
import android.text.TextUtils;

public class ArticleDB {

	public static final int TYPE_VIEW 		= 0x001;
	public static final int TYPE_WRITE 		= 0x010;
	public static final int TYPE_FAVORITE 	= 0x100;

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
		articleDB.articleType = ArticleDB.TYPE_VIEW;

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
		articleDB.articleType = articleDetailData.getArticleType();

		return articleDB;
	}

	private static int parseInt(String str){
		if (str == null || TextUtils.isEmpty(str)) {
			return 0;
		} else {
			return Integer.parseInt(str.replaceAll("[^0-9]", ""));
		}
	}

	@Override
	public String toString() {
		return "ArticleDB{" +
				"articleNum=" + articleNum +
				", articleTitle='" + articleTitle + '\'' +
				", articleUser='" + articleUser + '\'' +
				", articleDate='" + articleDate + '\'' +
				", articleHit=" + articleHit +
				", articleVote=" + articleVote +
				", articleComment=" + articleComment +
				", articleUserHomepage='" + articleUserHomepage + '\'' +
				", articleContents='" + articleContents + '\'' +
				", articleUrl='" + articleUrl + '\'' +
				", articleModifyUrl='" + articleModifyUrl + '\'' +
				", articleDeleteUrl='" + articleDeleteUrl + '\'' +
				", articleFavorite=" + articleFavorite +
				", articleType=" + articleType +
				'}';
	}
}
