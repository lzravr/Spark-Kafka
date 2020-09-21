from time import sleep
from json import dumps
import json
from kafka import KafkaProducer
from essential_generators import DocumentGenerator

gen = DocumentGenerator()

producer = KafkaProducer(bootstrap_servers=['localhost:9092'],
                         value_serializer=lambda x: 
                         dumps(x).encode('utf-8'))

while 1:
    sentence = gen.sentence()
    data = {'text' : sentence}
    producer.send('input', value=data)
#    sleep(5)
