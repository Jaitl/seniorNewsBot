package com.github.jaitl.seniornews.telegram

import com.github.jaitl.seniornews.SubscriberStorageConfig
import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.Serializer
import scala.collection.JavaConverters._

import scala.util.Try

class SubscriberStorage(config: SubscriberStorageConfig) {
  private val db: DB = DBMaker.fileDB(config.dbPath).make()
  private val storage = db.hashSet("subscribers").serializer(Serializer.LONG).createOrOpen()

  def addSubscriber(id: Long): Try[Unit] = Try {
    storage.add(id)
    db.commit()
  }

  def removeSubscriber(id: Long): Try[Unit] = Try {
    storage.remove(id)
    db.commit()
  }

  def subscribers(): Set[Long] = storage.asScala.toSet.map(Long2long)

  def close(): Unit = db.close()
}
