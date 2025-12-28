package com.bulefire.neuracraft.core.annotation;

import java.lang.annotation.*;

/**
 * 注册一个 {@link com.bulefire.neuracraft.core.Agent}. <br>
 * 此注解会默认类实现了一个 {@code init}
 * <pre>
 *     {@code
 *          public static void init(){
 *              //do something there...
 *          }
 *     }
 * </pre>
 * 方法, 并尝试调用. 如果类没有实现 {@code init} 方法, 则什么也不做. 并认为类会在 {@code static代码块}
 * <pre>
 *     {@code
 *        static{
 *            //do something there...
 *        }
 *     }
 * </pre>
 * 中实现初始化逻辑.
 * 注册方法请使用 {@link RegisterAgent}
 * @author bulefire_fox
 * @since 2.0
 * @version 1.0
 * @see com.bulefire.neuracraft.core.Agent
 * @see com.bulefire.neuracraft.core.annotation.RegisterAgent
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Agent {

}
