package com.pockru.bestizhelper.data;

import android.text.TextUtils;

import java.io.Serializable;

public class ArticleData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4267991639351428243L;
	
	String atcNum;
	String atcTitle;
	String atcUser;
	String atcDate;
	String atcHit;
	String atcVote;
	String atcComment;
	String atcLink;

	public ArticleData() {

	}

	public ArticleData(String atcNum, String atcTitle, String atcUser, String atcDate, String atcHit, String atcVote) {
		super();
		this.atcNum = atcNum;
		this.atcTitle = atcTitle;
		this.atcUser = atcUser;
		this.atcDate = atcDate;
		this.atcHit = atcHit;
		this.atcVote = atcVote;
	}

	public String getAtcNum() {
		return atcNum;
	}

	public void setAtcNum(String atcNum) {
		this.atcNum = atcNum;
	}

	public String getAtcTitle() {
		return atcTitle;
	}
	
//	public String getRealAtcTitle(){
//		String realTitle = "";
//		if (TextUtils.isEmpty(atcTitle) == false) {
//			try{
//				if (atcTitle.matches(".*[0-9].*")) {
//					realTitle = atcTitle.substring(0, atcTitle.indexOf("[")).trim();									
//				} else {
//					realTitle = atcTitle.trim();
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return realTitle;
//	}

	public void setAtcTitle(String atcTitle) {
		this.atcTitle = atcTitle;
	}

	public String getAtcUser() {
		return atcUser;
	}

	public void setAtcUser(String atcUser) {
		this.atcUser = atcUser;
	}

	public String getAtcDate() {
		return atcDate;
	}

	public void setAtcDate(String atcDate) {
		this.atcDate = atcDate;
	}

	public String getAtcHit() {
		return atcHit;
	}

	public void setAtcHit(String atcHit) {
		this.atcHit = atcHit;
	}

	public String getAtcVote() {
		return atcVote;
	}

	public void setAtcVote(String atcVote) {
		this.atcVote = atcVote;
	}

	public String getAtcComment() {
		return atcComment;
	}

	public void setAtcComment(String atcComment) {
		this.atcComment = atcComment;
	}

	public String getAtcLink() {
		return atcLink;
	}

	public void setAtcLink(String atcLink) {
		this.atcLink = atcLink;
		getArticleNumFromLink(atcLink);
	}

	private void getArticleNumFromLink(String atcLink) {
		String params = atcLink.substring(atcLink.indexOf("?"));
		if (params == null) {
			return;
		}
		
		String splitParams[] = params.split("&");
		String splitValues[];
		if (splitParams == null) {
			return;
		}
		
		for (int i = 0; i < splitParams.length; i++) {
			splitValues = splitParams[i].split("=");
			
			if (splitValues != null && splitValues.length == 2 && splitValues[0].equalsIgnoreCase("no")) {
				setAtcNum(splitValues[1]);
				break;
			}
		}
	}
	
	@Override
	public boolean equals(Object o) {
		
		if (o instanceof ArticleData) {
			ArticleData cmpData = (ArticleData) o;
			if (TextUtils.isEmpty(getAtcNum()) == false && TextUtils.isEmpty(cmpData.getAtcNum()) == false) {
				return getAtcNum().equals(cmpData.getAtcNum());
			}
		}
		
		return super.equals(o);
	}

	@Override
	public String toString() {
		return "ArticleData [atcNum=" + atcNum + ", atcTitle=" + atcTitle + ", atcUser=" + atcUser + ", atcDate=" + atcDate + ", atcHit=" + atcHit
				+ ", atcVote=" + atcVote + ", atcComment=" + atcComment + ", atcLink=" + atcLink + "]";
	}

}
