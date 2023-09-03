package com.gah.empire.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.gah.empire.utils.ReflectionUtils;

import fi.bugbyte.spacehaven.world.Ship;
import fi.bugbyte.spacehaven.world.World;
import fi.bugbyte.utils.FastXMLReader;

@Aspect
public class ShipAspect {

	/* *****************************************************************************************
	 *                           Mod SectorSelected.open Method
	 * display information on player ship in starmap selected sector
	 * ***************************************************************************************
	@Pointcut( "execution(void fi.bugbyte.spacehaven.world.Ship.draw(fi.bugbyte.framework.graphics.Renderer2D, float)) && args(renderer, zoom) " )
	public void modDraw( Renderer2D renderer, float zoom ) {
	}
	
	@Around( "modDraw(renderer, zoom)" )
	public void aroundOpen( ProceedingJoinPoint pjp, Renderer2D renderer, float zoom ) throws Throwable {
		Ship _this = ReflectionUtils.getThis(pjp);
		if ( !_this.hidden )
			pjp.proceed();
	}
	*/

	/* *****************************************************************************************
	 *                           Mod save and load ship for new attributes
	 * ***************************************************************************************/
	@Pointcut( "execution(void fi.bugbyte.spacehaven.world.Ship.saveMap(fi.bugbyte.utils.FastXMLReader.Element)) && args(data) " )
	public void modSaveMap( FastXMLReader.Element data ) {
	}

	@Around( "modSaveMap(data)" )
	public void aroundSaveMap( ProceedingJoinPoint pjp, FastXMLReader.Element data ) throws Throwable {
		Ship _this = ReflectionUtils.getThis(pjp);
		int stationSectorId = _this.stationSectorId != null ? _this.stationSectorId : 0;
		data.setAttribute("ssi", stationSectorId);
		pjp.proceed();
	}

	@Pointcut( "execution(void fi.bugbyte.spacehaven.world.Ship.laodMap(fi.bugbyte.utils.FastXMLReader.Element, fi.bugbyte.spacehaven.world.World)) && args(data, world) " )
	public void modLoadMap( FastXMLReader.Element data, World world ) {
	}

	@Around( "modLoadMap(data, world)" )
	public void aroundLoadMap( ProceedingJoinPoint pjp, FastXMLReader.Element data, World world ) throws Throwable {
		Ship _this = ReflectionUtils.getThis(pjp);
		_this.stationSectorId = data.getInt("ssi", 0);
		pjp.proceed();
	}

	/* *****************************************************************************************
	 *                           set ship stationSectorId
	 * ***************************************************************************************/
	@Pointcut( "set(Integer fi.bugbyte.spacehaven.world.Ship.stationSectorId) && args(stationSectorId) " )
	public void modSetStationSectorId( Integer stationSectorId ) {
	}

	@Around( "modSetStationSectorId(stationSectorId)" )
	public void arounddSetStationSectorId( ProceedingJoinPoint pjp, Integer stationSectorId ) throws Throwable {
		if ( pjp.getThis() instanceof Ship ) {
			Ship _this = ReflectionUtils.getThis(pjp);
			pjp.proceed();
		} else
			pjp.proceed();
	}
}