package stellarium.stellars.system;

import javax.vecmath.Vector3d;

import stellarapi.api.ICelestialCoordinate;
import stellarapi.api.ISkyEffect;
import stellarapi.api.lib.config.IConfigHandler;
import stellarapi.api.lib.math.SpCoord;
import stellarapi.api.optics.FilterHelper;
import stellarapi.api.optics.IOpticalFilter;
import stellarapi.api.optics.IViewScope;
import stellarapi.api.optics.Wavelength;
import stellarium.client.ClientSettings;
import stellarium.stellars.Optics;
import stellarium.stellars.layer.IRenderCache;
import stellarium.world.IStellarSkySet;

public class PlanetRenderCache implements IRenderCache<Planet, IConfigHandler> {
	
	protected boolean shouldRender;
	protected SpCoord appCoord = new SpCoord();
	protected float appMag;
	protected float size;
	protected float multiplier;
	protected double[] color;
	
	@Override
	public void initialize(ClientSettings settings, IConfigHandler config, Planet planet) { }

	@Override
	public void updateCache(ClientSettings settings, IConfigHandler config, Planet object,
			ICelestialCoordinate coordinate, ISkyEffect sky, IViewScope scope, IOpticalFilter filter) {
		Vector3d ref = new Vector3d(object.earthPos);
		coordinate.getProjectionToGround().transform(ref);
		appCoord.setWithVec(ref);
		double airmass = sky.calculateAirmass(this.appCoord);
		sky.applyAtmRefraction(this.appCoord);

		this.appMag = (float) (object.currentMag + airmass * Optics.ext_coeff_V);
		this.shouldRender = true;
		if(this.appMag > settings.mag_Limit)
		{
			this.shouldRender = false;
			return;
		}
		
		if(appCoord.y < 0.0
				&& sky instanceof IStellarSkySet
				&& ((IStellarSkySet) sky).hideObjectsUnderHorizon())
			this.shouldRender = false;
		
		this.size = (float) (scope.getResolution(Wavelength.visible) / 0.3);
		this.multiplier = (float)(scope.getLGP() / (this.size * this.size * scope.getMP() * scope.getMP()));
		this.size *= 0.6f;
		this.color = FilterHelper.getFilteredRGBBounded(filter, new double[] {1.0, 1.0, 1.0});
	}

	@Override
	public int getRenderId() {
		return LayerSolarSystem.planetRenderId;
	}

}
