package com.pockru.bestizhelper.data;

public class UserData {

	public String server;
	public String id;
	public String pwd;
	public int level;
	public String name;
	public String email;
	public String homepage;
	public String comment;
	public String point;
	public boolean discloseInfo;
	public boolean isShowComment;

	public UserData() {
	}

	public UserData(String id, String pwd, String server) {
		super();
		this.id = id;
		this.pwd = pwd;
		this.server = server;
	}

	@Override
	public String toString() {
		return "UserData{" +
				"server='" + server + '\'' +
				", id='" + id + '\'' +
				", pwd='" + pwd + '\'' +
				", level=" + level +
				", name='" + name + '\'' +
				", email='" + email + '\'' +
				", homepage='" + homepage + '\'' +
				", comment='" + comment + '\'' +
				", point='" + point + '\'' +
				", discloseInfo=" + discloseInfo +
				", isShowComment=" + isShowComment +
				'}';
	}
}
