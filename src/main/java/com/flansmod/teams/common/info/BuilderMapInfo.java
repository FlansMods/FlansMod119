package com.flansmod.teams.common.info;

import javax.annotation.Nonnull;

public record BuilderMapInfo(@Nonnull String mapID,
							 boolean isConstruct,
							 int numPlayers)
{
}
