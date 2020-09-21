package ex.text.spark

case class KafkaPayload(key: Option[Array[Byte]], value: Array[Byte])
