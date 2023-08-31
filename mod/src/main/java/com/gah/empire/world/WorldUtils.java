package com.gah.empire.world;

import com.badlogic.gdx.utils.Array;

import fi.bugbyte.spacehaven.world.World;

public class WorldUtils {

	public static void initWorldLoad() {
		if ( World.toLoad == null ) {
			World.toLoad = new Array(false, 1000);
		}
	}
}
