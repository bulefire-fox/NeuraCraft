# Get started with NeuraCraft for CyberFurry API

## How to Obtain the Token and AppID
The ```银影``` model for this mod is provided by [CyberFurry](https://chat.wingmark.cn/). You can try having a conversation with ```银影```on the website first.  
If you need to obtain the model's token and appid, you will need to apply through [CyberFurry](https://chat.wingmark.cn/). <br>
You can refer to the description below for the specific process. If you do not want to apply for the official API, you can also use our forwarding API. For more details, see:
<button onclick="location.href='#forward_api';">Use Our Forwarding API</button>

### how to obtain the token and appid is not importance for you, we suggest you use our forwarding api
Because our forwarding api is free and easy to use.
Registering a token and appid is extremely unfriendly to non-Chinese mainland users.
if you really register a token and appid, you can read the [chinese](zh_cn.md) version of the document

<a id="forward_api"></a>
## Use Our Forwarding API
If you want to use our provided forwarding API, you only need to replace the value of ```apiUrl```  in the configuration file with ```https://neuracraft.bulefire.top:39132/v1```.
```toml
    #API URL
    apiUrl = "https://neuracraft.bulefire.top:39132/v1"
```
No other options need to be changed.  
Note that our forwarding service **currently** does not require```token```or```appId```,, but we plan to add independent authentication measures in a future version to distinguish users.
