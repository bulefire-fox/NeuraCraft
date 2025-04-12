# 配置文件编写
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

---
目前我们使用 CyberFurry API 请参阅 [开始使用 NeuraCraft for CyberFurry API](../zh_cn.md)