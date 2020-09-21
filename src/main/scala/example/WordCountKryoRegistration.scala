package example

import com.esotericsoftware.kryo.Kryo
import org.apache.spark.serializer.KryoRegistrator
import ex.text.spark.KafkaPayloadStringCodec

class WordCountKryoRegistration extends KryoRegistrator {

  override def registerClasses(kryo: Kryo): Unit = {
    kryo.register(classOf[KafkaPayloadStringCodec])

  }

}
