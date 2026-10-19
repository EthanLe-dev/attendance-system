package com.attendance_system.service;

import com.attendance_system.config.LocationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationProperties locationProperties;

    public double calculateDistance(BigDecimal latitude, BigDecimal longitude) {
        double studentLatitude = latitude.doubleValue();
        double studentLongitude = longitude.doubleValue();

        double museumLatitude = locationProperties.getLatitude().doubleValue();

        double museumLongitude = locationProperties.getLongitude().doubleValue();

        return haversine(
                studentLatitude,
                studentLongitude,
                museumLatitude,
                museumLongitude
        );
    }

    public boolean isWithinRadius(BigDecimal latitude, BigDecimal longitude) {
        double distance = calculateDistance(
                latitude,
                longitude
        );

        double radius = locationProperties.getRadiusMeters().doubleValue();

        return distance <= radius;
    }

    private double haversine(double latitude1, double longitude1, double latitude2, double longitude2) {
        final double EARTH_RADIUS_METERS = 6_371_000;

        double latitudeDifference = Math.toRadians(latitude2 - latitude1);

        double longitudeDifference = Math.toRadians(longitude2 - longitude1);

        double a = Math.sin(latitudeDifference / 2)
                    * Math.sin(latitudeDifference / 2)
                    +
                    Math.cos(Math.toRadians(latitude1))
                            * Math.cos(Math.toRadians(latitude2))
                            * Math.sin(longitudeDifference / 2)
                            * Math.sin(longitudeDifference / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    public boolean isAccuracyAcceptable(BigDecimal accuracy) {
        return accuracy.compareTo(
                locationProperties.getMaxAccuracyMeters()
        ) <= 0;
    }
}
