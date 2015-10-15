package stellarium;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import stellarium.config.IConfigHandler;
import stellarium.stellars.StellarManager;

public class ClientProxy extends CommonProxy implements IProxy {
	
	private static final String clientConfigCategory = "clientconfig";
	
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		manager = new StellarManager(Side.CLIENT);
		
        this.setupConfigManager(event.getSuggestedConfigurationFile());
        
		MinecraftForge.EVENT_BUS.register(new StellarClientHook());
		FMLCommonHandler.instance().bus().register(new StellarKeyHook());
	}

	@Override
	public void load(FMLInitializationEvent event) throws IOException {
		super.load(event);
		
		manager.InitializeStars();
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
	}
	
	@Override
	public void setupConfigManager(File file) {
		super.setupConfigManager(file);
		cfgManager.register(clientConfigCategory, new IConfigHandler() {
			
			Property Mag_Limit, turb, Moon_Frac, minuteLength, hourToMinute;
			
			@Override
			public void setupConfig(Configuration config, String category) {
		        config.setCategoryComment(category, "Configurations for client modifications.\n"
		        		+ "Most of them are for rendering/view.");
		        config.setCategoryLanguageKey(category, "config.category.client");
		        config.setCategoryRequiresMcRestart(category, false);
				
		        Mag_Limit=config.get(category, "Mag_Limit", 5.0);
		        Mag_Limit.comment="Limit of magnitude can be seen on naked eye.\n" +
		        		"If you want to increase FPS, you can set this property a bit little (e.g. 0.3)\n" +
		        		"and FPS will be exponentially improved";
		        Mag_Limit.setRequiresMcRestart(false);
		        Mag_Limit.setLanguageKey("config.property.client.maglimit");

		        turb=config.get(category, "Twinkling(Turbulance)", 0.3);
		        turb.comment="Degree of the twinkling effect of star.\n"
		        		+ "It determines the turbulance of atmosphere, which actually cause the twinkling effect";
		        turb.setRequiresMcRestart(false);
		        turb.setLanguageKey("config.property.client.turbulance");
		        
		        Moon_Frac=config.get(category, "Moon_Fragments_Number", 16);
		        Moon_Frac.comment="Moon is drawn with fragments\n" +
		        		"Less fragments will increase FPS, but the moon become more defective";
		        Moon_Frac.setRequiresMcRestart(false);
		        Moon_Frac.setLanguageKey("config.property.client.moonfrac");
		        
		        minuteLength = config.get(category, "Minute_Length", 20.0);
		        minuteLength.comment = "Length of minute in tick. (The minute & hour is displayed on HUD as HH:MM format)";
		        minuteLength.setRequiresMcRestart(false);
		        minuteLength.setLanguageKey("config.property.client.minutelength");
		        
		        hourToMinute = config.get(category, "Hour_Length", 60);
		        hourToMinute.comment = "Length of hour in minute. (The minute & hour is displayed on HUD as HH:MM format)";
		        hourToMinute.setRequiresMcRestart(false);
		        hourToMinute.setLanguageKey("config.property.client.hourlength");
		        
		        List<String> propNameList = Arrays.asList(Mag_Limit.getName(),
		        		Moon_Frac.getName(), turb.getName(), minuteLength.getName(),
		        		hourToMinute.getName());
		        config.setCategoryPropertyOrder(category, propNameList);
			}

			@Override
			public void loadFromConfig(Configuration config, String category) {
		        manager.Mag_Limit=(float)Mag_Limit.getDouble();
		        manager.Turb=(float)turb.getDouble();
		        manager.ImgFrac=Moon_Frac.getInt();
		        manager.minuteLength = minuteLength.getDouble();
		        manager.anHourToMinute = hourToMinute.getInt();
			}
			
		});
	}
	
	@Override
	public World getDefWorld() {
		return Minecraft.getMinecraft().theWorld;
	}
}
