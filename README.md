# Apache Spark i Apache Kafka

Želimo da napravimo sistem takav da Spark radi sa Kafkom i na ulazu i na izlazu. To znači da želimo da
Spark (Spark Streaming) čita podatke iz Kafke, odnosno teme, obrađuje ih i tako obrađene podatke (rezultate)
prosledi u novu temu. Odnosno, postoji klijent prizvođač koji dodaje događaje u ulaznu temu, potrošač koji
se nalazi u našem sistemu koji čita događaje iz ulazne teme, obrađuje ih i kao proivođač upisuje rezultate u
izlaznu temu. Sa spoljnih strane imamo strane proizvođače i potrošače dok sa unutrašnje, sistem ima svoje.

## Priprema infrastrukture

Pre nego što krenemo treba da pripremimo mašinu za rad. Podrazumevamo da je operativni sistem na kome radimo Linux. Biće nam potrebni:

- Java JDK;
- Scala;
- sbt;
- Python (opciono);
- Spark;
- Kafka.

```sh
sudo apt update
# Java
sudo apt install openjdk-8-jdk
java -version

# Scala i sbt
echo "deb https://dl.bintray.com/sbt/debian /" 
| sudo tee -a /etc/apt/sources.list.d/sbt.list
curl -sL 
   "https://keyserver.ubuntu.com/pks/lookup?op=get&search=
   0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" 
   | sudo apt-key add
sudo apt update
sudo apt install sbt
sbt --version
scala -version

# Python
sudo apt install software-properties-common
sudo apt install python3.8
python3 -V

# Spark
wget https://downloads.apache.org/spark/spark-3.0.1/spark-3.0.1-bin-hadoop2.7.tgz
tar xvf spark-*
sudo mv spark-* /opt/spark

# Kafka
wget https://downloads.apache.org/kafka/2.6.0/kafka-2.6.0-src.tgz
tar xvf kafka-*
sudo mv kafka-* /opt/kafka
```

Za razvoj je moguće koristiti bilo koji editor ili IDE. IDE koji je korišćen je IntelliJ IDEA Community.

## Pokretanje

Kafku smo smestili u /opt direktorijum. Kako bismo testirali prebacićemo se u direktorijum /opt/kafka.

```sh
cd /opt/kafka
```

1. Pokrenuti *Zookeeper* server:
```sh
./bin/zookeeper-server-start.sh config/zookeeper.properties
```

2. Pokrenuti Kafka server:
```sh
./bin/kafka-server-start.sh config/server.properties
```

3. Kreiramo ulaznu temu:
```sh
./bin/kafka-topics.sh --create --topic input --bootstrap-server localhost:9092
```

4. Kreiramo izlaznu temu:
```sh
./bin/kafka-topics.sh --create --topic output --bootstrap-server localhost:9092
```

5. Pokrećemo proizvođača
```sh
./bin/kafka-console-producer.sh --broker-list localhost:9092 --topic input
```

Dobićemo konzolu u koju možemo da upisujemo tekst koji želimo da obradimo. Na ovaj način simuliramo ulaz u naš sistem.

5. Pokrećemo potrošača
```sh
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic output
```

Na ovaj način smo se pretplatili na izlaznu temu iz sistema. U ovoj konzoli ćemo moći da vidimo rezulatate obrade, broj pojavljivanja reči za rečenice koje unosimo u konzoli potrošača.

6. Pokrećemo aplikaciju

U slučaju korišćenja IntelliJ IDEA pokrenuti *WordCountJob* klikom na dugme *run*. A u suprotnom moguće je i pokretanje iz komandne linije na sledeći način:
```sh
sbt "runMain example.WordCountJob"
```

Komandu izvršiti iz korenog direktorijuma projekta.

Umesto ili pored koraka broj 5, radi testiranja, moguće je koristiti i *python* skriptu *kafka_producer.py* iz preuzetog primera. Skripta nasumično generiše rečenice i upisuje ih u ulaznu temu (*input*).
