package com.gah.empire.aspect;

import org.aspectj.lang.annotation.Aspect;

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
}