package com.gah.empire.world;

import java.util.HashMap;
import java.util.Map;

import fi.bugbyte.spacehaven.starmap.StarMap.Sector;
import fi.bugbyte.spacehaven.world.Ship;

public class StationHolder {

	public static Map<Integer, Integer> HOLDER = new HashMap<>();

	public static void put( int stationId, int sectorId ) {
		HOLDER.put(stationId, sectorId);
	}

	public static void put( Ship station, Sector sector ) {
		HOLDER.put(station.getShipId(), sector.getId());
	}

	public static int get( Ship station ) {
		return HOLDER.getOrDefault(station.getShipId(), 0);
	}

	public static void remove( Ship station ) {
		HOLDER.remove(station.getShipId());
	}
}
