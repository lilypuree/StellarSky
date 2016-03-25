package stellarium;

import java.io.File;
import java.io.IOException;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import stellarium.client.ClientSettings;
import stellarium.common.CommonSettings;
import stellarium.config.ConfigManager;
import stellarium.sleepwake.AlarmWakeHandler;
import stellarium.sleepwake.LightWakeHandler;
import stellarium.sleepwake.SleepWakeManager;
import stellarium.util.math.Spmath;

public class CommonProxy implements IProxy {

	protected Configuration config;
	protected ConfigManager cfgManager;
	public CommonSettings commonSettings = new CommonSettings();
	public SleepWakeManager wakeManager = new SleepWakeManager();
	
	private static final String serverConfigCategory = "serverconfig";
	private static final String serverConfigWakeCategory = "serverconfig.wake";
	
	@Override
	public void preInit(FMLPreInitializationEvent event) {		
		this.setupConfigManager(event.getSuggestedConfigurationFile());
	}

	@Override
	public void load(FMLInitializationEvent event) throws IOException {
        cfgManager.syncFromFile();
        
		System.out.println("[Stellarium]: "+"Initializing Math class...");
		//Initializing Spmath
		Spmath.Initialize();
		System.out.println("[Stellarium]: "+"Math Class Initialized!");	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		
	}
	
	public void setupConfigManager(File file) {
		config = new Configuration(file);
        cfgManager = new ConfigManager(config);
        
        cfgManager.register(serverConfigCategory, this.commonSettings);
        
        cfgManager.register(serverConfigWakeCategory, this.wakeManager);
        wakeManager.register("wakeByBright", new LightWakeHandler(), true);
        wakeManager.register("wakeByAlarm", new AlarmWakeHandler(), false);
	}
	
	public ConfigManager getCfgManager() {
		return this.cfgManager;
	}
	
	@Override
	public World getDefWorld() {
		return FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
	}
	
	@Override
    public World getDefWorld(boolean isRemote) {
    	return FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
    }

	
	@Override
	public ClientSettings getClientSettings() {
		return null;
	}

	public Configuration getConfig() {
		return this.config;
	}

	public void addScheduledTask(Runnable runnable) {
		FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnable);
	}
}
