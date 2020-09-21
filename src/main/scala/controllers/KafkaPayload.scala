package controllers

case class KafkaPayload(key: Option[Array[Byte]], value: Array[Byte])
