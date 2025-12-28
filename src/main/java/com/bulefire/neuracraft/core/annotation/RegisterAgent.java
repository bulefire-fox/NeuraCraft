package com.bulefire.neuracraft.core.annotation;

import java.lang.annotation.*;

/**
 * 注册一个 {@link com.bulefire.neuracraft.core.Agent} 的初始化方法.
 * 名字任意, 但必须为 {@code public static void} 的 {@code 空参数} 的方法.
 * <pre>
 *     {@code
 *          @RegisterAgent
 *          public static void anyName(){
 *              // do something there...
 *          }
 *     }
 * </pre>
 * 注册类请使用 {@link com.bulefire.neuracraft.core.annotation.Agent}
 *
 * @author bulefire_fox
 * @version 1.0
 * @see com.bulefire.neuracraft.core.Agent
 * @see com.bulefire.neuracraft.core.annotation.Agent
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RegisterAgent {

}
