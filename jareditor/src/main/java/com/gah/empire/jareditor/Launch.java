package com.gah.empire.jareditor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.gah.empire.jareditor.adapters.Adapter;
import com.gah.empire.jareditor.adapters.AddFieldAdapter;
import com.gah.empire.jareditor.adapters.PublicClassAdapter;
import com.gah.empire.jareditor.adapters.PublicFieldAdapter;

public class Launch {

	public static void main( String[] args ) throws Exception {
		String jarPath = "spacehaven.jar";
		System.out.println("Edit spacehaven jar");
		File file = new File(jarPath);
		if ( !file.exists() )
			System.out.println("invalid location : " + jarPath);

		else {
			Map<String, Adapter> modifications = new HashMap<>();
			modifications.put("fi/bugbyte/spacehaven/gui/MenuSystemItems$SectorSelected.class", new PublicClassAdapter());
			modifications.put("fi/bugbyte/spacehaven/gui/MenuSystemItems$SectorSelected$SectorInfo.class", new PublicClassAdapter());
			modifications.put("fi/bugbyte/spacehaven/gui/StarMapScreen.class", new PublicClassAdapter());
			modifications.put("fi/bugbyte/spacehaven/gui/StarMapScreen$ScrollTarget.class", new PublicClassAdapter());

			modifications.put("fi/bugbyte/spacehaven/world/Ship.class",
					new AddFieldAdapter("stationSectorId", Type.getType(Integer.class), Opcodes.ACC_PUBLIC));

			modifications.put("fi/bugbyte/spacehaven/world/World.class", new PublicFieldAdapter("toLoad", Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC));

			List<String> excludes = new ArrayList<>();
			excludes.add("fi/bugbyte/spacehaven/steam/SpacehavenSteam.class");

			JarEditor editor = new JarEditor(jarPath, modifications, excludes);
			editor.edit();
		}
	}
}