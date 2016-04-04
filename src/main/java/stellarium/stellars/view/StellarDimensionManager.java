package stellarium.stellars.view;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import sciapi.api.value.IValRef;
import sciapi.api.value.euclidian.EVector;
import stellarium.StellarSky;
import stellarium.config.INBTConfig;
import stellarium.stellars.StellarManager;
import stellarium.util.math.SpCoord;

public final class StellarDimensionManager extends WorldSavedData {
	
	public static String ID = "stellarskydimension%s";
	
	private StellarManager manager;
	
	private PerDimensionSettings settings;
	private IStellarViewpoint viewpoint;
	
	private String dimensionName;
	
	public static StellarDimensionManager loadOrCreate(World world, StellarManager manager, String dimName) {
		WorldSavedData data = world.perWorldStorage.loadData(StellarDimensionManager.class, ID);
		StellarDimensionManager dimManager;
		
		if(!(data instanceof StellarDimensionManager))
		{
			dimManager = new StellarDimensionManager(String.format(ID, dimName));
			world.perWorldStorage.setData(String.format(ID, dimName), dimManager);
			
			dimManager.loadSettingsFromConfig();
		} else
			dimManager = (StellarDimensionManager) data;

		dimManager.manager = manager;
		
		return dimManager;
	}

	public static StellarDimensionManager get(World world) {
		WorldSavedData data = world.perWorldStorage.loadData(StellarDimensionManager.class,
				String.format(ID, world.provider.getDimensionName()));
		
		if(!(data instanceof StellarDimensionManager))
			return null;
		
		return (StellarDimensionManager)data;
	}

	public StellarDimensionManager(String id) {
		super(id);
		this.dimensionName = id.replaceFirst("stellarskydimension", "");
	}
	
	public PerDimensionSettings getSettings() {
		return this.settings;
	}
	
	public IStellarViewpoint getViewpoint() {
		return this.viewpoint;
	}
	
	private void loadSettingsFromConfig() {
		this.settings = (PerDimensionSettings) ((INBTConfig) StellarSky.proxy.dimensionSettings.getSubConfig(this.dimensionName)).copy();
		this.markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.syncFromNBT(compound, false);
	}
	
	public void syncFromNBT(NBTTagCompound compound, boolean isRemote) {
		if(StellarManager.getManager(isRemote).isLocked() || isRemote)
		{
			this.settings = new PerDimensionSettings(this.dimensionName);
			settings.readFromNBT(compound);
		} else {
			this.loadSettingsFromConfig();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		settings.writeToNBT(compound);
	}
	
	public void setup() {
		StellarSky.logger.info("Initializing Dimension Settings...");
		if(settings.allowRefraction)
			this.viewpoint = new RefractiveViewpoint(manager.getSettings(), this.settings);
		else this.viewpoint = new NonRefractiveViewpoint(manager.getSettings(), this.settings);
		StellarSky.logger.info("Initialized Dimension Settings.");
	}
	
	public void update(World world, double currentTick) {
		double skyTime = manager.getSkyTime(currentTick);
		viewpoint.update(world, skyTime / manager.getSettings().day / manager.getSettings().year);
	
		EVector sunPos = manager.getCelestialManager().getSunEcRPos();
		sunEquatorPos.set(viewpoint.projectionToEquatorial().transform(sunPos));
		sunAppPos.setWithVec(viewpoint.getProjection().transform(sunPos));
		viewpoint.applyAtmRefraction(this.sunAppPos);
		
		EVector moonPos = manager.getCelestialManager().getMoonEcRPos();
		moonEquatorPos.set(viewpoint.projectionToEquatorial().transform(moonPos));
		moonAppPos.setWithVec(viewpoint.getProjection().transform(moonPos));
		viewpoint.applyAtmRefraction(this.moonAppPos);
		
		moonFactors = manager.getCelestialManager().getMoonFactors();
	}
	
	public SpCoord sunAppPos = new SpCoord(), moonAppPos = new SpCoord();
	public EVector sunEquatorPos = new EVector(3), moonEquatorPos = new EVector(3);
	public double[] moonFactors = new double[3];

}