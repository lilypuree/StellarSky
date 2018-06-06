package stellarium.world;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import stellarapi.api.ICelestialCoordinates;
import stellarapi.api.ICelestialHelper;
import stellarapi.api.ICelestialScene;
import stellarapi.api.ISkyEffect;
import stellarapi.api.SAPIReferences;
import stellarapi.api.celestials.ICelestialCollection;
import stellarapi.api.celestials.ICelestialObject;
import stellarapi.api.celestials.IEffectorType;
import stellarapi.api.lib.config.INBTConfig;
import stellarapi.api.render.IAdaptiveRenderer;
import stellarapi.api.world.worldset.WorldSet;
import stellarapi.example.CelestialHelper;
import stellarium.StellarSky;
import stellarium.stellars.StellarManager;
import stellarium.stellars.layer.StellarCollection;
import stellarium.stellars.layer.StellarObjectContainer;

public final class StellarScene implements ICelestialScene {
	private final StellarManager manager;
	private final World world;
	private final WorldSet worldSet;

	private PerDimensionSettings settings;
	private IStellarSkySet skyset;
	private StellarCoordinates coordinate;
	private List<StellarCollection> collections = Lists.newArrayList();
	private List<ICelestialObject> foundSuns = Lists.newArrayList();
	private List<ICelestialObject> foundMoons = Lists.newArrayList();

	@Deprecated
	public static StellarScene getScene(World world) {
		ICelestialScene scene = SAPIReferences.getActivePack(world);
		return (scene instanceof StellarScene)? (StellarScene) scene : null;
	}

	public StellarScene(World world, WorldSet worldSet, PerDimensionSettings settings) {
		this.world = world;
		this.worldSet = worldSet;
		this.manager = StellarManager.getManager(world);
		this.settings = settings;
	}

	public PerDimensionSettings getSettings() {
		return this.settings;
	}

	private void loadSettingsFromConfig() {
		this.settings = (PerDimensionSettings) ((INBTConfig) StellarSky.PROXY.getDimensionSettings().getSubConfig(worldSet.name)).copy();
	}


	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();

		// Writes Stellar Manager.
		// TODO Stellar API Separate networking code and serialization code
		nbt.setTag("main", StellarManager.getManager(this.world).serializeNBT());
		settings.writeToNBT(nbt);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		// When it's the default world and there's the manager nbt, read it.
		if(world.provider.getDimension() == 0 || world.isRemote) {
			if(nbt.hasKey("main", 10)) {
				manager.deserializeNBT(nbt.getCompoundTag("main"));
			}
		}

		if(world.isRemote) {
			manager.setup(StellarSky.PROXY.getClientCelestialManager().copyFromClient());
		}

		if(manager.isLocked() || world.isRemote) {
			this.settings = new PerDimensionSettings(this.worldSet);
			settings.readFromNBT(nbt);
		} else {
			this.loadSettingsFromConfig();
		}
	}

	public List<StellarCollection> getCollections() {
		return this.collections;
	}

	public List<ICelestialObject> getSuns() {
		return this.foundSuns;
	}
	
	public List<ICelestialObject> getMoons() {
		return this.foundMoons;
	}

	public void update(World world, long currentTick, long currentUniversalTick) {
		coordinate.update(manager.getSkyYear(currentTick));

		for(int i = 0; i < collections.size(); i++) {
			StellarCollection collection = collections.get(i);
			StellarObjectContainer container = manager.getCelestialManager().getLayers().get(i);
			container.updateCollection(collection, currentUniversalTick);
		}
	}


	@Override
	public void prepare() {
		collections.clear();
		foundSuns.clear();
		foundMoons.clear();

		String dimName = world.provider.getDimensionType().getName();
		StellarSky.INSTANCE.getLogger().info(String.format("Initializing Dimension Settings on Dimension %s...", dimName));
		if(settings.allowRefraction())
			this.skyset = new RefractiveSkySet(this.settings);
		else this.skyset = new NonRefractiveSkySet(this.settings);
		this.coordinate = new StellarCoordinates(manager.getSettings(), this.settings);
		coordinate.update(manager.getSkyYear(0.0));

		StellarSky.INSTANCE.getLogger().info(String.format("Initialized Dimension Settings on Dimension %s.", dimName));


		StellarSky.INSTANCE.getLogger().info("Evaluating Stellar Collections from Celestial State...");

		StellarSky.INSTANCE.getLogger().info("Starting Test Update.");
		manager.update(0.0);
		StellarSky.INSTANCE.getLogger().info("Test Update Ended.");
		
		for(StellarObjectContainer container : manager.getCelestialManager().getLayers()) {
			StellarCollection collection = new StellarCollection(container, this.coordinate, this.skyset,
					this.coordinate.getYearPeriod());
			container.addCollection(collection);
			collections.add(collection);
			
			foundSuns.addAll(collection.getSuns());
			foundMoons.addAll(collection.getMoons());
		}

		if(world.isRemote)
			StellarSky.PROXY.setupDimensionLoad(this);
		
		StellarSky.INSTANCE.getLogger().info("Evaluated Stellar Collections.");
	}

	@Override
	public void onRegisterCollection(Consumer<ICelestialCollection> colRegistry,
			BiConsumer<IEffectorType, ICelestialObject> effRegistry) {
		for(ICelestialCollection col : this.collections)
			colRegistry.accept(col);

		for(ICelestialObject sun : this.foundSuns)
			effRegistry.accept(IEffectorType.Light, sun);

		for(ICelestialObject moon : this.foundMoons)
			effRegistry.accept(IEffectorType.Tide, moon);
	}

	@Override
	public ICelestialCoordinates createCoordinates() {
		return this.coordinate;
	}

	@Override
	public ISkyEffect createSkyEffect() {
		return this.skyset;
	}

	@Override
	public ICelestialHelper createCelestialHelper() {
		if(this.getSettings().doesPatchProvider()) {
			return new CelestialHelper((float)this.getSettings().getSunlightMultiplier(), 1.0f,
					this.getSuns().get(0), this.getMoons().get(0), this.coordinate, this.skyset);
		} else return null;
	}

	@Override
	public IAdaptiveRenderer createSkyRenderer() {
		return StellarSky.PROXY.setupSkyRenderer(this.world, this.worldSet, settings.getSkyRendererType());
	}
}
