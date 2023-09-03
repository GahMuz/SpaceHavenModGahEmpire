package com.gah.empire.dao;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.gah.empire.model.Data;

import fi.bugbyte.spacehaven.world.World;
import fi.bugbyte.utils.FastXMLReader;

public class DataDao extends Dao {

	private static Data DATA = null;

	private static File getStationFile( World world ) {
		return getFile(world, "gah", false);
	}

	private static FastXMLReader.Element getStationData( World world ) {
		File file = getStationFile(world);

		if ( file.exists() ) {
			FastXMLReader.Element e = FastXMLReader.parse(file);
			return e;
		}
		return null;
	}

	private static Data load( World world ) {
		FastXMLReader.Element element = getStationData(world);
		if ( element == null )
			return null;

		Data data = new Data();
		FastXMLReader.Element map = element.getChild("stations");
		if ( map != null && map.hasChildren() ) {
			for ( FastXMLReader.Element c : map.getChildren() ) {
				int stationId = c.getInt("stationId");
				int sectorId = c.getInt("sectorId");
				data.put(stationId, sectorId);
			}
		}
		return data;
	}

	public static void save() {
		World world = getWorld();
		File file = getStationFile(world);
		mkdirs(file);
		FastXMLReader.Element element = FastXMLReader.newElement("gah");
		FastXMLReader.Element map = element.addChild("stations");
		for ( Map.Entry<Integer, Integer> entry : getData().get().entrySet() ) {
			FastXMLReader.Element e = map.addChild("e");
			e.setAttribute("stationId", entry.getKey());
			e.setAttribute("sectorId", entry.getValue());
		}

		try {
			element.saveToFile(file);
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	public static Data getData() {
		World world = getWorld();
		if ( DATA == null )
			DATA = load(world);
		return DATA;
	}
}
