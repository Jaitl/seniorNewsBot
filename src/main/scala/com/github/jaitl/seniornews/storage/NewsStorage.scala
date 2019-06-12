package com.github.jaitl.seniornews.storage

import com.github.jaitl.seniornews.NewsStorageConfig
import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.Serializer
import scala.collection.JavaConverters._

import scala.util.Try

class NewsStorage(config: NewsStorageConfig) {
  private val db: DB = DBMaker.fileDB(config.dbPath).make()
  private val storage =
    db.hashSet("newsids")
      .serializer(Serializer.STRING)
      .expireMaxSize(config.storeCount)
      .createOrOpen()

  def addItems(id: Set[String]): Try[Unit] = Try {
    storage.addAll(id.asJava)
    db.commit()
  }

  def items(): Set[String] = storage.asScala.toSet

  def close(): Unit = db.close()
}
