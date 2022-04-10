package com.snowalker.raft.core.leaderelection;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/7 16:33
 * @desc 角色类型
 */
public enum RoleType {

	/**
	 * FOLLOWER
	 */
	FOLLOWER(0, "跟随者"),
	/**
	 * LEADER
	 */
	LEADER(1, "领导者"),
	/**
	 * CANDIDATE
	 */
	CANDIDATE(2, "候选者");

	private final int id;
	private final String desc;

	RoleType(int id, String desc) {
		this.id = id;
		this.desc = desc;
	}
}
