package com.gah.empire.aspect;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.gah.empire.dao.SectorDao;
import com.gah.empire.dao.ShipDao;
import com.gah.empire.utils.ReflectionUtils;
import com.gah.empire.world.StationHolder;
import com.gah.empire.world.WorldUtils;

import fi.bugbyte.framework.Game;
import fi.bugbyte.framework.Settings;
import fi.bugbyte.framework.library.Locale;
import fi.bugbyte.framework.screen.ExpandableItem;
import fi.bugbyte.framework.screen.GuiSkin;
import fi.bugbyte.framework.screen.ScrollList;
import fi.bugbyte.framework.screen.StageButton;
import fi.bugbyte.gen.compiled.GUISettings;
import fi.bugbyte.gen.compiled.GuiSkin1;
import fi.bugbyte.gen.compiled.GuiSkins3;
import fi.bugbyte.gen.compiled.IconButton1;
import fi.bugbyte.gen.compiled.UnitControlButtons1;
import fi.bugbyte.spacehaven.GameData;
import fi.bugbyte.spacehaven.SpaceHaven;
import fi.bugbyte.spacehaven.SpaceHavenSettings;
import fi.bugbyte.spacehaven.gui.GUI;
import fi.bugbyte.spacehaven.gui.MenuSystem;
import fi.bugbyte.spacehaven.gui.MenuSystem.SelectionBox;
import fi.bugbyte.spacehaven.gui.MenuSystemItems.SectorSelected;
import fi.bugbyte.spacehaven.gui.MenuSystemItems.SectorSelected.SectorInfo;
import fi.bugbyte.spacehaven.gui.StarMapScreen;
import fi.bugbyte.spacehaven.gui.StarMapScreen.ScrollTarget;
import fi.bugbyte.spacehaven.starmap.Bodies;
import fi.bugbyte.spacehaven.starmap.StarMap;
import fi.bugbyte.spacehaven.starmap.StarMap.CreatedShip;
import fi.bugbyte.spacehaven.starmap.StarMap.Fleet;
import fi.bugbyte.spacehaven.starmap.StarMap.Sector;
import fi.bugbyte.spacehaven.stuff.FactionUtils;
import fi.bugbyte.spacehaven.world.Backgrounds;
import fi.bugbyte.spacehaven.world.Environment;
import fi.bugbyte.spacehaven.world.Ship;
import fi.bugbyte.spacehaven.world.Space;
import fi.bugbyte.spacehaven.world.TopSpace;
import fi.bugbyte.spacehaven.world.Visuals;
import fi.bugbyte.spacehaven.world.World;
import fi.bugbyte.spacehaven.world.World.LoadJob;

@Aspect
public class NavigateAspect {

	private ShipDao shipDao = new ShipDao();
	private SectorDao sectorDao = new SectorDao();

	/* *****************************************************************************************
	 *                           Mod SectorSelected.open Method
	 * display information on player ship in starmap selected sector
	 * ****************************************************************************************/
	@Pointcut( "execution(void fi.bugbyte.spacehaven.gui.MenuSystemItems.SectorSelected.open(fi.bugbyte.spacehaven.gui.MenuSystem.SelectionBox)) && args(selectionBox) " )
	public void modOpen( SelectionBox selectionBox ) {
	}

	@Around( "modOpen(selectionBox)" )
	public void aroundOpen( ProceedingJoinPoint pjp, MenuSystem.SelectionBox selectionBox ) throws Throwable {
		SectorSelected _this = ReflectionUtils.getThis(pjp);

		Sector sector = ReflectionUtils.getDeclaredField(_this, "sector");

		Array<StarMap.SectorStuff> stuff;

		// open from AbstractSelectedBoxItem
		ReflectionUtils.setDeclaredField(_this, 2, "selectionBox", selectionBox);
		Array<StageButton> myButtons = ReflectionUtils.getDeclaredField(_this, 2, "myButtons");
		GuiSkin skin = ReflectionUtils.setDeclaredField(_this, 2, "skin", selectionBox.getSkin());

		if ( myButtons != null ) {
			for ( StageButton b : myButtons ) {
				selectionBox.addButton(b);
			}
		}

		// open from SingleSelectedBoxItem
		Vector2 v = skin.getTitlePos(ReflectionUtils.getDeclaredField(_this, 1, "name"));
		ReflectionUtils.setDeclaredField(_this, 1, "nameX", v.x);
		ReflectionUtils.setDeclaredField(_this, 1, "nameY", v.y + ( Game.lang == Locale.CN ? 2.0f * Settings.uiScale : 0 ));

		// back to black
		GuiSkin1 listSkin = ReflectionUtils.setDeclaredField(_this, "listSkin", GuiSkins3.getGeneralTextBox1());
		float skinOffX = 20.0f * Settings.uiScale;
		float skinOffY = 30.0f * Settings.uiScale;
		listSkin.setSize(skin.getWidth() - skinOffX, skin.getHeight() - skinOffY);
		listSkin.setPos(skin.getPosX() + skinOffX / 2.0f, skin.getPosY() - skinOffY / 2.0f);
		GuiSkin.ListBuilder builder = listSkin.getListBuilder(!SpaceHaven.isMobile);
		builder.setListHOffset(4.0f * Settings.uiScale);
		builder.setItemW(listSkin.getWidth());
		builder.setItemHeight(35.0f * Settings.uiScale);
		builder.setItemSpacingY(0.0f);
		builder.setItemSpacingX(10.0f * Settings.uiScale);
		builder.setReducedScrollbarItemW(true, 20.0f * Settings.uiScale);
		builder.setSplitMode(ExpandableItem.SplitMode.Horizontal, skin.getWidth() / 3.0f);
		ScrollList list = ReflectionUtils.setDeclaredField(_this, 2, "list", builder.build());
		list.getScrollBar().setAlwaysShowScrollBar(false);
		BitmapFont font = skin.getNormalFont();
		Color fontColor = skin.normalFontColor;

		if ( sector.hasMission() ) {
			SectorInfo i = new SectorInfo();
			ReflectionUtils.setDeclaredField(i, "iconScale", (float) ReflectionUtils.getDeclaredField(i, "iconScale") * Settings.uiScale);
			ReflectionUtils.setDeclaredField(i, "iconColor", GUISettings.generalInProgressColor);
			ReflectionUtils.setDeclaredField(i, "anim", Game.library.getAnimation("starmapMissionLegendIcon"));
			ReflectionUtils.setDeclaredField(i, "name", Game.library.getTextById("5574").getText());
			ReflectionUtils.setDeclaredField(i, "iconScale", (float) ReflectionUtils.getDeclaredField(i, "iconScale") * Settings.uiScale);
			ReflectionUtils.setDeclaredField(i, "font", font);
			ReflectionUtils.setDeclaredField(i, "fontColor", fontColor);
			list.addItem(i);
		}

		if ( ( stuff = sector.getStuff() ) != null ) {
			for ( StarMap.SectorStuff sectorStuff : stuff ) {
				if ( sectorStuff.getType().isHidden() )
					continue;
				SectorInfo i = new SectorInfo();
				if ( sectorStuff.type == StarMap.SectorStuffType.WarpGate || sectorStuff.type == StarMap.SectorStuffType.Exit ) {
					ReflectionUtils.setDeclaredField(i, "iconScale", 0.75f);
				} else if ( sectorStuff.type == StarMap.SectorStuffType.Station ) {
					Color color = ReflectionUtils.setDeclaredField(i, "iconColor", new Color(sectorStuff.getTint()));
					color.a = 1.0f;
				}
				ReflectionUtils.setDeclaredField(i, "iconScale", (float) ReflectionUtils.getDeclaredField(i, "iconScale") * Settings.uiScale);
				ReflectionUtils.setDeclaredField(i, "anim", sectorStuff.getAnim());
				ReflectionUtils.setDeclaredField(i, "name", sectorStuff.getName());
				if ( sectorStuff instanceof StarMap.SectorResource && sector.isVisited() ) {
					ReflectionUtils.setDeclaredField(i, "name", sectorStuff.getName() + " " + ( (StarMap.SectorResource) sectorStuff ).howMuch);
				}
				ReflectionUtils.setDeclaredField(i, "font", font);
				ReflectionUtils.setDeclaredField(i, "fontColor", fontColor);
				list.addItem(i);
			}
		}

		/********************* core modif ************************/
		boolean samesector = sector.equals(GUI.instance.getWorld().getStarMap().getPlayerAt());
		boolean hasPlayerShip = displayFleets(sector, list, font, fontColor, samesector);

		if ( hasPlayerShip ) {
			addDisplayButton(sector, selectionBox);
		}
		/********************* end core modif ************************/

		if ( !sector.isVisited() ) {
			SectorInfo i = new SectorInfo();
			ReflectionUtils.setDeclaredField(i, "anim", null);
			ReflectionUtils.setDeclaredField(i, "name", Game.library.getTextById("3984").getText());
			ReflectionUtils.setDeclaredField(i, "font", font);
			ReflectionUtils.setDeclaredField(i, "fontColor", fontColor);
			list.addItem(i);
		}

		if ( Game.debug ) {
			for ( int k = list.getAllListItems().size; k < 6; ++k ) {
				this.addText(_this, "", font, fontColor);
			}
			float solar = StarMap.getSolarOuptut(sector);
			StarMap.StarMapSystem starMapSystem = sector.getSystem();
			this.addText(_this, "id:" + sector.getId(), font, fontColor);
			this.addText(_this, "systemid:" + starMapSystem.getSystemId(), font, fontColor);
			this.addText(_this, "dstToPlr:" + starMapSystem.getDistanceFromPlayer(), font, fontColor);
			this.addText(_this, "solar:" + Visuals.df.format(solar), font, fontColor);
			this.addText(_this, "galaxies:" + starMapSystem.getMap().getNewGalaxies(), font, fontColor);
			this.addText(_this, "diff:" + starMapSystem.meta.difficulty + "(" + starMapSystem.getMap().getDiffScale(sector) + ")", font, fontColor);

			if ( starMapSystem.meta.isMainSystemEvent() ) {
				this.addText(_this, "event", font, fontColor);
			}
			if ( sector instanceof Bodies.AsteroidField ) {
				this.addText(_this, "density: " + ( (Bodies.AsteroidField) sector ).getDensity().toString(), font, fontColor);
			}
		}

		for ( int i = 0; i < 10; ++i ) {
			list.update(0.016f);
		}
	}

	private void addText( SectorSelected _this, String text, BitmapFont font, Color fontColor )
			throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ReflectionUtils.getDeclaredMethod(_this, "addText", Arrays.asList(String.class, BitmapFont.class, Color.class), Arrays.asList(text, font, fontColor));
	}

	/**********************************************************************************************************
	 * Display fleet in sector map
	 **********************************************************************************************************/
	private boolean displayFleets( Sector sector, ScrollList list, BitmapFont font, Color fontColor, boolean samesector )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		boolean hasPlayerShip = false;
		Array<StarMap.Fleet> fleets = sector.getFleet();
		if ( fleets != null ) {
			for ( StarMap.Fleet fleet : fleets ) {
				if ( fleet.getFaction().side == FactionUtils.FactionSide.Player ) {
					if ( displayPlayerFleet(sector, list, font, fontColor, fleet, samesector) ) {
						hasPlayerShip = true;
					}
				} else
					displayOtherFleet(sector, list, font, fontColor, fleet);
			}
		}
		return hasPlayerShip;
	}

	private boolean displayPlayerFleet( Sector sector, ScrollList list, BitmapFont font, Color fontColor, Fleet fleet, boolean samesector )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		World world = GUI.instance.getWorld();

		boolean hasPlayerShip = false;
		if ( fleet.createdShips != null ) {
			Iterator<CreatedShip> iterator = fleet.createdShips.iterator();
			while ( iterator.hasNext() ) {
				CreatedShip cs = iterator.next();
				Ship ship = shipDao.loadShip(world, cs, false);
				int sectorId = StationHolder.get(ship);
				if ( ship.getName() != null && ( sectorId == 0 || sectorId == sector.getId() ) ) {
					list.addItem(shipSectorInfo(fleet, ship.getName(), font, fontColor));
					hasPlayerShip = true;
				} else if ( sectorId != 0 )
					iterator.remove();
			}
		}
		return hasPlayerShip;
	}

	private SectorInfo shipSectorInfo( Fleet fleet, String shipname, BitmapFont font, Color fontColor )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		SectorInfo i = new SectorInfo();
		ReflectionUtils.setDeclaredField(i, "iconColor", FactionUtils.hostilityMap.getColor(fleet.getFaction().side).getStarMapColor());
		ReflectionUtils.setDeclaredField(i, "anim", fleet.getFactionIcon());
		ReflectionUtils.setDeclaredField(i, "name", shipname);
		ReflectionUtils.setDeclaredField(i, "font", font);
		ReflectionUtils.setDeclaredField(i, "fontColor", fontColor);
		return i;
	}

	private void displayOtherFleet( Sector sector, ScrollList list, BitmapFont font, Color fontColor, Fleet fleet )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if ( sector.isVisible() ) {
			SectorInfo i = new SectorInfo();
			ReflectionUtils.setDeclaredField(i, "iconColor", FactionUtils.hostilityMap.getColor(fleet.getFaction().side).getStarMapColor());
			ReflectionUtils.setDeclaredField(i, "anim", fleet.getFactionIcon());
			ReflectionUtils.setDeclaredField(i, "name", fleet.getFaction().getName());
			ReflectionUtils.setDeclaredField(i, "font", font);
			ReflectionUtils.setDeclaredField(i, "fontColor", fontColor);
			list.addItem(i);
		}
	}

	/**********************************************************************************************************
	 * Display enter sector button
	 **********************************************************************************************************/

	private void addDisplayButton( Sector sector, SelectionBox selectionBox ) {

		IconButton1 button = UnitControlButtons1.getEnter();
		Vector2 v = selectionBox.getSkin().getPos(GuiSkin.BoxCorner.TopRight);
		button.moveTo(v.x - 50.0f * Settings.uiScale, v.y - 15.0f * Settings.uiScale);
		button.setScale(0.75f);
		button.setClickHandler(new StageButton.clickHandler() {
			@Override
			public void clicked() {
				try {
					loadSector(sector);
				} catch ( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException
						| InvocationTargetException e ) {
					e.printStackTrace();
				}
			}
		});
		selectionBox.addButton(button);
	}

	/**********************************************************************************************************
	 *                                    
	 **********************************************************************************************************/
	private void loadSector( Sector sector )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		System.out.println("load sector : " + sector.getName());

		GUI gui = GUI.instance;
		World world = gui.getWorld();
		GameData gameData = world.getGameData();
		StarMap starmap = sector.getSystem().getMap();

		// get initial sector
		Sector from = world.getStarMap().getPlayerAt();
		sectorDao.save(world, sector, false);

		from.getSystem().getMap().checkSectorStuff(world);
		// save it
		ReflectionUtils.getDeclaredMethod(world, "saveSector", Arrays.asList(), Arrays.asList());

		// save and remove ships
		ShipDao dao = new ShipDao();
		for ( Ship ship : world.getShips() ) {
			dao.saveToDisk(world, ship);
			world.removeShip(ship);
		}

		// clearing world
		world.getShips().clear();
		world.getCrafts().clear();
		world.getFloatingItems().clear();
		world.getSpaceWalkers().clear();
		world.getEncounterAIs().clear();
		world.clearProjectiles();

		WorldUtils.initWorldLoad();

		// set new sector
		ReflectionUtils.setDeclaredField(starmap, "playerAt", sector);
		ReflectionUtils.setDeclaredField(gameData, "playerSectorId", sector.getId());

		// update space
		updateSpace(gui, world, starmap, sector);

		// load ship
		for ( Fleet fleet : sector.getFleet() ) {
			for ( CreatedShip cs : fleet.createdShips ) {
				Ship ship = shipDao.loadShip(world, cs, true);
			}
		}

		animate(gui, sector);
	}

	// inspired by World.createNewSector()
	private void updateSpace( GUI gui, World world, StarMap starMap, Sector sector )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Space space = world.getSpace();

		//for (ExplosionHelper.SpaceProjectileDamageCounter p : this.damageCounters) {
		//    p.setDone();
		//}

		world.getLightWorker().removeSpace(space);
		space.dispose();

		space = ReflectionUtils.setDeclaredField(world, "space", sectorDao.loadSector(world, sector));
		ReflectionUtils.setDeclaredField(world, "topSpace", new TopSpace(SpaceHavenSettings.getMapSizeX(), SpaceHavenSettings.getMapSizeY(), world));

		if ( space == null ) {
			System.out.println("missing space");
			space = ReflectionUtils.setDeclaredField(world, "space", new Space(SpaceHavenSettings.getMapSizeX(), SpaceHavenSettings.getMapSizeY(), world));
			space.prepFor(sector);
		} else {
			System.out.println("load map");
			World.toLoad.sort(new Comparator<LoadJob>() {

				@Override
				public int compare( LoadJob o1, LoadJob o2 ) {
					if ( o1.getPriority() > o2.getPriority() ) {
						return 1;
					}
					if ( o1.getPriority() < o2.getPriority() ) {
						return -1;
					}
					return 0;
				}
			});

			for ( LoadJob j : World.toLoad ) {
				j.load(world);
			}

			World.toLoad.clear();
			for ( Environment.EnvHazard h : space.getHazards() ) {
				h.playerArrivedAgain();
			}

			space.setSectorStuff(sector, world.getGameSettings(), starMap);
			space.setSectorSolarHeat(world.getSolarOutput(), world.getGameSettings(), starMap);
			Array<StarMap.WorldAddable> worldAddable = new Array<StarMap.WorldAddable>(false, 12);
			sector.addWorldAddable(worldAddable);

			for ( StarMap.WorldAddable a : worldAddable ) {
				a.addToWorld(world, world.getRenderer());
				World.toLoad.sort(new Comparator<LoadJob>() {

					@Override
					public int compare( LoadJob o1, LoadJob o2 ) {
						if ( o1.getPriority() > o2.getPriority() ) {
							return 1;
						}
						if ( o1.getPriority() < o2.getPriority() ) {
							return -1;
						}
						return 0;
					}
				});

				for ( LoadJob j : World.toLoad ) {
					j.load(world);
				}
				World.toLoad.clear();
			}

			World.toLoad = null;
		}

		if ( world.getBg() != null ) {
			world.getBg().dispose();
		}

		//if ( !load ) {
		world.setBg(Backgrounds.getBackground(world));
		if ( Backgrounds.useFBO ) {
			world.setBg(Backgrounds.wrapFbo(world.getBg()));
		}
		space.setSectorSolarHeat(world.getSolarOutput(), world.getGameSettings(), starMap);
		//}

		world.getTrades().clear();
		space.setDebugGrid(Game.library.getAnimation("floorTile1"));

		GUI.instance.refreshSectorContent();
	}

	private void debugShips( World world, int i ) {
		System.out.println("check world ship " + i);
		for ( Ship ship : world.getShips() ) {
			System.out.println("-- ship : " + ship.getName());
		}
	}

	/**********************************************************************************************************
	 * Animate the click on on enter sector
	 **********************************************************************************************************/

	private void animate( GUI gui, Sector sector )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		gui.getWorld().setCameraToPlayerShip(false);

		// nice zoom effect from StarMapScreen.drop()
		StarMapScreen sms = (StarMapScreen) gui.getActivePopup();
		ReflectionUtils.setDeclaredField(sms, "selectedDropTarget", sector);

		ReflectionUtils.getDeclaredMethod(sms, "dropOrOpenPopup", Arrays.asList(), Arrays.asList());
		ReflectionUtils.getDeclaredMethod(sms, "cancelAutoTravel", Arrays.asList(), Arrays.asList());

		StarMap map = ReflectionUtils.getDeclaredField(sms, "map");
		map.cancelRoute();
		map.setDropToNormal(true);
		ReflectionUtils.setDeclaredField(sms, "useZoomInToDrop", true);

		ScrollTarget scrollTarget = ReflectionUtils.getDeclaredField(sms, "scrollTarget");
		scrollTarget.zoomInLerpTo(sector);

		ReflectionUtils.setDeclaredField(sms, "zoomInTime", 0.0f);
	}
}