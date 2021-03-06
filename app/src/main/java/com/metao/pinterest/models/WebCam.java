package com.metao.pinterest.models; /**
 * Created by metao on 2/1/2017.
 */

import java.io.Serializable;

@SuppressWarnings("serial")
public class WebCam implements Serializable {

    private static final String EMPTY_STRING = "";

    private long id;
    private long uniId;
    private boolean isStream;
    private String sku;
    private boolean isInFavorites;
    private int type;
    private String webCamName;
    private String webCamTags;
    private String webCamUrl;
    private String webCamThumbUrl;
    private String webCamTimeLapseDay;
    private String webCamTimeLapseMonth;
    private int position;
    private int status;
    private double latitude;
    private double longitude;
    private String country;
    private boolean popular;
    private String created_at;
    private boolean selected;
    private long requested;
    private boolean free;
    private float price;

    // constructors
    public WebCam() {
    }

    public WebCam(boolean isStream, String webCamName, String webCamDescription, String webCamUrl,
                  String webCamThumbUrl, double latitude, double longitude) {
        this.isStream = isStream;
        this.webCamName = webCamName;
        this.webCamTags = webCamDescription;
        this.webCamUrl = webCamUrl;
        this.webCamThumbUrl = webCamThumbUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // setters
    public void setId(long id) {
        this.id = id;
    }

    public void setUniId(long uniId) {
        this.uniId = uniId;
    }

    public void setIsStream(boolean isStream) {
        this.isStream = isStream;
    }

    public void setIsInFavorites(boolean isInFavorites) {
        this.isInFavorites = isInFavorites;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setName(String webCamName) {
        this.webCamName = webCamName;
    }

    public void setTags(String webCamTags) {
        this.webCamTags = webCamTags;
    }

    public void setUrl(String webCamUrl) {
        this.webCamUrl = webCamUrl;
    }

    public void setThumbUrl(String webCamThumbUrl) {
        this.webCamThumbUrl = webCamThumbUrl;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setPopular(boolean popular) {
        this.popular = popular;
    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    // getters
    public long getId() {
        return this.id;
    }

    public long getUniId() {
        return this.uniId;
    }

    public boolean isStream() {
        return this.isStream;
    }

    public boolean isInFavorites() {
        return this.isInFavorites;
    }

    public int getType() {
        return this.type;
    }

    public String getName() {
        return this.webCamName;
    }

    public String getTags() {
        if (webCamTags != null) {
            return this.webCamTags;
        } else return EMPTY_STRING;
    }

    public String getUrl() {
        return this.webCamUrl;
    }

    public String getThumbUrl() {
        if (webCamThumbUrl != null) {
            return this.webCamThumbUrl;
        } else return EMPTY_STRING;
    }

    public String getWebCamTimeLapseDay() {
        return webCamTimeLapseDay;
    }

    public void setWebCamTimeLapseDay(String webCamTimeLapseDay) {
        this.webCamTimeLapseDay = webCamTimeLapseDay;
    }

    public String getWebCamTimeLapseMonth() {
        return webCamTimeLapseMonth;
    }

    public void setWebCamTimeLapseMonth(String webCamTimeLapseMonth) {
        this.webCamTimeLapseMonth = webCamTimeLapseMonth;
    }

    public int getPosition() {
        return this.position;
    }

    public int getStatus() {
        return this.status;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String getCountry() {
        return this.country;
    }

    public boolean isPopular() {
        return this.popular;
    }

    public String getCreatedAt() {
        return this.created_at;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public long getRequested() {
        return requested;
    }

    public void setRequested(long requested) {
        this.requested = requested;
    }

    public boolean isFree() {
        return price == 0 ? true : false;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}