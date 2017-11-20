package com.mockumatrix.storm;

import java.util.ArrayList;
import java.util.List;

public class StormEntry {

	long tweetId = 0;
	String tweetText;
	List<String> attachmentPaths;
	
	public StormEntry(Integer tweetId, String tweetText, List<String> paths){
		this.tweetId = Long.parseLong(tweetId.toString());
		this.tweetText = tweetText;
		this.attachmentPaths = paths;
	}

	public StormEntry() {
		attachmentPaths = new ArrayList<String>();
	}

	public StormEntry(String text) {
		this();
		tweetText = text;
	}

	public void setTweetText(String tweetText) {
		this.tweetText = tweetText;
	}

	public long getTweetId() {
		return tweetId;
	}

	public void setTweetId(long tweetId) {
		this.tweetId = tweetId;
	}

	public List<String> getAttachmentPaths() {
		return attachmentPaths;
	}

	public void setAttachmentPaths(List<String> attachmentPaths) {
		this.attachmentPaths = attachmentPaths;
	}

	public String getTweetText() {
		return tweetText;
	}

	@Override
	public String toString() {
		return "StormEntry [tweetId=" + tweetId + ", tweetText=" + tweetText
				+ ", attachmentPaths=" + attachmentPaths + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attachmentPaths == null) ? 0 : attachmentPaths.hashCode());
		result = prime * result + (int) (tweetId ^ (tweetId >>> 32));
		result = prime * result
				+ ((tweetText == null) ? 0 : tweetText.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StormEntry other = (StormEntry) obj;
		if (attachmentPaths == null) {
			if (other.attachmentPaths != null)
				return false;
		} else if (!attachmentPaths.equals(other.attachmentPaths))
			return false;
		if (tweetId != other.tweetId)
			return false;
		if (tweetText == null) {
			if (other.tweetText != null)
				return false;
		} else if (!tweetText.equals(other.tweetText))
			return false;
		return true;
	}

}
