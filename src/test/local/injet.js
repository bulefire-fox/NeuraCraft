const fs = require('fs');
const path = require('path');

// 读取 system prompt 内容
const promptPath = path.resolve(__dirname, './system-prompt.md');
const systemPromptContent = fs.readFileSync(promptPath, 'utf8');

// 读取 HTTP 文件内容
const httpFilePath = path.resolve(__dirname, './deepseek.http');
const httpFileContent = fs.readFileSync(httpFilePath, 'utf8');

// 找到 JSON 主体部分 - 从第一个 { 开始到最后一个 } 结束
const firstBraceIndex = httpFileContent.indexOf('{');
const lastBraceIndex = httpFileContent.lastIndexOf('}');
const jsonPart = httpFileContent.substring(firstBraceIndex, lastBraceIndex + 1);
const beforeJson = httpFileContent.substring(0, firstBraceIndex);
const afterJson = httpFileContent.substring(lastBraceIndex + 1);

// 解析 JSON 主体
const requestBody = JSON.parse(jsonPart);

// 找到 system 角色的消息并替换其 content
for (const message of requestBody.messages) {
    if (message.role === 'system') {
        message.content = systemPromptContent;  // 直接替换 content 字段
        break;
    }
}

// 重新构建完整的请求内容
const updatedHttpContent = beforeJson + JSON.stringify(requestBody, null, 2) + afterJson;

// 写回文件
fs.writeFileSync(httpFilePath, updatedHttpContent, 'utf8');
