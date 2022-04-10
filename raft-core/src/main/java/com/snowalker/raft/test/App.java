package com.snowalker.raft.test;

import lombok.extern.slf4j.Slf4j;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/8 12:10
 * @className
 * @desc
 */
@Slf4j
public class App {

	public static void main(String[] args) {
		System.out.println(System.getenv("raft.log.dir"));
		for (int i = 0; i < 1000; i++) {
			log.info("测试日志打印");
		}
	}
}
