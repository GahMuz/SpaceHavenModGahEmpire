package com.gah.empire.model;

import java.util.HashMap;
import java.util.Map;

import com.gah.empire.dao.DataDao;

import fi.bugbyte.spacehaven.starmap.StarMap.Sector;
import fi.bugbyte.spacehaven.world.Ship;

public class Data {

	private Map<Integer, Integer> stations = new HashMap<>();

	public void put( int stationId, int sectorId ) {
		stations.put(stationId, sectorId);
		DataDao.save();
	}

	public void put( Ship station, Sector sector ) {
		stations.put(station.getShipId(), sector.getId());
		DataDao.save();
	}

	public int get( Ship station ) {
		return stations.getOrDefault(station.getShipId(), 0);
	}

	public void remove( Ship station ) {
		stations.remove(station.getShipId());
		DataDao.save();
	}

	public Map<Integer, Integer> get() {
		return stations;
	}
}
