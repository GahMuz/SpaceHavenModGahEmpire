package com.gah.empire.station;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.gah.empire.utils.ReflectionUtils;

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
import fi.bugbyte.spacehaven.world.Visuals;
import fi.bugbyte.spacehaven.world.World;

@Aspect
public class NavigateAspect {

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
		boolean hasPlayerShip = false;
		Array<StarMap.Fleet> fleets = sector.getFleet();
		if ( fleets != null ) {
			for ( StarMap.Fleet f : fleets ) {
				if ( f.getFaction().side == FactionUtils.FactionSide.Player ) {
					if ( f.createdShips != null ) {
						for ( StarMap.CreatedShip cs : f.createdShips ) {
							list.addItem(createdShipSectorInfo(f, cs, font, fontColor));
							hasPlayerShip = true;
						}
					}
				} else if ( sector.isVisible() ) {
					SectorInfo i = new SectorInfo();
					ReflectionUtils.setDeclaredField(i, "iconColor", FactionUtils.hostilityMap.getColor(f.getFaction().side).getStarMapColor());
					ReflectionUtils.setDeclaredField(i, "anim", f.getFactionIcon());
					ReflectionUtils.setDeclaredField(i, "name", f.getFaction().getName());
					ReflectionUtils.setDeclaredField(i, "font", font);
					ReflectionUtils.setDeclaredField(i, "fontColor", fontColor);
					list.addItem(i);
				}
			}
		}

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

	private SectorInfo createdShipSectorInfo( Fleet fleet, CreatedShip cs, BitmapFont font, Color fontColor )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		SectorInfo i = new SectorInfo();
		ReflectionUtils.setDeclaredField(i, "iconColor", FactionUtils.hostilityMap.getColor(fleet.getFaction().side).getStarMapColor());
		ReflectionUtils.setDeclaredField(i, "anim", fleet.getFactionIcon());
		ReflectionUtils.setDeclaredField(i, "name", cs.getShipName(fleet.getFaction().side));
		ReflectionUtils.setDeclaredField(i, "font", font);
		ReflectionUtils.setDeclaredField(i, "fontColor", fontColor);
		return i;
	}

	private void addDisplayButton( Sector sector, SelectionBox selectionBox ) {

		IconButton1 button = UnitControlButtons1.getEnter();
		Vector2 v = selectionBox.getSkin().getPos(GuiSkin.BoxCorner.TopRight);
		button.moveTo(v.x - 50.0f * Settings.uiScale, v.y - 15.0f * Settings.uiScale);
		button.setScale(0.75f);
		button.setClickHandler(new StageButton.clickHandler() {
			@Override
			public void clicked() {
				StarMap starmap = sector.getSystem().getMap();
				try {
					ReflectionUtils.setDeclaredField(starmap, "playerAt", sector);
					GUI gui = GUI.instance;
					World world = gui.getWorld();
					GameData gameData = world.getGameData();
					ReflectionUtils.setDeclaredField(gameData, "playerSectorId", sector.getId());

					StarMapScreen sms = (StarMapScreen) gui.getActivePopup();
					ReflectionUtils.getDeclaredMethod(sms, "cancelAutoTravel", Arrays.asList(), Arrays.asList());

					StarMap map = ReflectionUtils.getDeclaredField(sms, "map");
					map.cancelRoute();
					map.setDropToNormal(true);
					ReflectionUtils.setDeclaredField(sms, "useZoomInToDrop", true);

					ScrollTarget scrollTarget = ReflectionUtils.getDeclaredField(sms, "scrollTarget");
					scrollTarget.zoomInLerpTo(sector);

					ReflectionUtils.setDeclaredField(sms, "zoomInTime", 0.0f);

				} catch ( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException
						| InvocationTargetException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//starmap.setPlayerAt(sector);
			}
		});
		selectionBox.addButton(button);
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
}