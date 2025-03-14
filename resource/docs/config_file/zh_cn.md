# 开始使用 NeuraCraft

## 配置文件编写
NeuraCraft 的配置文件位于 `config/neuracraft-common.toml` 文件中。 <br>
其内容为
```toml
["银影AI 设置"]
	#API URL
	# API 的地址.默认为 CuberFurry 官方地址. 你可以选择其他的第三方转发服务(如果有的话)
	apiUrl = "https://api-yinying-ng.wingmark.cn/v1"
	#API 接口
	# API 的接口.默认为 /chatWithCyberFurry,不同的第三方转发可能会使用不同的接口地址
	apiInterface = "/chatWithCyberFurry"
	#Token
	# 密钥. 密钥由 API 提供者提供，请勿泄露
	token = "yinying-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
	#AppID
	# 应用 ID. ID 由 API 提供者提供，请勿泄露
	appid = "xxxxxxxxx"
	#Model
	# 模型. 默认为 yinyingllm-latest, 你可以选择任何支持调用的模型
	model = "yinyingllm-latest"
	#System Prompt
	# 系统级提示词,是整个模型的根基提示词. 默认为 "你的名字叫银影，是翎迹网络开发的仿生灰狼"
	systemPrompt = "你的名字叫银影，是翎迹网络开发的仿生灰狼"
	#显示名称
	# 游戏内的显示名称. 默认为 "银影"
	showName = "银影"
	#保存聊天
	# 是否保存聊天记录. 默认为 true 及保存聊天记录
	saveChat = true
	#次数 / min
	# 聊天次数限制. 默认为 20 次/分钟
	times = 20

    # 变量部分
	["银影AI 设置".Variables]
		#昵称
		# 你的昵称, 在多人环境下不建议使用,可以在单人环境下增加沉浸感.
		nickname = "没有哦"
		#设定
		# 角色设定,在多人环境下不建议使用,可以在单人环境下增加沉浸感. 默认为 "没有哦"
		furryCharacter = "没有哦"
		#微调提示词
		# 微调提示词,对模型进行微调的提示词全部放在此处. 默认提示词是为了在多人环境下让模型区分每位用户而设计的,你可以自行微调.
		promptPatch = "你在和一群人对话,每个人以 [id] 前缀区分,前缀只是用户的唯一标识符,你可以理解为用户id,你可以根据这个id来判断用户是否相同.前缀和用户的设定或者其他任何信息没有任何关系."
```

配置文件的大致内容如上所示, 你可以根据你的需求进行修改。

## 如何获取 token 和 appid
此mod的```银影```模型由 [CyberFurry](https://chat.wingmark.cn/) 提供,你可以尝试在网页上先尝试与```银影```进行对话,
如要获取模型的 token 和 appid 则需要像 [CyberFurry](https://chat.wingmark.cn/) 申请. <br>
具体流程可以参考下面的叙述,如果你不像申请官方的api,也可以使用我们提供的转发api,具体参阅
<button onclick="location.href='#forward_api';">使用我们提供的转发接口</button>

### 1.注册账号
访问[注册页面](https://chat.wingmark.cn/?page=register)填写信息并注册. 注意需要QQ号,没有的请自行寻找办法注册

[![注册页面](../../img/docs/register.png)](https://chat.wingmark.cn/?page=register)

### 2.验证账号
访问[qq交流群](https://qm.qq.com/q/cxLuILvN3G)并加入群聊,确保加群qq号和注册qq号一致.并耐心等待管理员审核.
如等不及可以尝试联系管理员加速验证

[![qq交流群](../../img/docs/qq.png)](https://qm.qq.com/q/cxLuILvN3G)

### 3.申请 ```token``` 和 ```appid```
联系群主 ```秩乱``` 并发送类似于 ```申请api token之类的字眼```

[![申请token](../../img/docs/application_api_token.png)](https://chat.wingmark.cn/?page=apply)

如果询问类似于 ```使用场景``` 或者 ```项目规模``` 之类的问题, 可以回答接入 ```机器人``` , ```已经有项目``` 之类的回答.

![问题及回答](../../img/docs/question.png)

接下来需要填写一些信息

![填写信息](../../img/docs/request.png)
参考填写

```text
需要填点东西，根据以下信息整理一个文档然后发给我（txt或word皆可）：
1. 游戏内聊天机器人
2. NeuraCraft 用户
3. 游戏内机器人
4. 无
5. 将ai添加到游戏内,给用户更沉浸的游戏体验
6. mc 玩家
7. //这条看服务器情况自行填写
8. //这条看服务器情况自行填写
9. 没有网站或app
```

然后等待审核

![审核](../../img/docs/Audit.png)

接下来仔细阅读协议和开发者须知并回复

![开发者须知和协议](../../img/docs/agreement.png)
![回复](../../img/docs/replay.png)

随后就会得到 ```token``` 和 ```appid```

![信息](../../img/docs/infomation.png)

将 ```AppID``` 和 ```bearer key``` 填入 ```neuracraft-common.toml``` 文件中即可正常使用

<a id="forward_api"></a>
## 使用我们提供的转发接口
我们目前正在开发转发接口,他在 ```2025.3.16``` 就会正式上线

### 稍安勿躁