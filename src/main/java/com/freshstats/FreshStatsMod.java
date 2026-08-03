package com.freshstats;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FreshStatsMod implements ModInitializer {
    public static final String MOD_ID = "freshstats";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("FreshStats initialized!");
    }
}
