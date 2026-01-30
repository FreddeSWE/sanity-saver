package com.sanitysaver;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SanitySaverPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SanitySaverPlugin.class);
		RuneLite.main(args);
	}
}