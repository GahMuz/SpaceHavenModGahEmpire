package com.gah.empire.dao;

import java.io.File;

import fi.bugbyte.spacehaven.GameData.SaveGame;
import fi.bugbyte.spacehaven.gui.GUI;
import fi.bugbyte.spacehaven.world.World;

public class Dao {

	protected static File getFile( World world, String path, boolean temp ) {
		SaveGame slot = world.getGameData().getCurrentGameSlot();
		String dir = temp ? slot.getTempDir() : slot.getDir();
		File f = new File(dir + path);
		return f;
	}

	protected static void mkdirs( File file ) {
		File dir = file.getParentFile();
		if ( !dir.exists() )
			dir.mkdirs();
	}

	protected static World getWorld() {
		return GUI.instance.getWorld();
	}
}
