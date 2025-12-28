package com.bulefire.neuracraft.core.plugin;

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
 * @author bulefire_fox
 * @since 2.0
 * @version 1.0
 * @see com.bulefire.neuracraft.core.annotation.Agent
 * @see com.bulefire.neuracraft.core.annotation.RegisterAgent
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Plugin {
    String value();
}
