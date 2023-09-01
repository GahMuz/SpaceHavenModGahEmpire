package com.gah.empire.aspect;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.gah.empire.dao.ShipDao;
import com.gah.empire.utils.ReflectionUtils;
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
import fi.bugbyte.utils.FastXMLReader;

@Aspect
public class NavigateAspect {

	private ShipDao shipDao = new ShipDao();

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
		boolean hasPlayerShip = displayFleets(sector, list, font, fontColor);

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
	private boolean displayFleets( Sector sector, ScrollList list, BitmapFont font, Color fontColor )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		boolean hasPlayerShip = false;
		Array<StarMap.Fleet> fleets = sector.getFleet();
		if ( fleets != null ) {
			for ( StarMap.Fleet fleet : fleets ) {
				if ( fleet.getFaction().side == FactionUtils.FactionSide.Player ) {
					System.out.println(fleet);
					if ( displayPlayerFleet(sector, list, font, fontColor, fleet) ) {
						hasPlayerShip = true;
					}
				} else
					displayOtherFleet(sector, list, font, fontColor, fleet);
			}
		}
		return hasPlayerShip;
	}

	private boolean displayPlayerFleet( Sector sector, ScrollList list, BitmapFont font, Color fontColor, Fleet fleet )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		World world = GUI.instance.getWorld();

		boolean hasPlayerShip = false;
		if ( fleet.createdShips != null ) {
			for ( StarMap.CreatedShip cs : fleet.createdShips ) {
				Ship ship = shipDao.loadShip(world, cs, false);
				if ( ship != null ) {
					list.addItem(shipSectorInfo(fleet, ship, font, fontColor));
					hasPlayerShip = true;
				}
			}
		}
		return hasPlayerShip;
	}

	private SectorInfo shipSectorInfo( Fleet fleet, Ship ship, BitmapFont font, Color fontColor )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		System.out.println(ship.getName());
		SectorInfo i = new SectorInfo();
		ReflectionUtils.setDeclaredField(i, "iconColor", FactionUtils.hostilityMap.getColor(fleet.getFaction().side).getStarMapColor());
		ReflectionUtils.setDeclaredField(i, "anim", fleet.getFactionIcon());
		ReflectionUtils.setDeclaredField(i, "name", ship.getName());
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
	private void saveSector( Sector sector ) {
		sector.save(null);
	}

	private void loadSector( Sector sector )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		GUI gui = GUI.instance;
		World world = gui.getWorld();
		GameData gameData = world.getGameData();
		StarMap starmap = sector.getSystem().getMap();

		System.out.println("----------------------- sector : " + sector.getName());

		// get initial sector
		Sector from = world.getStarMap().getPlayerAt();
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

		// display fleets
		//hideShipOfSector(world, from, true);
		//hideShipOfSector(world, sector, false);

		WorldUtils.initWorldLoad();

		debugShips(world, 1);

		// update space
		updateSpace(gui, world, starmap, sector);

		debugShips(world, 2);

		// set new sector
		ReflectionUtils.setDeclaredField(starmap, "playerAt", sector);
		ReflectionUtils.setDeclaredField(gameData, "playerSectorId", sector.getId());

		debugShips(world, 3);

		//removeShip(world, from);
		//addShip(world, sector);

		debugShips(world, 4);

		System.out.println();

		System.out.println("check sector ship");
		for ( Fleet fleet : sector.getFleet() ) {
			for ( CreatedShip cs : fleet.createdShips ) {
				System.out.println("-- cs : " + cs.getShipName(fleet.getFaction().side));
				Ship ship = shipDao.loadShip(world, cs, true);
				//world.addShip(ship);
				world.getShips().add(ship);
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

		space = ReflectionUtils.setDeclaredField(world, "space", new Space(SpaceHavenSettings.getMapSizeX(), SpaceHavenSettings.getMapSizeY(), world));
		ReflectionUtils.setDeclaredField(world, "topSpace", new TopSpace(SpaceHavenSettings.getMapSizeX(), SpaceHavenSettings.getMapSizeY(), world));

		debugShips(world, 12);

		boolean load = false;
		if ( !load ) {

			StarMap.SectorInformation info = sector.getInformation();
			FastXMLReader.Element loadSpace = null;
			if ( info != null && info.saved && ( loadSpace = info.getSpaceMap(world.getGameData().getCurrentGameSlot().getTempDir()) ) == null ) {
				loadSpace = info.getSpaceMap(world.getGameData().getCurrentGameSlot().getDir());
			}
			if ( loadSpace == null ) {
				System.out.println("prep");
				space.prepFor(sector);
			} else {
				System.out.println("load map");
				space.loadMap(loadSpace, world);

				debugShips(world, 13);
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

				debugShips(world, 14);

				World.toLoad.clear();
				for ( Environment.EnvHazard h : space.getHazards() ) {
					h.playerArrivedAgain();
				}
			}

			debugShips(world, 15);
			space.setSectorStuff(sector, world.getGameSettings(), starMap);
			space.setSectorSolarHeat(world.getSolarOutput(), world.getGameSettings(), starMap);
			Array<StarMap.WorldAddable> worldAddable = new Array<StarMap.WorldAddable>(false, 12);
			//sector.addWorldAddable(worldAddable);

			debugShips(world, 16);
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

			debugShips(world, 17);
			World.toLoad = null;
		}
		debugShips(world, 18);
		if ( world.getBg() != null ) {
			world.getBg().dispose();
		}
		debugShips(world, 19);
		if ( !load ) {
			world.setBg(Backgrounds.getBackground(world));
			if ( Backgrounds.useFBO ) {
				world.setBg(Backgrounds.wrapFbo(world.getBg()));
			}
			debugShips(world, 20);
			space.setSectorSolarHeat(world.getSolarOutput(), world.getGameSettings(), starMap);
		}

		debugShips(world, 21);
		world.getTrades().clear();
		space.setDebugGrid(Game.library.getAnimation("floorTile1"));
	}

	private void debugShips( World world, int i ) {
		System.out.println("check world ship " + i);
		for ( Ship ship : world.getShips() ) {
			System.out.println("-- ship : " + ship.getName());
		}
	}

	private void hideShipOfSector( World world, Sector sector, boolean hidden ) {
		for ( Fleet fleet : sector.getFleet() ) {
			for ( CreatedShip cs : fleet.createdShips ) {
				Ship ship = null;
				for ( Ship s : world.getShips() ) {
					if ( s.getShipId() == cs.createdShipId ) {
						ship = s;
					}
				}

				if ( ship != null )
					ship.hidden = hidden;
				else
					System.out.println("unfound ship:" + cs.getShipName(fleet.getFaction().side));
			}
		}
	}

	private void removeShip( World world, Sector sector ) {
		for ( Fleet fleet : sector.getFleet() ) {
			for ( CreatedShip cs : fleet.createdShips ) {
				Ship ship = null;
				for ( Ship s : world.getShips() ) {
					if ( s.getShipId() == cs.createdShipId ) {
						ship = s;
					}
				}

				if ( ship != null ) {
					System.out.println("remove ship:" + cs.getShipName(fleet.getFaction().side));
					ship.saveMap(null);
					world.removeShip(ship);
				}
			}
		}
	}

	private void addShip( World world, Sector sector ) {
		for ( Fleet fleet : sector.getFleet() ) {
			for ( CreatedShip cs : fleet.createdShips ) {
				Ship ship = null;
				for ( Ship s : world.getShips() ) {
					if ( s.getShipId() == cs.createdShipId ) {
						ship = s;
					}
				}

				if ( ship == null ) {
					ship = shipDao.loadShip(world, cs, true);
					if ( ship != null ) {
						System.out.println("add ship:" + cs.getShipName(fleet.getFaction().side));
						//world.addShip(ship);
					}
				}
			}
		}
	}
	/*
	private void deploy() {
		SectorMap.this.lastAnswer = true;
	    SectorMap.this.stillOpen = false;
	    SpaceHaven.disableWorkers = false;
	    if (SectorMap.this.isMoveShips) {
	        for (AwayTeam.Mission m : SectorMap.this.world.getAcceptedMissions()) {
	            AwayTeam.MissionRule r = m.getRule(AwayTeam.MissionRuleType.CannotLeaveSector);
	            if (r == null) continue;
	            r.failed = true;
	        }
	        for (IsoShip s : SectorMap.this.playerShips) {
	            if (s.changedPosition && s.newGridX >= 0 && s.newGridY >= 0) {
	                SectorMap.this.world.getSpace().moveShipToNewPos(s.newGridX, s.newGridY, s.ship);
	            }
	            if (!SectorMap.this.wasLongJump) continue;
	            GameCustomization.DiffThreat travelThreat = GameCustomization.DiffThreat.NoThreat;
	            if (((SectorMap)SectorMap.this).world.getGameSettings().modeSettings instanceof GameCustomization.NormalModeDiffSettings) {
	                travelThreat = ((GameCustomization.NormalModeDiffSettings)((SectorMap)SectorMap.this).world.getGameSettings().modeSettings).interTravelThreat;
	            }
	            GameCustomization.Randomness randomness = ((SectorMap)SectorMap.this).world.getGameSettings().modeSettings.getRandomness();
	            if (travelThreat == GameCustomization.DiffThreat.NoThreat) continue;
	            int rolls = 0;
	            float chanceToGetOneLevel = 0.0f;
	            int minLevel = 0;
	            switch (travelThreat) {
	                case NoThreat: {
	                    break;
	                }
	                case LittleThreat: {
	                    minLevel = 1;
	                    break;
	                }
	                case MediumThreat: {
	                    minLevel = 2;
	                    break;
	                }
	                case SubstantialThreat: {
	                    rolls = 1;
	                    chanceToGetOneLevel = 0.6f;
	                    minLevel = 3;
	                    break;
	                }
	                case SeriousThreat: {
	                    rolls = 2;
	                    chanceToGetOneLevel = 0.8f;
	                    minLevel = 6;
	                    break;
	                }
	            }
	            Array<Character> arr = s.ship.getCharacters();
	            for (int k = 0; k < arr.size; ++k) {
	                AbstractPersonality.Condition condition;
	                Character c = arr.get(k);
	                if (c.isInStasis()) continue;
	                ConditionManager.trigger(AbstractPersonality.ConditionTriggerSignal.NoHyperSleepChamber, c.getPersonality(), "");
	                int sicknessLevel = minLevel;
	                for (int i = 0; i < rolls; ++i) {
	                    boolean hit = EncounterHelpers.rollDice(randomness, Game.random, "longJump_sickness", chanceToGetOneLevel);
	                    if (!hit) continue;
	                    ++sicknessLevel;
	                }
	                if (sicknessLevel <= 0 || (condition = SpaceHaven.library.getCondition(3164)) == null) continue;
	                condition.setLevel(sicknessLevel);
	                condition.setLevelAffectsAndRandoms();
	                c.getPersonality().addCondition(condition, null);
	            }
	        }
	        if (SectorMap.this.cameraClosestTo != null) {
	            int sx = SectorMap.this.cameraClosestTo.getShipGridX() + SectorMap.this.cameraClosestTo.getMassCenterX() / 2;
	            int sy = SectorMap.this.cameraClosestTo.getShipGridY() + SectorMap.this.cameraClosestTo.getMassCenterY() / 2;
	            Vector2 k = GridUtils.toIsometric(sx, sy);
	            float shipX = k.x;
	            float shipY = k.y;
	            float cameraX = shipX - SectorMap.this.cameraOffX;
	            float cameraY = shipY - SectorMap.this.cameraOffY;
	            float zoom = SectorMap.this.world.getRenderer().getZoom();
	            SectorMap.this.world.getRenderer().setCameraPos(cameraX, cameraY, zoom);
	        }
	    }
	    for (Craft c : SectorMap.this.world.getCrafts()) {
	        c.stop();
	    }
	    for (Ship s : SectorMap.this.world.getShips()) {
	        s.getRoof().redoRoofLine();
	    }
	    SectorMap.this.world.redoSunRayMap();
	    if (SectorMap.this.isMoveShips) {
	        SectorMap.this.world.setCameraToPlayerShip(true);
	        float target = SectorMap.this.world.getAutoSaveTimeTargetTime();
	        float now = SectorMap.this.world.getAutoSaveTimer();
	        if (now > 120.0f && target - now > 60.0f) {
	            SectorMap.this.world.setAutoSaveTimer(target - 5.0f);
	        }
	    }
	}*/

	/**********************************************************************************************************
	 * Animate the click on on enter sector
	 **********************************************************************************************************/

	private void animate( GUI gui, Sector sector )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
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