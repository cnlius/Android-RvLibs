package com.jason.app.vo;

/**
 * Created by frank on 2017/4/14.
 */

public class GroupInfo {
    private int mGroupID; // 组号
    private String mTitle; // Header 的 title
    private int position; // 在组内的索引
    private int mGroupLength; // 组的成员个数???

    public GroupInfo(int groupId, int position, String title) {
        this.mGroupID = groupId;
        this.position = position;
        this.mTitle = title;
    }

    public int getGroupID() {
        return mGroupID;
    }

    public void setGroupID(int groupID) {
        this.mGroupID = groupID;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public boolean isFirstViewInGroup() {
        return position == 0;
    }

    public boolean isLastViewInGroup() {
        return position == mGroupLength - 1 && position >= 0;
    }

    public void setGroupLength(int groupLength) {
        this.mGroupLength = groupLength;
    }
}
