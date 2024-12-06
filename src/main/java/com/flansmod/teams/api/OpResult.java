package com.flansmod.teams.api;

public enum OpResult
{
	SUCCESS,

	FAILURE_GENERIC,

	FAILURE_MAP_NOT_FOUND,
	FAILURE_INVALID_MAP_NAME,
	FAILURE_INVALID_MAP_INDEX,
	FAILURE_WRONG_PHASE,
	FAILURE_DUPLICATE_ID,
	FAILURE_INCORRECT_TEAMS

	;


	public boolean success() { return this == SUCCESS; }
	public boolean failure() { return this != SUCCESS; }

}
