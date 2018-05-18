package com.andry.gitcompanysearch.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class CompanyItem implements Parcelable{
    @SerializedName(value = "login")
    private String name;
    @SerializedName(value = "location")
    private String location;
    @SerializedName(value = "blog")
    private String site;
    @SerializedName(value = "avatar_url")
    private String imageUrl;
    @SerializedName(value = "public_repos")
    private int totalRepos;

    public CompanyItem(Parcel in) {
        name = in.readString();
        location = in.readString();
        site = in.readString();
        imageUrl = in.readString();
        totalRepos = in.readInt();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getTotalRepos() {
        return totalRepos;
    }

    public void setTotalRepos(int totalRepos) {
        this.totalRepos = totalRepos;
    }

    @Override
    public String toString() {
        return name + " " + location + " " + site + imageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(location);
        dest.writeString(site);
        dest.writeString(imageUrl);
        dest.writeInt(totalRepos);
    }

    public static final Parcelable.Creator<CompanyItem> CREATOR = new Parcelable.Creator<CompanyItem>() {
        @Override
        public CompanyItem createFromParcel(Parcel in) {
            return new CompanyItem(in);
        }

        @Override
        public CompanyItem[] newArray(int size) {
            return new CompanyItem[size];
        }
    };
}
