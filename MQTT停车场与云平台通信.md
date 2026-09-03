# MQTT 停车场 -> 云平台

```text
云平台MQTT连接地址: tcp://127.0.0.1:1883
云平台MQTT用户名: admin
云平台MQTT密码: 123456
云平台MQTT clientId: aaa

事件标识符：
    properties: 属性
    online: 上线/下线
```

## 上报 设备上线/下线

主题: /server/parking/${设备编码}/thing/event/online/post

```json
{
  "id": "10002",
  "version": "1.0",
  "sys": {
    "ack": 0,
    "deviceCode": "边设备Code",
    "childCode": "子设备Code"
  },
  "method": "thing.event.online.post",
  "params": {
    "online": true
  }
}
```

## 上报 车牌识别

主题: /server/parking/${设备编码}/thing/event/property/post

报文 Payload

```json
{
  "id": "10002",
  "version": "1.0",
  "sys": {
    "ack": 0,
    "deviceCode": "边设备Code",
    "childCode": "子设备Code"
  },
  "method": "thing.event.property.post",
  "params": {
    "deviceCode": "ABCDEFG001",
    "carNumber": "粤C123456",
    "carType": "小型车"
  }
}
```

## 下发 开闸/关闸

主题:

报文 Payload

```json

```







