package com.pulsarappdev.scaledronetestproject;

/**
 * Created by Shaunak, using Scaledrone guide on 25/09/2018.
 */

public class Message {
	private String text;
	private MemberData data;
	private boolean belongsToCurrentUser;

	public Message(String text, MemberData data, boolean belongsToCurrentUser) {
		this.text = text;
		this.data = data;
		this.belongsToCurrentUser = belongsToCurrentUser;
	}

	public String getText() {
		return text;
	}

	public MemberData getData() {
		return data;
	}

	public boolean isBelongsToCurrentUser() {
		return belongsToCurrentUser;
	}
}