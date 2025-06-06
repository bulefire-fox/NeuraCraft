# Getting Started with NeuraCraft for CyberFurry API

## How to Obtain Token and AppID
The `Ying Ying` model of this mod is provided by [CyberFurry](https://chat.wingmark.cn/). You can try interacting with `Ying Ying` on the website first.
To get the model's token and appid, you need to apply through [CyberFurry](https://chat.wingmark.cn/).
For specific steps, please refer to the instructions below.

### 1. Register an Account
Visit the [Registration Page](https://chat.wingmark.cn/?page=register), fill in the required information, and register. Note that a QQ number is required; if you don't have one, please find alternative ways to register.

[![Registration Page](../../../img/docs/register.png)](https://chat.wingmark.cn/?page=register)

### 2. Verify Your Account
Join the [QQ Discussion Group](https://qm.qq.com/q/cxLuILvN3G) and ensure the QQ number used to join matches the registered QQ number. Please wait patiently for the administrator to review your application.
If you're in a hurry, you may contact the administrator to expedite the verification process.

[![QQ Discussion Group](../../../img/docs/qq.png)](https://qm.qq.com/q/cxLuILvN3G)

### 3. Apply for [token](file://E:\files\project\NeuraCraft\src\main\java\com\bulefire\neuracraft\config\yy\BaseInformation.java#L60-L60) and [appid](file://E:\files\project\NeuraCraft\src\main\java\com\bulefire\neuracraft\config\yy\BaseInformation.java#L61-L61)
Contact the group owner `秩乱 (Zhì Luàn)` and send a message like "apply for api token."

[![Apply for Token](../../../img/docs/application_api_token.png)](https://chat.wingmark.cn/?page=apply)

If asked questions such as about the `usage scenario` or `project scale`, you can respond with something like integrating a `chatbot` or `already having a project`.

![Questions and Answers](../../../img/docs/question.png)

Next, you will need to fill in some information.

![Fill in Information](../../../img/docs/request.png)
Reference example:
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
Reference translation:
```text
Please prepare a document based on the following information and send it to me (TXT or Word format is acceptable):
In-game chatbot
NeuraCraft user
In-game bot
None
Adding AI into the game to provide users with a more immersive gaming experience
Minecraft player
//Fill this according to your server situation
//Fill this according to your server situation
No website or app
```
Then wait for the review.

![Review Process](../../../img/docs/Audit.png)

Carefully read the agreement and developer guidelines, then reply accordingly.

![Developer Guidelines and Agreement](../../../img/docs/agreement.png)
![Reply](../../../img/docs/replay.png)

After that, you will receive the [token](file://E:\files\project\NeuraCraft\src\main\java\com\bulefire\neuracraft\config\yy\BaseInformation.java#L60-L60) and [appid](file://E:\files\project\NeuraCraft\src\main\java\com\bulefire\neuracraft\config\yy\BaseInformation.java#L61-L61).

![Information Received](../../../img/docs/infomation.png)

Enter the `AppID` and `bearer key` into the [neuracraft-common.toml](file://E:\files\project\NeuraCraft\run\config\neuracraft-common.toml) file to start using them.

translation by QWEN3.
if you have any questions, please contact us.