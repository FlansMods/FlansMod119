package com.flansmod.teams.api.runtime;

import com.flansmod.teams.api.admin.IControlPointRef;

public interface IControlPointInstance extends IControlPointRef
{
	int getCurrentTeamIndex();
	int getCaptureProgress();
	boolean getContested();
	boolean getFlagPresent();
	void setCurrentTeamIndex(int set);
	void setCaptureProgress(int set);
	void setContested(boolean set);
	void setFlagPresent(boolean set);
}
