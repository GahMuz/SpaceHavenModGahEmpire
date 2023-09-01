package com.gah.empire.dao;

import fi.bugbyte.spacehaven.starmap.StarMap.Sector;

public class SectorDao {

	public void save( Sector sector ) {
		sector.save(null);
	}
}
