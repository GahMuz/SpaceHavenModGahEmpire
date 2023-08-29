package com.gah.empire.utils.jareditor;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.gah.empire.utils.jareditor.adapters.Adapter;
import com.gah.empire.utils.jareditor.adapters.AddFieldAdapter;
import com.gah.empire.utils.jareditor.adapters.PublicClassAdapter;

public class Launch {

	public static void main( String[] args ) throws Exception {
		String jarPath = "C:/Program Files (x86)/Steam/steamapps/common/SpaceHaven/spacehaven.jar";

		Map<String, Adapter> modifications = new HashMap<>();
		modifications.put("fi/bugbyte/spacehaven/gui/MenuSystemItems$SectorSelected.class", new PublicClassAdapter());
		modifications.put("fi/bugbyte/spacehaven/gui/MenuSystemItems$SectorSelected$SectorInfo.class", new PublicClassAdapter());
		modifications.put("fi/bugbyte/spacehaven/gui/StarMapScreen.class", new PublicClassAdapter());
		modifications.put("fi/bugbyte/spacehaven/gui/StarMapScreen$ScrollTarget.class", new PublicClassAdapter());

		modifications.put("fi/bugbyte/spacehaven/world/Ship.class", new AddFieldAdapter("hidden", Type.BOOLEAN_TYPE, Opcodes.ACC_PUBLIC));

		JarEditor editor = new JarEditor(jarPath, modifications);
		editor.edit();
	}
}