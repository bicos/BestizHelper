package com.pockru.bestizhelper.data;

import java.util.ArrayList;

public class ArticleDetailData {

	private String userName;
	private String atcHit;
	private String userHomepage;
	private String atcSubject;
	private String atcContents;
	private String atcRealContents;
	private String googleAd;
	private String modifyUrl;
	private String deleteUrl;
	private int commentCnt;
	private int articleType;

	private ArrayList<CommentUserData> commentUserList = new ArrayList<CommentUserData>();
	private String articleHeader;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAtcHit() {
		return atcHit;
	}

	public void setAtcHit(String atcHit) {
		this.atcHit = atcHit;
	}

	public String getUserHomepage() {
		return userHomepage;
	}

	public void setUserHomepage(String userHomepage) {
		this.userHomepage = userHomepage;
	}

	public String getAtcSubject() {
		return atcSubject;
	}

	public void setAtcSubject(String atcSubject) {
		this.atcSubject = atcSubject;
	}

	public String getAtcContents() {
		return atcContents;
	}

	public void setAtcContents(String atcContents) {
		this.atcContents = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>"
				+ getArticleHeader()
				+ "<meta name=\"viewport\" content=\"user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, width=device-width\" />"
				+ "</head><body>" + atcContents.replaceAll("\\+", " ") + "</body></html>";

	}

	public String getGoogleAd() {
		return googleAd;
	}

	public void setGoogleAd(String googleAd) {
		this.googleAd = googleAd;
	}

	public ArrayList<CommentUserData> getCommentUserList() {
		return commentUserList;
	}

	public void setCommentUserList(ArrayList<CommentUserData> commentUserList) {
		this.commentUserList = commentUserList;
	}

	public String getModifyUrl() {
		return modifyUrl;
	}

	public void setModifyUrl(String modifyUrl) {
		this.modifyUrl = modifyUrl;
	}

	public String getDeleteUrl() {
		return deleteUrl;
	}

	public void setDeleteUrl(String deleteUrl) {
		this.deleteUrl = deleteUrl;
	}

	public String getAtcRealContents() {
		return atcRealContents;
	}

	public void setAtcRealContents(String atcRealContents) {
		this.atcRealContents = atcRealContents;
	}

	public int getCommentCnt() {
		return commentCnt;
	}

	public void setCommentCnt(int commentCnt) {
		this.commentCnt = commentCnt;
	}

	public int getArticleType() {
		return articleType;
	}

	public void setArticleType(int articleType) {
		this.articleType = articleType;
	}

	public void setArticleHeader(String articleHeader) {
		this.articleHeader = articleHeader;
	}

	public String getArticleHeader() {
		return articleHeader;
	}
}
