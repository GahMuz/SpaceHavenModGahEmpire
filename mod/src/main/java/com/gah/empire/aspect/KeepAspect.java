package com.gah.empire.aspect;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.badlogic.gdx.utils.Array;
import com.gah.empire.dao.SectorDao;
import com.gah.empire.dao.ShipDao;
import com.gah.empire.utils.ReflectionUtils;
import com.gah.empire.world.StationHolder;

import fi.bugbyte.framework.Game;
import fi.bugbyte.framework.screen.Screen;
import fi.bugbyte.spacehaven.ai.AwayTeam;
import fi.bugbyte.spacehaven.ai.EncounterAI;
import fi.bugbyte.spacehaven.ai.Job;
import fi.bugbyte.spacehaven.gui.GUI;
import fi.bugbyte.spacehaven.gui.MenuSystemItems.JumpMenu;
import fi.bugbyte.spacehaven.gui.popups.LeftBehindPopup;
import fi.bugbyte.spacehaven.starmap.StarMap.Sector;
import fi.bugbyte.spacehaven.stuff.Character;
import fi.bugbyte.spacehaven.stuff.Entity;
import fi.bugbyte.spacehaven.stuff.Enums;
import fi.bugbyte.spacehaven.stuff.FactionUtils;
import fi.bugbyte.spacehaven.stuff.Robot;
import fi.bugbyte.spacehaven.stuff.crafts.Craft;
import fi.bugbyte.spacehaven.world.Events;
import fi.bugbyte.spacehaven.world.Services;
import fi.bugbyte.spacehaven.world.Ship;
import fi.bugbyte.spacehaven.world.Ship.HyperTravelCapability;
import fi.bugbyte.spacehaven.world.Ship.ShipSettings;
import fi.bugbyte.spacehaven.world.Stations.AbstractStation;
import fi.bugbyte.spacehaven.world.Stations.StationType;
import fi.bugbyte.spacehaven.world.World;
import fi.bugbyte.spacehaven.world.elements.Floor;
import fi.bugbyte.spacehaven.world.elements.Hull;

@Aspect
public class KeepAspect {

	private SectorDao sectorDao = new SectorDao();

	private ShipDao shipDao = new ShipDao();

	/* *****************************************************************************************
	 *                           Mod JumpPressed Method
	 * prevent warning on ship lose: might need to handle various case like when station crew is outside ...
	 * ****************************************************************************************/

	@Pointcut( "execution(void fi.bugbyte.spacehaven.gui.MenuSystemItems.JumpMenu.jumpPressed())" )
	public void modJumpPressed() {
	}

	@Around( "modJumpPressed()" )
	public void aroundJumpPressed( ProceedingJoinPoint pjp ) throws Throwable {
		JumpMenu _this = ReflectionUtils.getThis(pjp);
		World world = ReflectionUtils.getDeclaredField(_this, "world");

		boolean allReady = true;
		Array<Ship> shipsLeftBehind = new Array<Ship>(false, 8);
		Array<Character> crewLeftBehind = getLeftBehind(_this, world);
		Array<Craft> craftsLeftBehind = new Array<Craft>(false, 8);
		Array<Robot> robotsLeftBehind = getRobotsLeftBehind(_this, world);

		Array<Ship> leavesPartyOnJump = ReflectionUtils.getDeclaredField(_this, "leavesPartyOnJump");
		leavesPartyOnJump.clear();

		Sector sector = world.getStarMap().getPlayerAt();
		for ( Ship s : world.getShips() ) {
			EncounterAI.AiShipInfo info;
			boolean check = s.isPlayerShip();
			if ( !check && ( info = s.getAiShipInfo(false) ) != null && info.isInPlayerFleet() ) {
				check = true;
			}
			if ( !check )
				continue;

			if ( isStation(s) ) {
				s.stationSectorId = sector.getId();
				shipDao.saveToDisk(world, s);
				StationHolder.put(s, sector);
				continue;
			}

			Ship.HyperTravelCapability cap = s.getHyperTravelCapability();
			if ( cap != Ship.HyperTravelCapability.Charged ) {
				shipsLeftBehind.add(s);
			}
			if ( cap != Ship.HyperTravelCapability.Charged && !s.isPlayerShip() )
				continue;
			for ( Floor.Hangar h : s.getHangars() ) {
				boolean isLeftBehind;
				if ( h.getDesignatedCraft() == null )
					continue;
				Craft inBay = h.getCraftInBay();
				if ( !( inBay == null || inBay.isDocked() || inBay.isMovingToShipPlane() || inBay.isOnShipPlane() ) ) {
					inBay = null;
				}
				if ( !( isLeftBehind = inBay != h.getDesignatedCraft() || cap != Ship.HyperTravelCapability.Charged ) )
					continue;
				craftsLeftBehind.add(h.getDesignatedCraft());
				if ( s.isPlayerShip() || shipsLeftBehind.contains(s, true) )
					continue;
				shipsLeftBehind.add(s);
				leavesPartyOnJump.add(s);
			}
		}
		if ( craftsLeftBehind.size > 0 ) {
			allReady = false;
		}
		if ( crewLeftBehind.size > 0 ) {
			allReady = false;
		}
		if ( shipsLeftBehind.size > 0 ) {
			allReady = false;
		}
		if ( robotsLeftBehind.size > 0 ) {
			allReady = false;
		}
		if ( allReady ) {
			ReflectionUtils.getDeclaredMethod(_this, "continueWithJumpAfterCheckingLeftBehind", Arrays.asList(), Arrays.asList());
		} else {
			Screen activatedOn = ReflectionUtils.getDeclaredField(_this, 2, "activatedOn");
			if ( activatedOn == null ) {
				GUI.instance.closeJumpMenu();
				return;
			}
			LeftBehindPopup p = new LeftBehindPopup(crewLeftBehind, craftsLeftBehind, shipsLeftBehind, robotsLeftBehind);
			activatedOn.openCustomPopup("", 6542, p);
			world.togglePopupPause(true);
			activatedOn.setPopupClosedCallback(new Screen.PopupClosedCallback() {

				@Override
				public void popupClosed( boolean answer, int id ) {
					if ( answer && id == 6542 ) {
						try {
							ReflectionUtils.getDeclaredMethod(_this, "continueWithJumpAfterCheckingLeftBehind", Arrays.asList(), Arrays.asList());
						} catch ( NoSuchMethodException e ) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch ( IllegalAccessException e ) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch ( IllegalArgumentException e ) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch ( InvocationTargetException e ) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
		}
		Sector from = world.getStarMap().getPlayerAt();
		sectorDao.save(world, from, true);
	}

	private boolean isStation( Ship s ) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		AbstractStation station = ReflectionUtils.getDeclaredField(s, "station");
		return station != null && StationType.Asteroid.equals(station.type);
	}

	public Array<Character> getLeftBehind( JumpMenu _this, World world )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Array<Character> leftBehind = ReflectionUtils.getDeclaredField(_this, "leftBehind");

		boolean count;
		leftBehind.clear();
		for ( Craft c : world.getCrafts() ) {
			if ( c.getSide() != FactionUtils.FactionSide.Player || c.getState() == Craft.CraftState.DockedHangar )
				continue;
			for ( Entity entity : c.getEntitysOnBoard() ) {
				if ( !( entity instanceof Character ) )
					continue;
				Character ch = (Character) entity;
				count = ch.isPlayerChar();
				if ( ch.getOwnerSide() == FactionUtils.FactionSide.Player ) {
					count = true;
				}
				if ( !count )
					continue;
				leftBehind.add(ch);
			}
		}
		for ( Ship s : world.getShips() ) {
			if ( isStation(s) )
				continue;

			boolean isCharged = s.getHyperTravelCapability() == Ship.HyperTravelCapability.Charged;
			for ( Character c : s.getCharacters() ) {
				count = c.isPlayerChar();
				if ( c.getOwnerSide() == FactionUtils.FactionSide.Player ) {
					count = true;
				}
				if ( !count || !s.isLeftBehindOnJump(c) && s.isPlayerShip() && isCharged )
					continue;
				leftBehind.add(c);
			}
		}
		for ( Entity e : world.getSpaceWalkers() ) {
			if ( !( e instanceof Character ) )
				continue;
			Character c = (Character) e;
			boolean bl = c.isPlayerChar();
			if ( c.getOwnerSide() == FactionUtils.FactionSide.Player ) {
				bl = true;
			}
			if ( !bl )
				continue;
			leftBehind.add(c);
		}
		return leftBehind;
	}

	public Array<Robot> getRobotsLeftBehind( JumpMenu _this, World world )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Array<Robot> leftBehindRobots = ReflectionUtils.getDeclaredField(_this, "leftBehindRobots");

		boolean count;
		leftBehindRobots.clear();
		for ( Craft c : world.getCrafts() ) {
			if ( c.getSide() != FactionUtils.FactionSide.Player || c.getState() == Craft.CraftState.DockedHangar )
				continue;
			for ( Entity entity : c.getEntitysOnBoard() ) {
				if ( !( entity instanceof Robot ) )
					continue;
				Robot r = (Robot) entity;
				count = r.isPlayerChar();
				if ( r.getOwnerSide() == FactionUtils.FactionSide.Player ) {
					count = true;
				}
				if ( !count )
					continue;
				leftBehindRobots.add(r);
			}
		}
		for ( Ship s : world.getShips() ) {
			if ( isStation(s) )
				continue;

			boolean isCharged = s.getHyperTravelCapability() == Ship.HyperTravelCapability.Charged;
			for ( Robot c : s.getRobots() ) {
				count = c.isPlayerChar();
				if ( c.getOwnerSide() == FactionUtils.FactionSide.Player ) {
					count = true;
				}
				if ( !count || !s.isLeftBehindOnJump(c) && s.isPlayerShip() && isCharged )
					continue;
				leftBehindRobots.add(c);
			}
		}
		for ( Entity e : world.getSpaceWalkers() ) {
			if ( !( e instanceof Robot ) )
				continue;
			Robot c = (Robot) e;
			boolean bl = c.isPlayerChar();
			if ( c.getOwnerSide() == FactionUtils.FactionSide.Player ) {
				bl = true;
			}
			if ( !bl )
				continue;
			leftBehindRobots.add(c);
		}
		return leftBehindRobots;
	}

	/* *****************************************************************************************
	 *                           Mod jumpShip Method
	 * prevent station loss
	 * ****************************************************************************************/

	@Pointcut( "execution(void fi.bugbyte.spacehaven.world.Ship.jumpShip(boolean, boolean)) && args(toHyperSpace, toNormal) " )
	public void modJumpShip( boolean toHyperSpace, boolean toNormal ) {
	}

	@Around( "modJumpShip(toHyperSpace, toNormal)" )
	public void aroundJumpShip( ProceedingJoinPoint pjp, boolean toHyperSpace, boolean toNormal ) throws Throwable {
		Ship _this = ReflectionUtils.getThis(pjp);

		World world = ReflectionUtils.getDeclaredField(_this, 1, "world");
		Object mode;
		if ( !( toHyperSpace || toNormal || ( mode = world.getMode() ) != World.WorldMode.Normal && mode != World.WorldMode.JumpingNormal ) ) {
			ReflectionUtils.setDeclaredField(_this, "playHyperJumpSound", true);
		}
		if ( toHyperSpace || toNormal ) {
			if ( toHyperSpace ) {
				Array<Hull.EngineHub> engineHubs = ReflectionUtils.getDeclaredField(_this, "engineHubs");
				for ( Hull.EngineHub e : engineHubs ) {
					if ( e.getHost().getElementLink().getFacilityControl().isPaused() )
						continue;
					Enums.Rotation rot = e.getHost().getElementLink().getRot();
					ShipSettings shipSettings = ReflectionUtils.getDeclaredField(_this, "shipSettings");
					ReflectionUtils.setDeclaredField(shipSettings, "rot", rot);
					break;
				}
				if ( _this.getHyperTravelCapability() != HyperTravelCapability.Charged ) {
					ReflectionUtils.setDeclaredField(_this, "leaveBehind", true);
					EncounterAI.AiShipInfo aiShipInfo = ReflectionUtils.getDeclaredField(_this, "aiShipInfo");
					if ( _this.isPlayerShip() ) {
						world.getStarMap().checkPlayerFleet(world, false);
						System.err.println("abandon ship " + _this);
						if ( !isStation(_this) )
							_this.abandon(FactionUtils.FactionSide.Player, false, true);
					} else if ( aiShipInfo != null ) {
						AwayTeam.Mission m;
						if ( aiShipInfo.getInPlayerFleetOfMission() > 0
								&& ( m = world.getAcceptedMissionById(aiShipInfo.getInPlayerFleetOfMission()) ) != null ) {
							m.setFailedFromConsequence(world);
						}
						aiShipInfo.setInPlayerFleet(false, _this, world.getStarMap().getCreatedShipInSector(_this), 0);
						aiShipInfo.setPlayerOrderedJump(false);
						Services.AbsService p = aiShipInfo.getService(Services.ServiceType.Protection);
						if ( p instanceof Services.ProtectionService ) {
							( (Services.ProtectionService) p ).clearAgreement();
						}
					}
					return;
				}
				ReflectionUtils.setDeclaredField(_this, "playHyperJumpSound", true);
				ReflectionUtils.getDeclaredMethod(_this, "doDamageOnHyperJump", Arrays.asList(), Arrays.asList());
				for ( Hull.EngineHub e : engineHubs ) {
					e.jumped();
				}
				Array<Events.OnEvent> workConsoles = ReflectionUtils.getDeclaredField(_this, "workConsoles");
				for ( Events.OnEvent c : workConsoles ) {
					Character nav;
					if ( !c.workConsole.jump || ( nav = c.getWorker() ) == null )
						continue;
					nav.getPersonality().addExperienceForTask(Job.SkillClass.Navigation, 5);
				}
			}
			ReflectionUtils.setDeclaredField(_this, "leaveBehind", false);
			ReflectionUtils.setDeclaredField(_this, "jumpOut", true);
			ReflectionUtils.setDeclaredField(_this, "jumping", true);
			ReflectionUtils.setDeclaredField(_this, "jumpTime", -Game.random.nextFloat());
			ReflectionUtils.setDeclaredField(_this, "jumped", false);
			ShipSettings shipSettings = ReflectionUtils.getDeclaredField(_this, "shipSettings");
			Enums.Rotation rot = ReflectionUtils.getDeclaredField(shipSettings, "rot");
			switch ( rot ) {
				case R0 : {
					ReflectionUtils.setDeclaredField(_this, "jumpYIndex", -10);
					break;
				}
				case R180 : {
					ReflectionUtils.setDeclaredField(_this, "jumpYIndex", _this.getSizeY() + 10);
					break;
				}
				case R270 : {
					ReflectionUtils.setDeclaredField(_this, "jumpYIndex", _this.getSizeX() + 10);
					break;
				}
				case R90 : {
					ReflectionUtils.setDeclaredField(_this, "jumpYIndex", -10);
					break;
				}
			}
		} else {
			_this.setShipMoved(true);
			ReflectionUtils.setDeclaredField(_this, "jumping", true);
			ReflectionUtils.setDeclaredField(_this, "jumpOut", false);
			ReflectionUtils.setDeclaredField(_this, "jumpTime", -Game.random.nextFloat());
			ReflectionUtils.setDeclaredField(_this, "jumped", false);
			ShipSettings shipSettings = ReflectionUtils.getDeclaredField(_this, "shipSettings");
			Enums.Rotation rot = ReflectionUtils.getDeclaredField(shipSettings, "rot");
			switch ( rot ) {
				case R0 : {
					ReflectionUtils.setDeclaredField(_this, "jumpYIndex", _this.getSizeY() + 10);
					break;
				}
				case R180 : {
					ReflectionUtils.setDeclaredField(_this, "jumpYIndex", -10);
					break;
				}
				case R270 : {
					ReflectionUtils.setDeclaredField(_this, "jumpYIndex", -10);
					break;
				}
				case R90 : {
					ReflectionUtils.setDeclaredField(_this, "jumpYIndex", _this.getSizeX() + 10);
					break;
				}
			}
		}
	}
}