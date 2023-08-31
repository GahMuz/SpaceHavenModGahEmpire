package com.gah.empire.ship;

import java.io.File;
import java.io.IOException;

import com.gah.empire.world.WorldUtils;

import fi.bugbyte.framework.maths.MathHelp;
import fi.bugbyte.spacehaven.GameData.SaveGame;
import fi.bugbyte.spacehaven.ai.encounters.Encounter;
import fi.bugbyte.spacehaven.starmap.StarMap.CreatedShip;
import fi.bugbyte.spacehaven.stuff.crafts.ContainerCraft;
import fi.bugbyte.spacehaven.stuff.crafts.Craft;
import fi.bugbyte.spacehaven.world.Ship;
import fi.bugbyte.spacehaven.world.ShipHelper;
import fi.bugbyte.spacehaven.world.World;
import fi.bugbyte.spacehaven.world.elements.Door;
import fi.bugbyte.spacehaven.world.elements.Floor;
import fi.bugbyte.utils.FastXMLReader;
import fi.bugbyte.utils.Scheduler;

public class ShipDao {

	private File getShipFile( World world, int shipId ) {
		SaveGame slot = world.getGameData().getCurrentGameSlot();

		String dir = slot.getTempDir();
		File f = getShipFile(dir, shipId);
		if ( !f.exists() ) {
			f = null;
		}
		if ( f == null ) {
			dir = slot.getDir();
			f = getShipFile(dir, shipId);
		}
		return f;
	}

	private File getShipFile( String dir, int shipId ) {
		return new File(dir + "ships/ship" + shipId);
	}

	/******************************************************
	 * Save
	 *****************************************************/
	public void saveToDisk( World world, Ship ship ) {
		Craft c;

		FastXMLReader.Element data = FastXMLReader.newElement("ship");
		data.setAttribute("nglsCnt", world.getStarMap().getNewGalaxies());
		/*
		boolean useSurvivesLeftBehind = ship.isDerelict() && !jumpedAway;
		Iterator iter = ship.getCharacters().iterator();
		while ( ( (Array.ArrayIterator) iter ).hasNext() ) {
			Character c2 = (Character) ( (Array.ArrayIterator) iter ).next();
			if ( !c2.getControlSide().isRenegade && ( !useSurvivesLeftBehind || c2.survivesBeingLeftBehind() ) )
				continue;
			( (Array.ArrayIterator) iter ).remove();
			c2.dispose();
		}
		Iterator riter = ship.getRobots().iterator();
		while ( ( (Array.ArrayIterator) riter ).hasNext() ) {
			Robot r = (Robot) ( (Array.ArrayIterator) riter ).next();
			if ( !useSurvivesLeftBehind || r.survivesBeingLeftBehind() )
				continue;
			( (Array.ArrayIterator) riter ).remove();
			r.dispose();
		}
		*/
		ship.saveMap(data);
		FastXMLReader.Element craftXML = null;
		for ( Floor.Hangar h : ship.getHangars() ) {
			c = h.getCraftInBay();
			if ( c == null || c.getHomeHangar() != h )
				continue;
			if ( craftXML == null ) {
				craftXML = data.addChild("crafts");
			}
			c.saveState(craftXML.addChild("c"));
		}
		for ( Door.CargoPort port : ship.getCargoPorts() ) {
			c = port.getCraftInBay();
			if ( !( c instanceof ContainerCraft ) || ( (ContainerCraft) c ).isCanBeDiscarded() )
				continue;
			if ( craftXML == null ) {
				craftXML = data.addChild("crafts");
			}
			c.saveState(craftXML.addChild("c"));
		}
		File file = getShipFile(world, ship.getShipId());
		try {
			data.saveToFile(file);
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/******************************************************
	 * Load
	 *****************************************************/
	public Ship loadShip( World world, CreatedShip createdShip, boolean addToWorld ) {
		Ship ship = loadShipFromWorld(world, createdShip);
		if ( ship == null )
			ship = loadFromFile(world, createdShip, addToWorld);
		return ship;
	}

	private Ship loadShipFromWorld( World world, CreatedShip createdShip ) {
		for ( Ship ship : world.getShips() )
			if ( ship.getShipId() == createdShip.createdShipId )
				return ship;
		return null;
	}

	private Ship loadFromFile( World world, CreatedShip createdShip, boolean addToWorld ) {
		WorldUtils.initWorldLoad();

		File f = getShipFile(world, createdShip.createdShipId);
		if ( f.exists() ) {
			final Ship s = new Ship(world.getRenderer(), world);
			FastXMLReader.Element data = FastXMLReader.parse(f);
			if ( data != null ) {
				s.loadMap(data, world);
				if ( addToWorld ) {
					world.schedule(new Scheduler.ScheduledEvent() {

						@Override
						public void onSchedule() {
							Encounter.EncounterShip.replenishFactionShipStuff(s, createdShip.getMapAi());
						}
					}, 1.0f);
					world.addShip(s);
				}
				ShipHelper.checkDamage(s);
				FastXMLReader.Element craftsXML = data.getChild("crafts");
				if ( craftsXML != null && craftsXML.hasChildren() ) {
					for ( FastXMLReader.Element c : craftsXML.getChildren() ) {
						int cid = c.getInt("cid");
						MathHelp.helpVector.x = 1.0f;
						MathHelp.helpVector.y = 1.0f;
						Craft craft = world.addCraft(cid, MathHelp.helpVector, s.getCurrentOwnerSide());
						if ( craft == null )
							continue;
						craft.loadState(c);
						if ( addToWorld )
							craft.setWorld(world);
					}
				}
			}
			return s;
		}
		return null;
	}
}
