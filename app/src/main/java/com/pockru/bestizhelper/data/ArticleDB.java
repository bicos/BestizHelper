package com.pockru.bestizhelper.data;

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
	
	public static ArticleDB createInstance(ArticleData articleData, ArticleDetailData articleDetailData){
		ArticleDB articleDB = new ArticleDB();
		
		if (articleData == null || articleDetailData == null) {
			return articleDB;
		}
		
		articleDB.articleNum = Integer.parseInt(articleData.atcNum);
		articleDB.articleUrl = articleData.atcLink;
		articleDB.articleTitle = articleData.atcTitle;
		articleDB.articleUser = articleData.atcUser;
		articleDB.articleDate = articleData.atcDate;
		articleDB.articleHit = Integer.parseInt(articleData.atcHit);
		articleDB.articleVote = Integer.parseInt(articleData.atcVote == null ? "0" : articleData.atcVote.replace("+", ""));
		articleDB.articleComment = Integer.parseInt(articleData.atcComment == null ? "0" : articleData.atcComment);
		articleDB.articleUserHomepage = articleDetailData.getUserHomepage();
		articleDB.articleContents = articleDetailData.getAtcContents();
		articleDB.articleModifyUrl = articleDetailData.getModifyUrl();
		articleDB.articleDeleteUrl = articleDetailData.getDeleteUrl();
		
		return articleDB;
	}
	
}
