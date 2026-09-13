# NeuraCraft 框架简介
### 关于 NeuraCraft 框架
```NeuraCraft 框架```(以下简称NC) 是一个 ```mcmod lib```, 
旨在让 ```AI``` 接入 ```Minecraft``` 更加简便。注意: 这不是一个用于 ```训练``` 或 ```开发``` AI的框架,
他是一个提供 ```AI``` 与 ```MC``` __标准交互__ 的框架. 我们希望NC能为开发者打包繁琐的底层交互逻辑, 
让开发者将精力放在开发 __AI应用__ 而不是与 ```modloader``` 和 ```MC``` 打架. 
我们会提供包括 __插件__ 和 __子模组__ 在内的加载功能, 让开发者自己选择合适的打包方式. 

### 如何使用 NC
#### 插件项目
正常创建一个 ```gradle``` 或 ```maven``` 项目，添加 ```NeuraCraft``` 依赖，
并配置为 ```compileOnly``` . 此外，还需要 ```log4j``` 依赖，例如：
```kotlin
    compileOnly(files("libs/neuracraft-2.0.jar"))
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
```
注意：这里 ```NeuraCraft``` 是从文件导入的，因为我们还未将 NC 上传至 maven仓库

#### 子模组项目
创建一个 你喜欢的 ```modloader``` 的 ```mod``` 项目，添加 ```NeuraCraft``` 依赖，并配置为 ```compileOnly``` .
不需要额外添加 ```log4j``` 依赖， 因为大部分 ```mod``` 的项目都已经内置了 ```log4j``` 的依赖。
例如：
```kotlin
    compileOnly(files("libs/neuracraft-2.0.jar"))
```
注意同上。

关于NC项目在代码和开发中的使用，请参阅[开始使用NC](./开始使用NC.md)