package stellarium.stellars.milkyway;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import stellarapi.api.lib.math.Matrix3;
import stellarapi.api.lib.math.SpCoord;
import stellarapi.api.lib.math.Vector3;
import stellarapi.api.optics.EyeDetector;
import stellarium.client.ClientSettings;
import stellarium.render.stellars.access.IStellarChecker;
import stellarium.render.stellars.layer.IObjRenderCache;
import stellarium.stellars.OpticsHelper;
import stellarium.stellars.render.ICelestialObjectRenderer;
import stellarium.util.math.Allocator;
import stellarium.view.ViewerInfo;

public class MilkywayRenderCache implements IObjRenderCache<Milkyway, MilkywayImage, MilkywaySettings> {
	
	//Zero-time axial tilt
	public static final double e=0.4090926;
	public static final Matrix3 EqtoEc = new Matrix3();
	
	static {
		EqtoEc.setAsRotation(1.0, 0.0, 0.0, -e);
	}
	
	protected SpCoord[][] milkywaypos = null;
	protected int latn, longn;
	protected double[] color = new double[3];
	protected float milkywayAbsBr;
	protected boolean rendered;

	@Override
	public void updateSettings(ClientSettings settings, MilkywaySettings specificSettings,  Milkyway dummy) {
		this.latn = specificSettings.imgFracMilkyway;
		this.longn = 2*specificSettings.imgFracMilkyway;
		this.milkywaypos = Allocator.createAndInitializeSp(longn, latn+1);
		this.milkywayAbsBr = specificSettings.milkywayBrightness * OpticsHelper.getBrightnessFromMagnitude(4.0f);
	}

	@Override
	public void updateCache(Milkyway object, MilkywayImage image, ViewerInfo info, IStellarChecker checker) {
		checker.startDescription();
		checker.brightness(this.milkywayAbsBr, this.milkywayAbsBr, this.milkywayAbsBr);
		if(!checker.endCheckRendered()) {
			this.rendered = false;
			return;
		}
		
		for(int longc=0; longc<longn; longc++){
			for(int latc=0; latc<=latn; latc++){
				Vector3 Buf = new SpCoord(longc*360.0/longn + 90.0, latc*180.0/latn - 90.0).getVec();
				EqtoEc.transform(Buf);
				info.coordinate.getProjectionToGround().transform(Buf);

				SpCoord coord = new SpCoord();
				coord.setWithVec(Buf);

				info.sky.applyAtmRefraction(coord);

				milkywaypos[longc][latc].x = coord.x;
				milkywaypos[longc][latc].y = coord.y;
			}
		}
		
		this.rendered = true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ICelestialObjectRenderer getRenderer() {
		return MilkywayRenderer.INSTANCE;
	}

}
