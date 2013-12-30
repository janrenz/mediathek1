/*
 * Copyright (C) 2013 Jan Renz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.janrenz.app.mediathek;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {
	
	private String title;
	private String subtitle;
	private String extId;
	private String thumbnail;
	private String duration;	
	private String starttime;
	private Boolean isLive;
	private int starttimestamp;
	private String description;
	private ArrayList<String[]> sources = new ArrayList<String[]>() ;
	
	 public Movie(Parcel in) {  
	     readFromParcel(in);  
	    }  
	 public Movie() {  
	     return;
	    }  
	 private void readFromParcel(Parcel in) {    
	        // ...  
	        title = in.readString();  
	        subtitle = in.readString();  
	        extId = in.readString();  
	        thumbnail = in.readString();  
	        duration = in.readString();  
	        starttime = in.readString();  
	        description = in.readString();
	        starttimestamp = in.readInt();  
	         int isLiveInt =in.readInt();
	         if (isLiveInt == 1){
	        	 isLive = true;
	         }else{
	        	 isLive = false;
	         }
	        //this will be treated sligty differnet
	        in.readList (sources, String.class.getClassLoader());

	    }  
	  
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {  
    
        public Movie createFromParcel(Parcel in) {  
            return new Movie(in);  
        }  
        
        public Movie[] newArray(int size) {  
            return new Movie[size];  
        }  
        
    };
			
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSubtitle() {
		return subtitle;
	}
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	public String getExtId() {
		return extId;
	}
	public void setExtId(String extId) {
		this.extId = extId;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	//
	public String getSenderinfo(){
		return "ARD > " + this.getStarttime() + " Uhr";
	}
	public ArrayList<String[]> getSources() {
		return sources;
	}
	public void setSources(ArrayList<String[]> sources) {
		this.sources = sources;
	}
	public String getStarttime() {
		return starttime;
	}
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(title);  
		dest.writeString(subtitle); 
		dest.writeString(extId);
		dest.writeString(duration);
		dest.writeString(thumbnail);
		dest.writeString(getStarttime());
		dest.writeString(description);
		dest.writeInt(getStarttimestamp());
		dest.writeInt(getIsLiveAsInt());
		dest.writeList(sources);
	}
	private int getIsLiveAsInt() {
		// TODO Auto-generated method stub
		if (this.getIsLive()){
			return 1;
		}else{
			return 0;			
		}
	}
	public int getStarttimestamp() {
		return starttimestamp;
	}
	public void setStarttimestamp(int i) {
		this.starttimestamp = i;
	}
	public Boolean getIsLive() {
		return isLive;
	}
	public void setIsLive(Boolean isLive) {
		this.isLive = isLive;
	}
	public void setIsLive(int isLive) {
		this.setIsLive(isLive==1);
	}
	public void setIsLive(String isLive) {
		if (isLive.equalsIgnoreCase("true")){
			this.setIsLive(true);
		}else{
			this.setIsLive(false);
		}
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
