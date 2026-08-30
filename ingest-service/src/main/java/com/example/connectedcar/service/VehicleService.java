package com.example.connectedcar.service;

import com.example.connectedcar.domain.Vehicle;
import com.example.connectedcar.storage.VehicleCache;
import com.example.connectedcar.storage.VehicleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleCache vehicleCache;

    public VehicleService(VehicleRepository vehicleRepository, VehicleCache vehicleCache) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleCache = vehicleCache;
    }

    public List<Vehicle> listVehicles() {
        vehicleRepository.markOffline(LocalDateTime.now().minusSeconds( 15));
        List<Vehicle> vehicles = vehicleRepository.findAll();
        for (Vehicle v : vehicles) {
            Vehicle cached = vehicleCache.getLatestState(v.getVin());
            if (cached != null) {
                v.setStatus(cached.getStatus());
                v.setLastSpeed(cached.getLastSpeed());
                v.setLastLat(cached.getLastLat());
                v.setLastLon(cached.getLastLon());
                v.setLastSeen(cached.getLastSeen());
            }
        }
        return vehicles;
    }

    public Vehicle getVehicle(String vin) {
        return vehicleRepository.findByVin(vin);
    }
}
