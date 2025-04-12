# Configuration File Writing
The configuration file for NeuraCraft is located in the `config/neuracraft-common.toml` file. <br>
Its content is as follows:
```toml
["银影AI 设置"]
	#API URL
	# The address of the API. Default is the official CuberFurry address. You can choose other third-party forwarding services (if available). 
	apiUrl = "https://api-yinying-ng.wingmark.cn/v1"
	#API 接口
	# The API interface. Default is /chatWithCyberFurry. Different third-party forwarders may use different interface addresses. 
	apiInterface = "/chatWithCyberFurry"
	#Token
	# Secret key provided by the API provider. Do not disclose. 
	token = "yinying-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
	#AppID
	#  Application ID provided by the API provider. Do not disclose.
	appid = "xxxxxxxxx"
	#Model
	# Model to be used. Default is yinyingllm-latest. You can choose any supported model. 
	model = "yinyingllm-latest"
	#System Prompt
	# The root-level system prompt for the entire model. Default is "Your name is Silver Shadow, a bio-mimetic gray wolf developed by Lingji Network." 
	systemPrompt = "你的名字叫银影，是翎迹网络开发的仿生灰狼"
	#显示名称
	# In-game display name. Default is "银影"
	showName = "银影"
	#保存聊天
	# Whether to save chat history. Default is true (save chat history).
	saveChat = true
	#次数 / min
	# Chat frequency limit. Default is 20 times per minute.
	times = 20

    # Variable Section
	["银影AI 设置".Variables]
		#昵称
		# Your nickname. Not recommended for use in multi-player environments but can enhance immersion in single-player environments.
    nickname = "没有哦"
		#设定
		# Character setup. Not recommended for use in multi-player environments but can enhance immersion in single-player environments. Default is "Not Available".
    furryCharacter = "没有哦"
		#微调提示词
		# Fine-tuning prompts for the model are placed here. The default prompt is designed for multi-player environments to help the model distinguish between each user. You can customize it as needed.
    promptPatch = "你在和一群人对话,每个人以 [id] 前缀区分,前缀只是用户的唯一标识符,你可以理解为用户id,你可以根据这个id来判断用户是否相同.前缀和用户的设定或者其他任何信息没有任何关系."
```

The configuration file content is roughly as shown above, and you can modify it according to your needs.

---
We currently utilize the CyberFurry API. Please refer to [Get started with NeuraCraft for CyberFurry API](../en_us.md) for details.
