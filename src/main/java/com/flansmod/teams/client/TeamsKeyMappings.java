package com.flansmod.teams.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public class TeamsKeyMappings
{
	public static final Lazy<KeyMapping> TEAMS_MENU_MAPPING = Lazy.of(() -> new KeyMapping(
		"key.flansteams.teams_menu",
		KeyConflictContext.IN_GAME,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_V,
		"key.categories.teamsmod"));
}
