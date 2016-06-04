package stellarium.render.stellars.phased;

import net.minecraft.world.World;
import stellarapi.api.ISkyEffect;
import stellarapi.api.lib.math.SpCoord;
import stellarapi.api.lib.math.Spmath;
import stellarapi.api.lib.math.Vector3;
import stellarapi.api.optics.Wavelength;
import stellarium.render.stellars.access.IStellarChecker;
import stellarium.stellars.OpticsHelper;
import stellarium.view.ViewerInfo;

public class StellarChecker implements IStellarChecker {
	
	private static final float DEFAULT_SIZE = Spmath.Radians(0.3f);
	
	private float leastBrightnessRendered;
	private static final float leastBrightnessDominator = 1.0e-7f;
	
	private double multiplyingPower;
	private Vector3 colorMultiplier;
	private Vector3 resolutionColor;
	private ISkyEffect sky;

	private SpCoord cache = new SpCoord();
	private float red, green, blue;
	private float radius;

	public void setView(World world, ViewerInfo info) {
		this.multiplyingPower = info.multiplyingPower;
		this.colorMultiplier = info.colorMultiplier;
		this.resolutionColor = info.resolutionColor;
		this.sky = info.sky;
	}

	public void setMagLimit(float magLimit) {
		this.leastBrightnessRendered = OpticsHelper.getBrightnessFromMagnitude(magLimit);
	}

	@Override
	public void startDescription() {
		this.red = this.green = this.blue = 0;
		this.radius = 0.0f;
	}

	@Override
	public void radius(float radius) {
		this.radius = radius;
	}

	@Override
	public void brightness(float red, float green, float blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	@Override
	public boolean endCheckDominator() {
		if(Math.max(this.red * colorMultiplier.getX(),
				Math.max(this.green * colorMultiplier.getY(),
						this.blue * colorMultiplier.getZ()))
				> leastBrightnessDominator) {
			
			return true;
		} else return false;
	}

	@Override
	public boolean endCheckRendered() {
		if(Math.max(this.red * colorMultiplier.getX() / Spmath.sqr(multiplyingPower * (this.radius + resolutionColor.getX())),
				Math.max(this.green * colorMultiplier.getY() / Spmath.sqr(multiplyingPower * (this.radius + resolutionColor.getY())),
						this.blue * colorMultiplier.getZ() / Spmath.sqr(multiplyingPower * (this.radius + resolutionColor.getZ()))))
				> leastBrightnessRendered / Spmath.sqr(DEFAULT_SIZE)) {
			return true;
		} else return false;
	}
}
