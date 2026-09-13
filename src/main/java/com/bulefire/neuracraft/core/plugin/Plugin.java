package com.bulefire.neuracraft.core.plugin;

import com.bulefire.neuracraft.core.agent.annotation.Agent;
import com.bulefire.neuracraft.core.agent.annotation.RegisterAgent;

import java.lang.annotation.*;

/**
 * 注册一个可以被识别的插件.
 * 此注解默认插件主类有一个空的构造函数.
 * <pre>
 *     {@code
 *          public className(){
 *              // do something init there...
 *          }
 *     }
 * </pre>
 * 插件的初始化逻辑放在 {@code 空构造函数} 中。
 *
 * @author bulefire_fox
 * @version 1.0
 * @see Agent
 * @see RegisterAgent
 * @since 2.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Plugin {
    String value();
}
