package com.gah.empire.jareditor;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.gah.empire.jareditor.adapters.Adapter;
import com.gah.empire.jareditor.adapters.AddFieldAdapter;
import com.gah.empire.jareditor.adapters.PublicClassAdapter;
import com.gah.empire.jareditor.adapters.PublicFieldAdapter;

public class Launch {

	public static void main( String[] args ) throws Exception {
		String jarPath = "C:/Program Files (x86)/Steam/steamapps/common/SpaceHaven/spacehaven.jar";

		Map<String, Adapter> modifications = new HashMap<>();
		modifications.put("fi/bugbyte/spacehaven/gui/MenuSystemItems$SectorSelected.class", new PublicClassAdapter());
		modifications.put("fi/bugbyte/spacehaven/gui/MenuSystemItems$SectorSelected$SectorInfo.class", new PublicClassAdapter());
		modifications.put("fi/bugbyte/spacehaven/gui/StarMapScreen.class", new PublicClassAdapter());
		modifications.put("fi/bugbyte/spacehaven/gui/StarMapScreen$ScrollTarget.class", new PublicClassAdapter());

		modifications.put("fi/bugbyte/spacehaven/world/Ship.class", new AddFieldAdapter("stationSectorId", Type.getType(Integer.class), Opcodes.ACC_PUBLIC));

		modifications.put("fi/bugbyte/spacehaven/world/World.class", new PublicFieldAdapter("toLoad", Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC));

		JarEditor editor = new JarEditor(jarPath, modifications);
		editor.edit();
	}
}