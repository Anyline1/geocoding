package ru.anyline.geoservice.Service;

public interface Geocode {

    String geocode(String address);
    String reverseGeocode(double latitude, double longitude);
    boolean isAddressCached(String address);

}
