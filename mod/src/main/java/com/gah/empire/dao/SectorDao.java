package com.gah.empire.dao;

import java.io.File;
import java.io.IOException;

import fi.bugbyte.spacehaven.SpaceHavenSettings;
import fi.bugbyte.spacehaven.starmap.StarMap.CreatedShip;
import fi.bugbyte.spacehaven.starmap.StarMap.Fleet;
import fi.bugbyte.spacehaven.starmap.StarMap.Sector;
import fi.bugbyte.spacehaven.world.Ship;
import fi.bugbyte.spacehaven.world.Space;
import fi.bugbyte.spacehaven.world.World;
import fi.bugbyte.utils.FastXMLReader;

public class SectorDao extends Dao {

	private ShipDao shipDao = new ShipDao();

	private File getSectorFile( World world, Sector sector, boolean temp ) {
		return getFile(world, "sector" + sector.getId() + "/space", temp);
	}

	private FastXMLReader.Element getSectorData( World world, Sector sector ) {
		File file = getSectorFile(world, sector, true);
		if ( !file.exists() )
			file = getSectorFile(world, sector, false);

		if ( file.exists() ) {
			FastXMLReader.Element e = FastXMLReader.parse(file);
			return e;
		}
		System.out.println("no file exists for sector " + sector.getId());
		return null;
	}

	public Space loadSector( World world, Sector sector ) {
		FastXMLReader.Element loadSpace = getSectorData(world, sector);
		if ( loadSpace == null )
			return null;

		Space space = new Space(SpaceHavenSettings.getMapSizeX(), SpaceHavenSettings.getMapSizeY(), world);
		space.loadMap(loadSpace, world);
		return space;
	}

	public void save( World world, Sector sector, boolean temp ) {
		File file = getSectorFile(world, sector, temp);
		mkdirs(file);
		FastXMLReader.Element data = FastXMLReader.newElement("space");
		data.setAttribute("nglsCnt", sector.getSystem().getMap().getNewGalaxies());
		world.getSpace().saveMap(data, false);
		try {
			data.saveToFile(file);
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	public void displaySectorFleet( World world, Sector sector ) {
		if ( sector.getFleet() != null ) {
			for ( Fleet fleet : sector.getFleet() ) {
				if ( fleet.createdShips != null ) {
					for ( CreatedShip cs : fleet.createdShips ) {
						Ship ship = shipDao.loadShip(world, cs, false);
						System.out.println("-- ship " + ship.getName() + " (" + ship.stationSectorId + ")");
					}
				}
			}
		}

	}
}
