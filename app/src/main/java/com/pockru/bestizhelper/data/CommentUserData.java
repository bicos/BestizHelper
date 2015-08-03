package com.pockru.bestizhelper.data;

public class CommentUserData {

	private String userName;
	private String userComment;
	private String userAddress;
	private String deleteUrl;

	public CommentUserData() {

	}

	public CommentUserData(String userName, String userComment, String userAddress) {
		super();
		this.userName = userName;
		this.userComment = userComment;
		this.userAddress = userAddress;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserComment() {
		return userComment;
	}

	public void setUserComment(String userComment) {
		this.userComment = userComment;
	}

	public String getUserAddress() {
		return userAddress;
	}

	public void setUserAddress(String userAddress) {
		this.userAddress = userAddress;
	}

	public String getDeleteUrl() {
		return deleteUrl;
	}

	public void setDeleteUrl(String deleteUrl) {
		this.deleteUrl = deleteUrl;
	}

	@Override
	public String toString() {
		return "CommentUserData [userName=" + userName + ", userComment=" + userComment + ", userAddress=" + userAddress + ", deleteUrl=" + deleteUrl + "]";
	}

	
	
}
