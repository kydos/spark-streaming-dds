package org.apache.spark.streaming.dds

import dds.prelude._
import dds._
import dds.config.DefaultEntities.{defaultDomainParticipant, defaultPolicyFactory}
import org.apache.spark.storage.StorageLevel
import org.omg.dds.sub.DataReaderQos
import scala.collection.JavaConversions._
import org.apache.spark.streaming.receiver.Receiver

object DDSReceiver {

  def createDataReader[T: Manifest](topicName: String, sub: org.omg.dds.sub.Subscriber, qos: org.omg.dds.sub.DataReaderQos) = {
    val topic = Topic[T](topicName)
    implicit val s = sub
    val dr = DataReader[T](topic, qos)
    dr
  }

  def apply[T: Manifest](topicName: String, storageLevel: StorageLevel) = {
    val sub = dds.config.DefaultEntities.defaultSub
    val dr: org.omg.dds.sub.DataReader[T] = createDataReader(topicName, sub, sub.getDefaultDataReaderQos())
    new DDSReceiver[T](dr, storageLevel)
  }

  def apply[T: Manifest](topicName: String, qos: org.omg.dds.sub.DataReaderQos, storageLevel: StorageLevel) = {
    val sub = dds.config.DefaultEntities.defaultSub
    val dr: org.omg.dds.sub.DataReader[T] = createDataReader(topicName, sub, qos)
    new DDSReceiver[T](dr, storageLevel)
  }

  def apply[T: Manifest](topicName: String, partition: String, qos: org.omg.dds.sub.DataReaderQos, storageLevel: StorageLevel) = {
    val subQos = SubscriberQos().withPolicy(Partition(partition))
    implicit val sub = Subscriber(subQos)
    val dr: org.omg.dds.sub.DataReader[T] = createDataReader(topicName, sub, qos)
    new DDSReceiver[T](dr, storageLevel)
  }

  def apply[T: Manifest](topicName: String, partitions: List[String], qos: org.omg.dds.sub.DataReaderQos, storageLevel: StorageLevel) = {
    val subQos = SubscriberQos().withPolicy(Partition(partitions))
    implicit val sub = Subscriber(subQos)
    val dr: org.omg.dds.sub.DataReader[T] = createDataReader(topicName, sub, qos)
    new DDSReceiver[T](dr, storageLevel)
  }
}

class DDSReceiver[T](dr: org.omg.dds.sub.DataReader[T], storageLevel: StorageLevel ) extends Receiver[T](storageLevel) {

  private var lid: Int = 0

  /**
   * store samples store the received values as a reliable or unreliable receiver
   * depending on the DataReader Reliability QoS
   */
  val storeSamples = if (dr.getQos.getReliability.getKind == Reliability.Reliable.getKind) {
    (i: Iterator[T]) => this.store(i)
  }
  else {
    (i: Iterator[T]) => i.foreach(this.store(_))
  }

  def onStart(): Unit = {
    lid = dr listen {
      case DataAvailable(_) => {
        val data = dr.take().filter(_.getData != null).map(_.getData)
        storeSamples(data)
      }
    }
  }

  def onStop(): Unit = {
    dr.deaf(lid)
    dr.close()
  }
}
