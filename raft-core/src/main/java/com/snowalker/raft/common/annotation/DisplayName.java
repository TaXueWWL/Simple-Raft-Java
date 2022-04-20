package com.snowalker.raft.common.annotation;

import java.lang.annotation.*;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/4/10 19:26
 * @className
 * @desc
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DisplayName {

	String value();
}
